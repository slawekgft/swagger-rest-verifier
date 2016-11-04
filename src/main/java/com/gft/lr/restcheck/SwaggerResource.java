package com.gft.lr.restcheck;

/**
 * Created on 02/11/16.
 */
public class SwaggerResource {
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

    @Override
    public String toString() {
        return "SwaggerResource{" +
                "fileName='" + fileName + '\'' +
                ", fileNamePrefix='" + fileNamePrefix + '\'' +
                ", source='" + source + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}

