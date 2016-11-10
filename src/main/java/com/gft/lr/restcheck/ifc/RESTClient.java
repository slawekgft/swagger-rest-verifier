package com.gft.lr.restcheck.ifc;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;

import java.io.IOException;

/**
 * Created by Sławomir Węgrzyn, GFT on 20/10/16.
 */
public interface RESTClient {
    int executeMethod(final HttpMethod method) throws IOException;
    HttpMethod createGetMethod(final String url);
}
