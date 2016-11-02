package com.gft.lr.restcheck;

import com.gft.lr.restcheck.ifc.CommandExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Sławomir Węgrzyn, GFT on 19/10/16.
 */
public class MockCommandExecutor implements CommandExecutor {

    public static final Logger log = LoggerFactory.getLogger(MockCommandExecutor.class);
    private List<String> execs = Collections.synchronizedList(new ArrayList<>());

    class MockProcess extends Process {

        @Override
        public OutputStream getOutputStream() {
            return new ByteArrayOutputStream();
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(new byte[]{});
        }

        @Override
        public InputStream getErrorStream() {
            return new ByteArrayInputStream(new byte[]{});
        }

        @Override
        public int waitFor() throws InterruptedException {
            return 0;
        }

        @Override
        public int exitValue() {
            return 0;
        }

        @Override
        public void destroy() {

        }
    }

    @Override
    public Process exec(String temporaryJson, String sourceFilePath) throws IOException {
        log.debug("temporaryJson = '" + temporaryJson + "', sourceFilePath = '" + sourceFilePath + "'");
        execs.add("temporaryJson = '" + temporaryJson + "', sourceFilePath = '" + sourceFilePath + "'");
        log.debug("<<");

        return new MockProcess();
    }

    public Collection<String> getExecs() {
        return execs;
    }
}
