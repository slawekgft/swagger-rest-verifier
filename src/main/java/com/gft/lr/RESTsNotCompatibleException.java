package com.gft.lr;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Created by Sławomir Węgrzyn, GFT on 18/10/16.
 */
public class RESTsNotCompatibleException extends Exception {

    private final Collection<RESTSpecLRControllerTest.SwaggerResource> problematicSpecs;

    public RESTsNotCompatibleException(Collection<RESTSpecLRControllerTest.SwaggerResource> problematicSpecs) {
        super("Following REST specs are not backward compatible: \n");
        if(problematicSpecs == null) {
            throw new NullPointerException();
        }
        this.problematicSpecs = problematicSpecs;
    }

    public Collection<RESTSpecLRControllerTest.SwaggerResource> getProblematicSpecs() {
        return Collections.unmodifiableCollection(problematicSpecs);
    }

    public String getMessage() {
        return StringUtils.defaultString(super.getMessage())
                + getProblematicSpecs().stream()
                    .map(swaggerResource -> swaggerResource.getUrl())
                    .collect(Collectors.joining(",\n"));
    }
}
