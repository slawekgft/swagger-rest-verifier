package com.gft.lt.test;

/**
 * Created by Sławomir Węgrzyn, GFT on 20/10/16.
 */
public final class TestUtil {
    private TestUtil() {
    }

    public static final String sysProp(String systemPropertyName) {
        return System.getProperty(systemPropertyName);
    }
}
