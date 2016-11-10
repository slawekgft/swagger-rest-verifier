package com.gft.lr.restcheck;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Created by Sławomir Węgrzyn, GFT on 18/10/16.
 */
public class RESTsNotCompatibleException extends Exception {

    public static final String SEPARATOR = "============================================\n";
    private final Collection<SwaggerResource> problematicSpecs;

    public RESTsNotCompatibleException(Collection<SwaggerResource> problematicSpecs) {
        super("-----------------------------------------------------\n" +
                "| Following REST specs are not backward compatible: |\n" +
                "-----------------------------------------------------\n");
        if (problematicSpecs == null) {
            throw new NullPointerException();
        }
        this.problematicSpecs = problematicSpecs;
    }

    public Collection<SwaggerResource> getProblematicSpecs() {
        return Collections.unmodifiableCollection(problematicSpecs);
    }

    public String getMessage() {
        return StringUtils.defaultString(super.getMessage())
                + getProblematicSpecs().stream()
                .map(swaggerResource -> {
                    return SEPARATOR + swaggerResource.getUrl() + "\n" + swaggerResource.getError();
                })
                .collect(Collectors.joining("\n"));
    }
}
