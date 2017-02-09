package com.gft.lr.restcheck;

import com.typesafe.config.Config;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.api.Application;
import play.api.Configuration;
import play.api.inject.ApplicationLifecycle;
import scala.Some;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;

/**
 * Created on 27/01/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class JsonPreparatorTest {

    public static final String FN = "1";
    public static final String FN_PREF = "2";
    public static final String URL_META = "http://apphost:9900/api/metadata/meta.json";

    @Mock
    private Application application;

    @Mock
    private Configuration configuration;

    @Mock
    private Config config;

    @Mock
    private ApplicationLifecycle lifecycle;

    @Mock
    private ClassLoader classLoader;

    @Before
    public void setUp() {
        ClassLoader classLoader = this.getClass().getClassLoader();

        given(application.configuration()).willReturn(configuration);
        given(application.classloader()).willReturn(classLoader);

        given(configuration.getString(anyString(), any())).willReturn(new Some<>(""));
        given(configuration.underlying()).willReturn(config);

        given(config.hasPath("play.http.router")).willReturn(true);
        given(configuration.getString("play.http.router", null)).willReturn(new Some<>("routes"));
    }

    @Test
    public void shouldPrepareSwaggerJson() throws Exception {
        // given
        JsonPreparator jsonPreparator = new JsonPreparator(lifecycle, application, classLoader);
        SwaggerResource swaggerResource = createSwagger(FN, FN_PREF, URL_META);
        // when
        SwaggerResource swaggerWithJson = jsonPreparator.prepareJson(swaggerResource);
        // then
        verify(configuration).getString("play.http.router", null);
        assertThat(swaggerWithJson).isNotNull();
        assertThat(swaggerWithJson.getSource()).isNotNull();
        assertThat(swaggerWithJson.getFileName()).isEqualTo(FN);
        assertThat(swaggerWithJson.getFileNamePrefix()).isEqualTo(FN_PREF);
        assertThat(swaggerWithJson.getUrl()).isEqualTo(URL_META);
    }

    public static final SwaggerResource createSwagger(String fileName, String fileNamePrefix, String url) {
        SwaggerResource swaggerResource = new SwaggerResource(fileName, fileNamePrefix, url);

        return swaggerResource;
    }
}
