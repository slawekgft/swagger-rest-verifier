package com.gft.lr.restcheck;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Created on 19/10/16.
 */
public class CommandExecutor {

    public static final Logger log = LoggerFactory.getLogger(CommandExecutor.class);

    public static final String SWAGGER_DIFF = "swagger-diff --incompatibilities {old} {new}";
    public static final String SWAGGER_CONV = "java -jar swagger-codegen-cli.jar generate -i {json} -l swagger-yaml -o {destYamlDir}";
    public static final String DEFAULT_SWAGGER_YAML = "swagger.yaml";

    private final RuntimeExecutor runtimeExecutor;

    public CommandExecutor(final RuntimeExecutor runtimeExecutor) {
        this.runtimeExecutor = runtimeExecutor;
    }

    private String cmdForFiles(final String temporaryJson, final String sourceFilePath) {
        String cmd = setParams(SWAGGER_DIFF, new String[]{sourceFilePath, temporaryJson}, new String[]{"old", "new"});
        log.info("Issued command: " + cmd);

        return cmd;
    }

    public File convert(final String jsonFilePath, final String outputDir) throws IOException {
        String cmd = setParams(SWAGGER_CONV, new String[]{jsonFilePath, outputDir}, new String[]{"json", "destYamlDir"});
        Process process = runtimeExecutor.exec(cmd);
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
        if (process.exitValue() == 0) {
            log.debug("temp file is '" + outputDir + File.separator + DEFAULT_SWAGGER_YAML + "'");
            return new File(outputDir + File.separator + DEFAULT_SWAGGER_YAML);
        }
        throw new IOException("Convertion of '" + jsonFilePath + "' failed!");
    }

    public Process compare(final String temporaryJson, final String sourceFilePath) throws IOException {
        return runtimeExecutor.exec(cmdForFiles(temporaryJson, sourceFilePath));
    }

    private String setParams(String template, final String[] values, final String[] names) {
        for (int i = 0; i < values.length; i++) {
            template = template.replaceFirst("\\{" + names[i] + "\\}", values[i]);
        }

        return template;
    }

}
