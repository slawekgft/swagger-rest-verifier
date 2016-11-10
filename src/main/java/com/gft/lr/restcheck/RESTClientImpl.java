package com.gft.lr.restcheck;

import com.gft.lr.restcheck.ifc.RESTClient;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * Created on 20/10/16.
 */
public class RESTClientImpl implements RESTClient {

    private static final Logger log = LoggerFactory.getLogger(RESTClientImpl.class);

    @Override
    public int executeMethod(final HttpMethod method) throws IOException {
        HttpClient httpClient = new HttpClient();
        int status = httpClient.executeMethod(method);

        if (status != HttpURLConnection.HTTP_OK) {
            log.warn("executeMethod '" + method.getPath() + "'\nreturned: [" + status + "] and\n'" + method.getResponseBodyAsString() + "'");
        }

        return status;
    }

    @Override
    public HttpMethod createGetMethod(final String url) {
        return new GetMethod(url);
    }
}
