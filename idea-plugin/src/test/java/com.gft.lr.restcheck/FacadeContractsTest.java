package com.gft.lr.restcheck;

import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;

/**
 * Created on 11/01/17.
 */

public class FacadeContractsTest {

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
        FacadeContracts facadeContracts = new FacadeContracts("", new SwaggerBuilder());

        // when
        List<SwaggerResource> swaggerResources = facadeContracts.swaggerResources();

        // then
        assertThat(swaggerResources).hasSize(swaggers.size());
    }

    private SwaggerResource createSwagger(String fileName, String fileNamePrefix, String url) {
        SwaggerResource swaggerResource = new SwaggerResource(fileName, fileNamePrefix, url);

        return swaggerResource;
    }
}
