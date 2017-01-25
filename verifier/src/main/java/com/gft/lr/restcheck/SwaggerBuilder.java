package com.gft.lr.restcheck;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.strip;

/**
 * Created on 04/11/16.
 */
public class SwaggerBuilder {

    public static final Logger log = LoggerFactory.getLogger(SwaggerBuilder.class);

    public static final String PUBLIC_INTERFACESPEC_DIR = "yamls" + File.separator;
    public static final String SWAGGER_API_DOCS_URL_ENV = System.getProperty(RESTSpecLRValidator.ENV_PREF + "url")
            .trim().replaceFirst("/$", "") + "/{filename}.json";

    private SwaggerResource createSwaggerResource(Path path) {
        final String optionalPrefix;
        {
            String prefixTmp =
                    strip(path.getParent().toFile().getAbsolutePath().replace(
                            getRESTSpecsFullPath(), ""), File.separator)
                            .replaceAll(File.separator, RESTSpecLRValidator.URL_SEPARATOR.toString());
            if (StringUtils.isNotBlank(prefixTmp)) {
                prefixTmp = prefixTmp + RESTSpecLRValidator.URL_SEPARATOR.toString();
            }
            optionalPrefix = prefixTmp;
        }
        return new SwaggerResource(
                path.getFileName().toString(),
                optionalPrefix,
                getApiUrl(path.getFileName().toString(), optionalPrefix));
    }

    public Collection<SwaggerResource> prepareSwaggers(Function<String, Stream<Path>> getPaths, Predicate<Path> notIgnored) throws IOException {
        Stream<Path> pathStream = getPaths.apply(getRESTSpecsRelativePath());
        return pathStream
                .filter(path -> Files.isRegularFile(path))
                .filter(path -> notIgnored.test(path))
                .filter(path -> path.getFileName().toString().toLowerCase().contains(".yml")
                        || path.getFileName().toString().toLowerCase().contains(".yaml"))
                .map(path -> createSwaggerResource(path))
                .collect(Collectors.toList());
    }

    public String yamlExtReplace(String fileName, String replacement) {
        return fileName.replaceFirst("\\.yaml$", replacement).replaceFirst("\\.yml$", replacement);
    }

    public String getRESTSpecsRelativePath() {
        String lrPath = RESTSpecLRValidator.LOMBARD_RISK_REST_SPEC_PATH_ENV.trim().replaceFirst(File.separator + "$", "");
        return lrPath + File.separator + getPublicInterfaceSpecDir();
    }

    private String getApiUrl(String baseFileName, String prefix) {
        return SWAGGER_API_DOCS_URL_ENV.replaceAll("\\{filename\\}", prefix + noFormatName(baseFileName));
    }

    private String getRESTSpecsFullPath() {
        return new File(getRESTSpecsRelativePath()).getAbsolutePath();
    }

    private String noFormatName(String fileName) {
        return yamlExtReplace(fileName, "");
    }

    protected String getPublicInterfaceSpecDir() {
        return PUBLIC_INTERFACESPEC_DIR;
    }
}
