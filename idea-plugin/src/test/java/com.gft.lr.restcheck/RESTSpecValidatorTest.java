package com.gft.lr.restcheck;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import swagger.JsonPreparator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

/**
 * Created on 11/01/17.
 */

public class RESTSpecValidatorTest {

    @BeforeClass
    public static void setUpClass () {
        System.setProperty("lr.restwatch.url", "src/test/tmp");
        System.setProperty("lr.restwatch.rest.spec.path", "src/test/contracts");
    }

    @Test
    public void shouldReadPathsTest() throws IOException {
        // given
        Collection<SwaggerResource> swaggers = new ArrayList<>();
        swaggers.add(createSwagger("","",""));
        swaggers.add(createSwagger("","",""));
        swaggers.add(createSwagger("","",""));
        swaggers.add(createSwagger("","",""));
        swaggers.add(createSwagger("","",""));
        SwaggerBuilder swaggerBuilder = Mockito.mock(SwaggerBuilder.class);
        given(swaggerBuilder.getRESTSpecsRelativePath()).willReturn("");
        given(swaggerBuilder.prepareSwaggers(any(Function.class), any(Predicate.class))).willReturn(swaggers);
        JsonPreparator jsonPreparator = Mockito.mock(JsonPreparator.class);
        RESTVerifierConf restVerifierConf = mock(RESTVerifierConf.class);
        given(jsonPreparator.prepareJson(any(SwaggerResource.class))).willReturn(swaggers.iterator().next());
        RESTSpecValidator restSpecValidator = new RESTSpecValidator(
                "", swaggerBuilder, jsonPreparator, restVerifierConf);

        // when
        List<SwaggerResource> swaggerResources = restSpecValidator.swaggerResources();

        // then
        assertThat(swaggerResources).hasSize(swaggers.size());
    }

    private SwaggerResource createSwagger(String fileName, String fileNamePrefix, String url) {
        SwaggerResource swaggerResource = new SwaggerResource(fileName, fileNamePrefix, url);

        return swaggerResource;
    }
}
