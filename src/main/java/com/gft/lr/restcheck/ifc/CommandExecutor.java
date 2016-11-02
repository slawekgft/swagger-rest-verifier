package com.gft.lr.restcheck.ifc;

import java.io.IOException;

/**
 * Created by Sławomir Węgrzyn, GFT on 19/10/16.
 */
public interface CommandExecutor {
    Process exec(final String temporaryJson, final String sourceFilePath) throws IOException;
}
