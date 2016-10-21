package com.gft.lr.restcheck;

import com.gft.lr.restcheck.ifc.CommandIssuer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by Sławomir Węgrzyn, GFT on 19/10/16.
 */
public class CommandIssuerImpl implements CommandIssuer {

    public static final Logger log = LoggerFactory.getLogger(CommandIssuerImpl.class);

    public static final String SWAGGER_DIFF = "swagger-diff --incompatibilities {old} {new}";

    private String cmdForFiles(String temporaryJson, String sourceFilePath) {
        String cmd = SWAGGER_DIFF.replaceFirst("\\{old\\}", sourceFilePath).replaceAll("\\{new\\}", temporaryJson);
        log.info("Issued command: " + cmd);

        return cmd;
    }

    public Process exec(final String temporaryJson, final String sourceFilePath) throws IOException {
        return Runtime.getRuntime().exec(cmdForFiles(temporaryJson, sourceFilePath));
    }
}
