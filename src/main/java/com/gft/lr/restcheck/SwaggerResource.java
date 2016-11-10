package com.gft.lr.restcheck;

import org.apache.commons.lang3.StringUtils;

/**
 * Created on 02/11/16.
 */
public class SwaggerResource {
    private String fileName;
    private String fileNamePrefix;
    private String source;
    private String url;
    private String error;

    SwaggerResource(SwaggerResource swaggerResource, String error) {
        this(swaggerResource.getSource(), swaggerResource);
        this.error = error;
    }

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

    public boolean valid() {
        return StringUtils.isNotEmpty(getSource());
    }

    @Override
    public String toString() {
        if(null != error) {
            return "SwaggerResource{" +
                    "fileName='" + fileName + '\'' +
                    ", error='" + error + '\'' +
                    ", fileNamePrefix='" + fileNamePrefix + '\'' +
                    ", source='" + source + '\'' +
                    ", url='" + url + '\'' +
                    '}';
        } else {
            return "SwaggerResource{" +
                    "fileName='" + fileName + '\'' +
                    ", fileNamePrefix='" + fileNamePrefix + '\'' +
                    ", source='" + source + '\'' +
                    ", url='" + url + '\'' +
                    '}';
        }
    }

    public String getError() {
        return error;
    }
}

