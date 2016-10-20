package com.gft.lr.restcheck;

import com.gft.lr.restcheck.ifc.RESTClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by Sławomir Węgrzyn, GFT on 20/10/16.
 */
public class MockRESTClient implements RESTClient {

    private int response;

    public Collection<String> getPassedUrls() {
        return passedUrls;
    }

    private Collection<String> passedUrls = new ArrayList<String>();
    private HttpMethod httpMethod;

    public MockRESTClient(int response, HttpMethod httpMethod) {
        this.response = response;
        this.httpMethod = httpMethod;
    }

    @Override
    public int executeMethod(HttpMethod method) throws IOException, HttpException {
        return response;
    }

    @Override
    public HttpMethod createGetMethod(String url) {
        passedUrls.add(url);

        return httpMethod;
    }
}
