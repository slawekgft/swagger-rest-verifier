package com.gft.lr.restcheck;


import com.gft.lr.restcheck.ifc.CommandIssuer;
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

import static org.apache.commons.lang3.StringUtils.strip;

/**
 * @author: Sławomir Węgrzyn
 * @date: 17/10/2016
 */
public class RESTSpecLRChecker {

    public static final Logger log = LoggerFactory.getLogger(RESTSpecLRChecker.class);

    public static final String ENV_PREF = "lr.restwatch.";
    public static final String LOMBARD_RISK_REST_SPEC_PATH_ENV = System.getProperty(ENV_PREF + "rest.spec.path");
    public static final String SWAGGER_API_DOCS_URL_ENV = System.getProperty(ENV_PREF + "url").trim().replaceFirst("/$", "") + "/{filename}.json";

    public static final String UTF_8_CHARSET = "utf-8";
    public static final String PUBLIC_INTERFACESPEC_DIR = StringUtils.join(new String[]{"yamls", ""}, File.separator);
    public static final int HTTP_OK = 200;
    public static final int SEARCH_DEPTH_IS_2 = 2;
    public static final Character URL_SEPARATOR = '/';

    private CommandIssuer commandIssuer;
    private String filterUrl;
    private RESTClient restClient;

    public RESTSpecLRChecker(CommandIssuer commandIssuer, RESTClient restClient) {
        this.commandIssuer = commandIssuer;
        this.restClient = restClient;
    }

    public RESTSpecLRChecker(CommandIssuer commandIssuer, RESTClient restClient, String filterUrl) {
        this(commandIssuer, restClient);
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
        String fullPath = getRESTSpecsRelativePath();
        return fullPath + swaggerResource.getFileNamePrefix() + swaggerResource.getFileName();
    }

    private String getRESTSpecsFullPath() {
        return new File(getRESTSpecsRelativePath()).getAbsolutePath();
    }

    private String getRESTSpecsRelativePath() {
        String lrPath = LOMBARD_RISK_REST_SPEC_PATH_ENV.trim().replaceFirst(File.separator + "$", "");
        return lrPath + File.separator + PUBLIC_INTERFACESPEC_DIR;
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
        return yamlExtReplace(fileName, ".json");
    }

    private String noFormatName(String fileName) {
        return yamlExtReplace(fileName, "");
    }

    private String yamlExtReplace(String fileName, String replacement) {
        return fileName.replaceFirst("\\.yaml$", replacement).replaceFirst("\\.yml$", replacement);
    }

    private Collection<SwaggerResource> prepareSwaggers() throws IOException {
        Stream<Path> pathStream = Files.walk(Paths.get(getRESTSpecsRelativePath()), SEARCH_DEPTH_IS_2);
        return pathStream
                .filter(path -> Files.isRegularFile(path))
                .filter(path -> path.getFileName().toString().toLowerCase().contains(".yml")
                             || path.getFileName().toString().toLowerCase().contains(".yaml"))
                .map(path -> createSwaggerResource(path))
                .collect(Collectors.toList());
    }

    private SwaggerResource createSwaggerResource(Path path) {
        final String optionalPrefix;
        {
            String prefixTmp =
                    strip(path.getParent().toFile().getAbsolutePath().replace(
                            getRESTSpecsFullPath(), ""), File.separator)
                    .replaceAll(File.separator, URL_SEPARATOR.toString());
            if (StringUtils.isNotBlank(prefixTmp)) {
                prefixTmp = prefixTmp + URL_SEPARATOR.toString();
            }
            optionalPrefix = prefixTmp;
        }
        return new SwaggerResource(
                path.getFileName().toString(),
                optionalPrefix,
                getApiUrl(path.getFileName().toString(), optionalPrefix));
    }

    private String getApiUrl(String baseFileName, String prefix) {
        return SWAGGER_API_DOCS_URL_ENV.replaceAll("\\{filename\\}", prefix + noFormatName(baseFileName));
    }

    final static class SwaggerResource {
        private String fileName;
        private String fileNamePrefix;
        private String source;
        private String url;

        SwaggerResource(String source, SwaggerResource swaggerResource) {
            this.source = source;
            this.fileName = swaggerResource.getFileName();
            this.fileNamePrefix = swaggerResource.getFileNamePrefix();
            this.url = swaggerResource.getUrl();
        }

        SwaggerResource(String fileName, String fileNamePrefix, String url) {
            this.fileName = fileName;
            this.url = url;
            this.fileNamePrefix = fileNamePrefix;
        }

        public String getFileName() {
            return fileName;
        }

        public String getSource() {
            return source;
        }

        public String getUrl() {
            return url;
        }

        public String getFileNamePrefix() {
            return fileNamePrefix;
        }
    }

    private Collection<SwaggerResource> prepareJSons(Collection<SwaggerResource> swaggers) {
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

        log.debug("Tested JSON:\n" + jsonsSwaggers.get(0).getSource());

        return jsonsSwaggers;
    }
}