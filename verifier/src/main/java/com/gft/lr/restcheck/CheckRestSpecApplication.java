package com.gft.lr.restcheck;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CheckRestSpecApplication {
    public static final Logger log = LoggerFactory.getLogger(CheckRestSpecApplication.class);

    public static void main(String[] args) {
        try {
            String filterUrl = args.length > 0 ? args[0] : "";
            new RESTSpecLRValidator(
                    new CommandExecutor(new RuntimeExecutor()),
                    new RESTClientImpl(),
                    filterUrl,
                    new SwaggerBuilder(),
                    new RESTVerifierConf()).checkIfRestIsBackwardCompatible();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            System.exit(1);
        } catch (RESTsNotCompatibleException e) {
            System.err.print(e.getMessage());
            System.exit(1);
        }
    }
}
