package com.gft.lr.restcheck;

import com.gft.lr.restcheck.ifc.RESTClient;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.IOException;

/**
 * Created by Sławomir Węgrzyn, GFT on 20/10/16.
 */
public class RESTClientImpl implements RESTClient {

    private HttpClient httpClient = new HttpClient();

    @Override
    public int executeMethod(final HttpMethod method) throws IOException, HttpException {
        return httpClient.executeMethod(method);
    }

    @Override
    public HttpMethod createGetMethod(final String url) {
        return new GetMethod(url);
    }
}
