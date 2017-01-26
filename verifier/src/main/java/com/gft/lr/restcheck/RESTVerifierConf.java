package com.gft.lr.restcheck;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created on 09/01/17.
 */
public class RESTVerifierConf {
    public static final Logger log = LoggerFactory.getLogger(RESTVerifierConf.class);

    public static final String VALIDATORIGNORE = ".validatorignore";

    RESTVerifierConf() {}

    public Set<String> readIngnoreConfiguration(final String ignorePath) {
        final Set<String> ignoredPaths = new HashSet<>();
        if (new File(ignorePath).exists()) {
            try (InputStream inputStream
                         = new FileInputStream(ignorePath)) {
                if (null != inputStream) {
                    final LineNumberReader ignoredReader = new LineNumberReader(new InputStreamReader(inputStream));
                    String line;
                    while ((line = ignoredReader.readLine()) != null) {
                        ignoredPaths.add(line);
                    }
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }

        return ignoredPaths;
    }

}
