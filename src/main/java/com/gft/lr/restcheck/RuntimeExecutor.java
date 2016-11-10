package com.gft.lr.restcheck;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created on 09/11/16.
 */
public class RuntimeExecutor {
    private static final Logger log = LoggerFactory.getLogger(RuntimeExecutor.class);

    public Process exec (String commad) throws IOException {
        log.debug("> " + commad);
        return Runtime.getRuntime().exec(commad);
    }
}
