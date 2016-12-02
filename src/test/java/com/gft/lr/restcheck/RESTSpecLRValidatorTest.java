package com.gft.lr.restcheck;

import com.gft.lr.restcheck.ifc.RESTClient;
import org.apache.commons.httpclient.HttpMethod;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.gft.lt.test.TestUtil.sysProp;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created on 19/10/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class RESTSpecLRValidatorTest {

    public static final Logger log = LoggerFactory.getLogger(RESTSpecLRValidatorTest.class);

    public static final String FILTER_URL = "_1";
    public static final int SPECS_THAT_MATCH_COUNT = 3;
    public static final int ALL_SPECS_COUNT = 5;
    public static final byte[] BUF_FOR_MESSAGE = "abc".getBytes();
    public static final byte[] EMPTY_BUF = {};
    public static final int HTTP_NOT_FOUND = 404;

    private List<String> execs;

    @Mock
    private Process process;

    @Before
    public void setUp() {
        execs = Collections.synchronizedList(new ArrayList<>());
        System.setProperty(RESTSpecLRValidator.ENV_PREF + "rest.spec.path", "src/test/resources");
        System.setProperty(getUrlPropName(), "http://localhost:9000/rest/");
        given(process.getOutputStream()).willReturn(new ByteArrayOutputStream());
        given(process.getErrorStream()).willReturn(new ByteArrayInputStream(EMPTY_BUF));
        given(process.getInputStream()).willReturn(new ByteArrayInputStream(EMPTY_BUF));
    }

    @Test
    public void shouldNotFailWhenNoIgnoreFile() throws Exception {
        // given
        System.setProperty(RESTSpecLRValidator.ENV_PREF + "rest.spec.path", "src/test/resources/yamls2");
        CommandExecutor cmdIss = mock(CommandExecutor.class);
        RESTClient restClient = mock(RESTClient.class);
        HttpMethod getHTTPMethod = mock(HttpMethod.class);
        RESTSpecLRValidator restSpecLRValidator = new RESTSpecLRValidator(cmdIss, restClient, null, new SwaggerBuilder() {
            protected String getPublicInterfaceSpecDir() {
                return "yamls2" + File.separator;
            }
        });

        given(cmdIss.compare(anyString(), anyString())).willAnswer(compareAnswer());
        given(cmdIss.convert(anyString(), anyString())).willAnswer(convertAnswer());
        given(restClient.executeMethod(any(HttpMethod.class))).willReturn(RESTSpecLRValidator.HTTP_OK);
        given(restClient.createGetMethod(anyString())).willReturn(getHTTPMethod);
        given(getHTTPMethod.getResponseBodyAsString()).willReturn("swagger: 2.0");

        // when
        restSpecLRValidator.checkIfRestIsBackwardCompatible();

        // then
        // no exception, one verification
    }

    @Test
    public void checkIfRestIsBackwardCompatibleFilteredSpecs() throws Exception {
        // given
        CommandExecutor cmdIss = mock(CommandExecutor.class);
        RESTClient restClient = mock(RESTClient.class);
        HttpMethod getHTTPMethod = mock(HttpMethod.class);
        RESTSpecLRValidator restSpecLRValidator = new RESTSpecLRValidator(cmdIss, restClient, FILTER_URL, new SwaggerBuilder());

        given(cmdIss.compare(anyString(), anyString())).willAnswer(compareAnswer());
        given(cmdIss.convert(anyString(), anyString())).willAnswer(convertAnswer());
        given(restClient.executeMethod(any(HttpMethod.class))).willReturn(RESTSpecLRValidator.HTTP_OK);
        given(restClient.createGetMethod(anyString())).willReturn(getHTTPMethod);
        given(getHTTPMethod.getResponseBodyAsString()).willReturn("swagger: 2.0");

        // when
        restSpecLRValidator.checkIfRestIsBackwardCompatible();

        // then
        final String processedFilesPaths = execs.stream().collect(Collectors.joining());
        assertThat(execs).hasSize(SPECS_THAT_MATCH_COUNT);
        assertThat(processedFilesPaths).contains("/spec0_1.yml");
        assertThat(processedFilesPaths).contains("/spec1_1.yaml");
        assertThat(processedFilesPaths).contains("/spec2_1.yml");
        assertThat(processedFilesPaths).doesNotContain("/spec1_0.yml");
        assertThat(processedFilesPaths).doesNotContain("/spec1_2.yml");
        assertThat(processedFilesPaths).doesNotContain("/spec1_3.yml");
        assertThat(processedFilesPaths).doesNotContain("/spec2_2.yml");
        assertThat(processedFilesPaths).doesNotContain("/ignored.yml");
    }

    @Test
    public void checkIfErrorIfRestNotAwailableSpecs() throws IOException {
        // given
        CommandExecutor cmdIss = mock(CommandExecutor.class);
        RESTClient restClient = mock(RESTClient.class);
        HttpMethod getHTTPMethod = mock(HttpMethod.class);
        RESTSpecLRValidator restSpecLRValidator = new RESTSpecLRValidator(cmdIss, restClient, "spec0_1", new SwaggerBuilder());

        given(restClient.executeMethod(any(HttpMethod.class))).willReturn(HTTP_NOT_FOUND);
        given(restClient.createGetMethod(anyString())).willReturn(getHTTPMethod);
        given(getHTTPMethod.getResponseBodyAsString()).willReturn("swagger: 2.0");

        // when
        RESTsNotCompatibleException exception = null;
        try {
            restSpecLRValidator.checkIfRestIsBackwardCompatible();
        } catch (RESTsNotCompatibleException e) {
            exception = e;
        }

        // then
        assertThat(exception.getProblematicSpecs()).hasSize(1);
        SwaggerResource swaggerResource = exception.getProblematicSpecs().iterator().next();
        assertThat(swaggerResource.getSource()).isNull();
        assertThat(swaggerResource.getError()).contains(RESTSpecLRValidator.WRONG_SERVER_RESPONSE);
        verify(cmdIss, never()).compare(anyString(), anyString());
        verify(cmdIss, never()).convert(anyString(), anyString());
    }

    @Test
    public void checkIfRestIsBackwardCompatibleAllSpecs() throws Exception {
        // given
        CommandExecutor cmdIss = mock(CommandExecutor.class);
        HttpMethod getHTTPMethod = mock(HttpMethod.class);
        MockRESTClient restClient = new MockRESTClient(RESTSpecLRValidator.HTTP_OK, getHTTPMethod);
        RESTSpecLRValidator restSpecLRValidator = new RESTSpecLRValidator(cmdIss, restClient, new SwaggerBuilder());

        given(cmdIss.compare(anyString(), anyString())).willAnswer(compareAnswer());
        given(cmdIss.convert(anyString(), anyString())).willAnswer(convertAnswer());
        given(getHTTPMethod.getResponseBodyAsString()).willReturn("swagger: 2.0");

        // when
        restSpecLRValidator.checkIfRestIsBackwardCompatible();

        // then
        final String producedUrls = restClient.getPassedUrls().stream().collect(Collectors.joining(" "));
        assertThat(execs).hasSize(ALL_SPECS_COUNT);
        assertThat(producedUrls).contains(sysProp(getUrlPropName()) + "spec0_1.json");
        assertThat(producedUrls).contains(sysProp(getUrlPropName()) + "subApi1/spec1_1.json");
        assertThat(producedUrls).contains(sysProp(getUrlPropName()) + "subApi2/spec2_1.json");
        final String processedFilesPaths = execs.stream().collect(Collectors.joining());
        assertThat(processedFilesPaths).contains("/spec1_1.yaml");
        assertThat(processedFilesPaths).contains("/spec1_2.yml");
        assertThat(processedFilesPaths).contains("/spec1_3.yml");
        assertThat(processedFilesPaths).contains("/spec2_1.yml");
        assertThat(processedFilesPaths).contains("/spec0_1.yml");
        assertThat(processedFilesPaths).contains("/subApi1/spec1_1.yaml");
        assertThat(processedFilesPaths).contains("/subApi1/spec1_3.yml");
        assertThat(processedFilesPaths).contains("/subApi1/spec1_2.yml");
        assertThat(processedFilesPaths).contains("/subApi2/spec2_1.yml");
        assertThat(processedFilesPaths).doesNotContain("/ignored/");
        assertThat(processedFilesPaths).doesNotContain("ignored.yml");
    }

    @Test(expected = RESTsNotCompatibleException.class)
    public void checkIfRestIsNOTBackwardCompatibleAllSpecsWithSomeOutput() throws Exception {
        // given
        CommandExecutor cmdIss = mock(CommandExecutor.class);
        HttpMethod getHTTPMethod = mock(HttpMethod.class);
        MockRESTClient restClient = new MockRESTClient(RESTSpecLRValidator.HTTP_OK, getHTTPMethod);
        RESTSpecLRValidator restSpecLRValidator = new RESTSpecLRValidator(cmdIss, restClient, new SwaggerBuilder());
        given(process.getErrorStream()).willReturn(new ByteArrayInputStream(BUF_FOR_MESSAGE));
        given(process.getInputStream()).willReturn(new ByteArrayInputStream(BUF_FOR_MESSAGE));
        given(process.exitValue()).willReturn(1);

        given(cmdIss.compare(anyString(), anyString())).willAnswer(compareAnswer());
        given(cmdIss.convert(anyString(), anyString())).willAnswer(convertAnswer());
        given(getHTTPMethod.getResponseBodyAsString()).willReturn("swagger: 2.0");

        // when
        restSpecLRValidator.checkIfRestIsBackwardCompatible();

        // then
    }

    @Test(expected = RESTsNotCompatibleException.class)
    public void checkIfRestIsNOTBackwardCompatibleAllSpecsWithNoOutput() throws Exception {
        // given
        CommandExecutor cmdIss = mock(CommandExecutor.class);
        HttpMethod getHTTPMethod = mock(HttpMethod.class);
        MockRESTClient restClient = new MockRESTClient(RESTSpecLRValidator.HTTP_OK, getHTTPMethod);
        RESTSpecLRValidator restSpecLRValidator = new RESTSpecLRValidator(cmdIss, restClient, new SwaggerBuilder());
        given(process.exitValue()).willReturn(1);

        given(cmdIss.compare(anyString(), anyString())).willAnswer(compareAnswer());
        given(cmdIss.convert(anyString(), anyString())).willAnswer(convertAnswer());
        given(getHTTPMethod.getResponseBodyAsString()).willReturn("swagger: 2.0");

        // when
        restSpecLRValidator.checkIfRestIsBackwardCompatible();

        // then
    }

    @Test
    public void checkIfRestIsBackwardCompatibleAllSpecsWithSomeOutput() throws Exception {
        // given
        CommandExecutor cmdIss = mock(CommandExecutor.class);
        HttpMethod getHTTPMethod = mock(HttpMethod.class);
        MockRESTClient restClient = new MockRESTClient(RESTSpecLRValidator.HTTP_OK, getHTTPMethod);
        RESTSpecLRValidator restSpecLRValidator = new RESTSpecLRValidator(cmdIss, restClient, new SwaggerBuilder());
        given(process.getErrorStream()).willReturn(new ByteArrayInputStream(BUF_FOR_MESSAGE));
        given(process.getInputStream()).willReturn(new ByteArrayInputStream(BUF_FOR_MESSAGE));

        given(cmdIss.compare(anyString(), anyString())).willAnswer(compareAnswer());
        given(cmdIss.convert(anyString(), anyString())).willAnswer(convertAnswer());
        given(getHTTPMethod.getResponseBodyAsString()).willReturn("swagger: 2.0");

        // when
        restSpecLRValidator.checkIfRestIsBackwardCompatible();

        // then
        assertThat(execs).hasSize(ALL_SPECS_COUNT);
    }

    private Answer<Process> compareAnswer() {
        return invocationOnMock -> {
            String temporaryJson = invocationOnMock.getArgumentAt(0, String.class);
            String sourceFilePath = invocationOnMock.getArgumentAt(1, String.class);
            execs.add("temporaryJson = '" + temporaryJson + "', sourceFilePath = '" + sourceFilePath + "'");
            return process;
        };
    }

    private Answer<File> convertAnswer() {
        return invocation -> {
            String convertedFilePath = invocation.getArgumentAt(0, String.class);
            String outputDir = invocation.getArgumentAt(1, String.class);

            return new File(outputDir + File.separator + CommandExecutor.DEFAULT_SWAGGER_YAML);
        };
    }

    private String getUrlPropName() {
        return RESTSpecLRValidator.ENV_PREF + "url";
    }

}