package com.gft.lr.restcheck;


import com.gft.lr.restcheck.ifc.CommandExecutor;
import com.gft.lr.restcheck.ifc.RESTClient;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author: Sławomir Węgrzyn
 * @date: 17/10/2016
 */
public class RESTSpecLRValidator {

    public static final Logger log = LoggerFactory.getLogger(RESTSpecLRValidator.class);

    public static final String ENV_PREF = "lr.restwatch.";
    public static final String LOMBARD_RISK_REST_SPEC_PATH_ENV = System.getProperty(ENV_PREF + "rest.spec.path");

    public static final String UTF_8_CHARSET = "utf-8";
    public static final int HTTP_OK = 200;
    public static final int SEARCH_DEPTH_IS_2 = 2;
    public static final Character URL_SEPARATOR = '/';

    private CommandExecutor commandIssuer;
    private String filterUrl;
    private SwaggerBuilder swaggerBuilder;
    private RESTClient restClient;

    public RESTSpecLRValidator(CommandExecutor commandIssuer, RESTClient restClient, SwaggerBuilder swaggerBuilder) {
        this.commandIssuer = commandIssuer;
        this.restClient = restClient;
        this.swaggerBuilder = swaggerBuilder;
    }

    public RESTSpecLRValidator(CommandExecutor commandIssuer, RESTClient restClient, String filterUrl, SwaggerBuilder swaggerBuilder) {
        this(commandIssuer, restClient, swaggerBuilder);
        this.filterUrl = filterUrl;
    }

    public void checkIfRestIsBackwardCompatible() throws IOException, RESTsNotCompatibleException {
        final Collection<SwaggerResource> swaggerResources = prepareJSons(filterSwaggers(prepareSwaggers()));
        final Collection<SwaggerResource> problematicJSons = new ArrayList<>();

        swaggerResources.stream().forEach(swaggerResource -> {
            File temporaryJson = null;
            try {
                temporaryJson = storeTempFile(prepareTempDirectory(swaggerResource), swaggerResource);
                String sourceFilePath = getSourceFilePath(swaggerResource);
                if (!isBackwardCompatible(temporaryJson.getAbsolutePath(), sourceFilePath)) {
                    problematicJSons.add(swaggerResource);
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            } finally {
                if (temporaryJson != null) {
                    temporaryJson.delete();
                    new File(temporaryJson.getParent()).delete();
                }
            }
        });

        if (CollectionUtils.isNotEmpty(problematicJSons)) {
            throw new RESTsNotCompatibleException(problematicJSons);
        }
    }

    private Collection<SwaggerResource> filterSwaggers(Collection<SwaggerResource> swaggerResources) {
        if (StringUtils.isNotBlank(getFilterSwaggerUrl())) {
            return swaggerResources.stream().filter(swaggerResource -> swaggerResource.getUrl().contains(getFilterSwaggerUrl())).collect(Collectors.toList());
        }

        return swaggerResources;
    }

    private String getFilterSwaggerUrl() {
        return filterUrl;
    }

    private boolean isBackwardCompatible(final String temporaryJson, final String sourceFilePath) throws IOException {
        Process process = commandIssuer.exec(temporaryJson, sourceFilePath);
        try (BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
             BufferedReader errInput = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String message = getStringFromProcess(stdInput) + "\n" + getStringFromProcess(errInput);
            log.info("Comparing tool output:\n" + message);

            return StringUtils.isBlank(message);
        }
    }

    private String getStringFromProcess(BufferedReader stdInput) throws IOException {
        char[] output = new char[1024];
        int count = stdInput.read(output);

        return count <= 0 ? "" : new String(output);
    }

    private String getSourceFilePath(SwaggerResource swaggerResource) throws FileNotFoundException {
        if (StringUtils.isNotBlank(LOMBARD_RISK_REST_SPEC_PATH_ENV)) {
            return localPathBasedOnSystemProperty(swaggerResource);
        }

        throw new IllegalStateException("no source spec has been found!");
    }

    private String localPathBasedOnSystemProperty(SwaggerResource swaggerResource) {
        String fullPath = swaggerBuilder.getRESTSpecsRelativePath();
        return fullPath + swaggerResource.getFileNamePrefix() + swaggerResource.getFileName();
    }

    private File storeTempFile(File tempDir, SwaggerResource swaggerResource) throws IOException {
        File tempFile = new File(tempDir.getAbsolutePath() + File.separator + jsonFormatName(swaggerResource.getFileName()));
        try (FileOutputStream fos = new FileOutputStream(tempFile);) {
            fos.write(swaggerResource.getSource().getBytes(UTF_8_CHARSET));
        }

        return tempFile;
    }

    private File prepareTempDirectory(SwaggerResource swaggerResource) throws IOException {
        return Files.createTempDirectory(swaggerResource.getFileName()).toFile();
    }

    private String jsonFormatName(String fileName) {
        return swaggerBuilder.yamlExtReplace(fileName, ".json");
    }

    private Collection<SwaggerResource> prepareSwaggers() throws IOException {
        Stream<Path> pathStream = Files.walk(Paths.get(swaggerBuilder.getRESTSpecsRelativePath()), SEARCH_DEPTH_IS_2);
        return pathStream
                .filter(path -> Files.isRegularFile(path))
                .filter(path -> path.getFileName().toString().toLowerCase().contains(".yml")
                        || path.getFileName().toString().toLowerCase().contains(".yaml"))
                .map(path -> swaggerBuilder.createSwaggerResource(path))
                .collect(Collectors.toList());
    }

    private Collection<SwaggerResource> prepareJSons(Collection<SwaggerResource> swaggers) {
        log.debug("prepareJSons from following swaggers: " + swaggers);
        List<SwaggerResource> jsonsSwaggers = swaggers.parallelStream().map(swaggerResource -> {
            HttpMethod getJson = restClient.createGetMethod(swaggerResource.getUrl());
            try {
                int result = restClient.executeMethod(getJson);
                if (result != HTTP_OK) {
                    throw new IllegalStateException("Wrong server response: " + result + " for " + swaggerResource.getUrl());
                }
                return new SwaggerResource(getJson.getResponseBodyAsString(), swaggerResource);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());

        return jsonsSwaggers;
    }
}