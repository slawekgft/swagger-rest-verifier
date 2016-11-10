package com.gft.lr.restcheck;


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

import static java.lang.String.join;

/**
 * Created on 17/10/2016
 */
public class RESTSpecLRValidator {

    public static final Logger log = LoggerFactory.getLogger(RESTSpecLRValidator.class);

    public static final String ENV_PREF = "lr.restwatch.";
    public static final String LOMBARD_RISK_REST_SPEC_PATH_ENV = System.getProperty(ENV_PREF + "rest.spec.path");

    public static final String UTF_8_CHARSET = "utf-8";
    public static final int HTTP_OK = 200;
    public static final int SEARCH_DEPTH_IS_2 = 2;
    public static final Character URL_SEPARATOR = '/';
    public static final String WRONG_SERVER_RESPONSE = "Wrong server response: ";

    private CommandExecutor commandExecutor;
    private String filterUrl;
    private SwaggerBuilder swaggerBuilder;
    private RESTClient restClient;

    public RESTSpecLRValidator(CommandExecutor commandExecutor, RESTClient restClient, SwaggerBuilder swaggerBuilder) {
        this.commandExecutor = commandExecutor;
        this.restClient = restClient;
        this.swaggerBuilder = swaggerBuilder;
    }

    public RESTSpecLRValidator(CommandExecutor commandExecutor, RESTClient restClient, String filterUrl, SwaggerBuilder swaggerBuilder) {
        this(commandExecutor, restClient, swaggerBuilder);
        this.filterUrl = filterUrl;
    }

    public void checkIfRestIsBackwardCompatible() throws IOException, RESTsNotCompatibleException {
        final Collection<SwaggerResource> swaggerResources = prepareJSons(filterSwaggers(prepareSwaggers()));
        final Collection<SwaggerResource> problematicJSons = new ArrayList<>();

        swaggerResources.stream().filter(SwaggerResource::valid).forEach(swaggerResource -> validate(problematicJSons, swaggerResource));
        problematicJSons.addAll(swaggerResources.stream().filter((swaggerResource) -> !swaggerResource.valid()).collect(Collectors.toList()));

        if (CollectionUtils.isNotEmpty(problematicJSons)) {
            throw new RESTsNotCompatibleException(problematicJSons);
        }
    }

    private void validate(Collection<SwaggerResource> problematicJSons, SwaggerResource swaggerResource) {
        File temporaryJson = null;
        File temporaryYaml = null;
        try {
            temporaryJson = storeTempFile(prepareTempDirectory(swaggerResource), swaggerResource);
            temporaryYaml = convert2Yaml(temporaryJson);
            String sourceFilePath = getSourceFilePath(swaggerResource);
            StringBuffer errors = new StringBuffer();
            if (!isBackwardCompatible(temporaryYaml.getAbsolutePath(), sourceFilePath, errors)) {
                problematicJSons.add(new SwaggerResource(swaggerResource, errors.toString()));
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            closeIfNotNull(temporaryJson, false);
            closeIfNotNull(temporaryYaml, true);
        }
    }

    private void closeIfNotNull(File temporaryJson, boolean withDirectory) {
        if (temporaryJson != null) {
            temporaryJson.delete();
            if (withDirectory) {
                new File(temporaryJson.getParent()).delete();
            }
        }
    }

    private File convert2Yaml(File temporaryJson) throws IOException {
        File destYamlDir = temporaryJson.getParentFile();

        return commandExecutor.convert(temporaryJson.getAbsolutePath(), destYamlDir.getAbsolutePath());
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

    private boolean isBackwardCompatible(final String temporaryJson,
                                         final String sourceFilePath,
                                         final StringBuffer outBuffer) throws IOException {
        Process process = commandExecutor.compare(temporaryJson, sourceFilePath);
        try (BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
             BufferedReader errInput = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            process.waitFor();
            String errors = getStringFromStream(errInput);
            String message = join("\n", getStringFromStream(stdInput), errors);
            outBuffer.append(message);
            log.info("Comparing tool output:\n" + message);

            return process.exitValue() == 0;
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            throw new IOException(e);
        }
    }

    private String getStringFromStream(BufferedReader stdInput) throws IOException {
        StringBuffer sb = new StringBuffer();
        int count;
        char[] output = new char[128];
        count = stdInput.read(output);
        while (count > 0) {
            sb.append(output, 0, count);
            count = stdInput.read(output);
        }
        ;

        return sb.toString();
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
                    return new SwaggerResource(swaggerResource, WRONG_SERVER_RESPONSE + result + " for " + swaggerResource.getUrl() + ". See app logs for details.");
                }
                return new SwaggerResource(getJson.getResponseBodyAsString(), swaggerResource);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());

        return jsonsSwaggers;
    }
}