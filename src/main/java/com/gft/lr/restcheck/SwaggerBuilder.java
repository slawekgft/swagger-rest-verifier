package com.gft.lr.restcheck;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.file.Path;

import static org.apache.commons.lang3.StringUtils.strip;

/**
 * Created on 04/11/16.
 */
public class SwaggerBuilder {

    public static final String PUBLIC_INTERFACESPEC_DIR = StringUtils.join(new String[]{"yamls", ""}, File.separator);
    public static final String SWAGGER_API_DOCS_URL_ENV = System.getProperty(RESTSpecLRValidator.ENV_PREF + "url")
            .trim().replaceFirst("/$", "") + "/{filename}.json";

    public SwaggerResource createSwaggerResource(Path path) {
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

    public String yamlExtReplace(String fileName, String replacement) {
        return fileName.replaceFirst("\\.yaml$", replacement).replaceFirst("\\.yml$", replacement);
    }

    public String getRESTSpecsRelativePath() {
        String lrPath = RESTSpecLRValidator.LOMBARD_RISK_REST_SPEC_PATH_ENV.trim().replaceFirst(File.separator + "$", "");
        return lrPath + File.separator + PUBLIC_INTERFACESPEC_DIR;
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
}
