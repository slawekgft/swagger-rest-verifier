package com.gft.lr;


import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
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
 * @author: Słaowomir Węgrzyn
 * @date: 17/10/2016
 */
public class RESTSpecLRControllerTest {

    public static final Logger log = LoggerFactory.getLogger(RESTSpecLRControllerTest.class);

    public static final String SWAGGER_API_DOCS_URL = "http://localhost:9000/{filename}.json";
    public static final String UTF_8_CHARSET = "utf-8";
    public static final String SWAGGER_DIFF = "swagger-diff --incompatibilities {old} {new}";
    public static final String PUBLIC_INTERFACESPEC_DIR = StringUtils.join(new String[]{"public","interfacespec",""}, File.separator);
    public static final int HTTP_OK = 200;
    public static final String LOMBARD_RISK_REST_SPEC_PATH_ENV = System.getProperty("lombard.risk.rest.spec.path");

    public void checkIfRestIsBackwardCompatible() throws IOException, RESTsNotCompatibleException {
        final Collection<SwaggerResource> swaggerResources = prepareJSons(createSwaggers());
        final Collection<SwaggerResource> problematicJSons = new ArrayList<>();

        swaggerResources.parallelStream().forEach(swaggerResource -> {
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
                if(temporaryJson != null) {
                    temporaryJson.delete();
                    new File(temporaryJson.getParent()).delete();
                }
            }
        });

        if(CollectionUtils.isNotEmpty(problematicJSons)) {
            throw new RESTsNotCompatibleException(problematicJSons);
        }
    }

    private boolean isBackwardCompatible(final String temporaryJson, final String sourceFilePath) throws IOException {
        Process process = Runtime.getRuntime().exec(cmdForFiles(SWAGGER_DIFF,temporaryJson,sourceFilePath));
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

    private String cmdForFiles(String swaggerDiff, String temporaryJson, String sourceFilePath) {
        String cmd = swaggerDiff.replaceFirst("\\{old\\}", sourceFilePath).replaceAll("\\{new\\}", temporaryJson);
        log.info("Issued command: " + cmd);

        return cmd;
    }

    private String getSourceFilePath(SwaggerResource swaggerResource) throws FileNotFoundException {
        if(StringUtils.isNotBlank(LOMBARD_RISK_REST_SPEC_PATH_ENV)) {
            return localPathBasedOnSystemProperty(swaggerResource);
        }

        throw new IllegalStateException("no source spec has been found!");
    }

    private String localPathBasedOnSystemProperty(SwaggerResource swaggerResource) {
        String fullPath = getRESTSpecsFullPath();
        return fullPath + swaggerResource.getFileName();
    }

    private String getRESTSpecsFullPath() {
        String lrPath = LOMBARD_RISK_REST_SPEC_PATH_ENV.trim().replaceFirst(File.separator + "$", "");
        return lrPath + File.separator + PUBLIC_INTERFACESPEC_DIR;
    }

    private File storeTempFile(File tempDir, SwaggerResource swaggerResource) throws IOException {
        File tempFile = new File(tempDir.getAbsolutePath() + File.separator + jsonFormatName(swaggerResource.getFileName()));
        try(FileOutputStream fos = new FileOutputStream(tempFile);) {
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

    private Collection<SwaggerResource> createSwaggers() throws IOException {
        Stream<Path> pathStream = Files.list(Paths.get(getRESTSpecsFullPath()));
        return pathStream
                .filter(path -> Files.isRegularFile(path))
                .map(path -> new SwaggerResource(path.getFileName().toString(), getApiUrl(path.getFileName().toString())))
                .collect(Collectors.toList());
    }

    private String getApiUrl(String baseFileName) {
        return SWAGGER_API_DOCS_URL.replaceAll("\\{filename\\}", noFormatName(baseFileName));
    }

    final static class SwaggerResource {
        private String fileName;
        private String source;
        private String url;

        SwaggerResource(String source, SwaggerResource swaggerResource) {
            this.source = source;
            this.fileName = swaggerResource.getFileName();
            this.url = swaggerResource.getUrl();
        }

        SwaggerResource(String fileName, String url) {
            this.fileName = fileName;
            this.url = url;
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
    }

    private Collection<SwaggerResource> prepareJSons(Collection<SwaggerResource> swaggers) {
        List<SwaggerResource> jsonsSwaggers = swaggers.parallelStream().map(swaggerResource -> {
            HttpClient httpClient = new HttpClient();
            HttpMethod getJson = new GetMethod(swaggerResource.getUrl());
            try {
                int result = httpClient.executeMethod(getJson);
                if(result != HTTP_OK) {
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