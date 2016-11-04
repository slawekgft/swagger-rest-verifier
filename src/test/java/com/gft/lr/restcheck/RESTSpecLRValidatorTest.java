package com.gft.lr.restcheck;

import com.gft.lr.restcheck.ifc.CommandExecutor;
import com.gft.lr.restcheck.ifc.RESTClient;
import org.apache.commons.httpclient.HttpMethod;
import org.junit.Before;
import org.junit.Test;
import org.mockito.BDDMockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.stream.Collectors;

import static com.gft.lt.test.TestUtil.sysProp;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

/**
 * Created by Sławomir Węgrzyn, GFT on 19/10/16.
 */
public class RESTSpecLRValidatorTest {

    public static final Logger log = LoggerFactory.getLogger(RESTSpecLRValidatorTest.class);

    public static final String FILTER_URL = "_1";
    public static final int SPECS_THAT_MATCH_COUNT = 3;
    public static final int ALL_SPECS_COUNT = 5;

    @Test
    public void checkIfRestIsBackwardCompatibleAllSpecs() throws Exception {
        // given
        CommandExecutor cmdIss = new MockCommandExecutor();
        HttpMethod getHTTPMethod = BDDMockito.mock(HttpMethod.class);
        MockRESTClient restClient = new MockRESTClient(RESTSpecLRValidator.HTTP_OK, getHTTPMethod);
        RESTSpecLRValidator restSpecLRValidator = new RESTSpecLRValidator(cmdIss, restClient, new SwaggerBuilder());

        given(getHTTPMethod.getResponseBodyAsString()).willReturn("swagger: 2.0");

        // when
        restSpecLRValidator.checkIfRestIsBackwardCompatible();

        // then
        final Collection<String> execs = ((MockCommandExecutor) cmdIss).getExecs();
        final String producedUrls = restClient.getPassedUrls().stream().collect(Collectors.joining(" "));
        assertThat(execs).hasSize(ALL_SPECS_COUNT);
        assertThat(producedUrls).contains(sysProp(getUrlPropName()) + "spec0_1.json");
        assertThat(producedUrls).contains(sysProp(getUrlPropName()) + "subApi1/spec1_1.json");
        assertThat(producedUrls).contains(sysProp(getUrlPropName()) + "subApi2/spec2_1.json");
        final String processedFilesPaths = execs.stream().collect(Collectors.joining());
        assertThat(processedFilesPaths).contains("/spec1_1.json");
        assertThat(processedFilesPaths).contains("/spec1_2.json");
        assertThat(processedFilesPaths).contains("/spec1_3.json");
        assertThat(processedFilesPaths).contains("/spec2_1.json");
        assertThat(processedFilesPaths).contains("/spec0_1.json");
        assertThat(processedFilesPaths).contains("/subApi1/spec1_1.yaml");
        assertThat(processedFilesPaths).contains("/subApi1/spec1_2.yml");
        assertThat(processedFilesPaths).contains("/subApi1/spec1_3.yml");
        assertThat(processedFilesPaths).contains("/subApi2/spec2_1.yml");
        assertThat(processedFilesPaths).contains("/spec0_1.yml");
    }

    @Test
    public void checkIfRestIsBackwardCompatibleFilteredSpecs() throws Exception {
        // given
        CommandExecutor cmdIss = new MockCommandExecutor();
        RESTClient restClient = BDDMockito.mock(RESTClient.class);
        HttpMethod getHTTPMethod = BDDMockito.mock(HttpMethod.class);
        RESTSpecLRValidator restSpecLRValidator = new RESTSpecLRValidator(cmdIss, restClient, FILTER_URL, new SwaggerBuilder());

        given(restClient.executeMethod(any(HttpMethod.class))).willReturn(RESTSpecLRValidator.HTTP_OK);
        given(restClient.createGetMethod(anyString())).willReturn(getHTTPMethod);
        given(getHTTPMethod.getResponseBodyAsString()).willReturn("swagger: 2.0");

        // when
        restSpecLRValidator.checkIfRestIsBackwardCompatible();

        // then
        final Collection<String> execs = ((MockCommandExecutor) cmdIss).getExecs();
        final String processedFilesPaths = execs.stream().collect(Collectors.joining());
        assertThat(execs).hasSize(SPECS_THAT_MATCH_COUNT);
        assertThat(processedFilesPaths).contains("/spec0_1.json");
        assertThat(processedFilesPaths).contains("/spec1_1.json");
        assertThat(processedFilesPaths).contains("/spec2_1.json");
    }

    @Before
    public void setProps() {
        System.setProperty(RESTSpecLRValidator.ENV_PREF + "rest.spec.path", "src/test/resources");
        System.setProperty(getUrlPropName(), "http://localhost:9000/rest/");
    }

    private String getUrlPropName() {
        return RESTSpecLRValidator.ENV_PREF + "url";
    }

}