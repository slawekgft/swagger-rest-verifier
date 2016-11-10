
# Description
This project checks if YAML specs in the Facade project are coherent with what given Facade instance presents by theirs
REST services.
It simply checks if instance REST service is backward compatible with YAML files at provided location.

If you build from sources:
* > mvn clean package
* > cd target
* > rm swagger-rest-validator-\<ver\>.jar
* > mv swagger-rest-validator-\<ver\>-jar-with-dependencies.jar swagger-rest-validator-\<ver\>.jar
* > jar xf swagger-rest-validator-\<ver\>.jar checkFacadeRESTs.sh
* > chmod 766 checkFacadeRESTs.sh

Location is injected as in this example (use `lombard.risk.rest.spec.path` env var):
* > `./checkFacadeRESTs.sh -s git@git.gft.com:ac/rest-contract.agile-collateral.git -u http://localhost:9000/api`
or
* > './checkFacadeRESTs.sh -s /src/LR/facade-contracts -u http://localhost:9000/api'

Add `-f login` if you want to check only `login` api. Controllers must have proper resource path configred.
See examples in Facade `routes` file.

All interfaces described by found *.yml, *.yaml files will be processed.

# Output
In case of any problem with interface compatibility script will produce exit code = 1. Set `REST_VERIFIER_LOG` variable to indicate where to direct verification report. The default is `./verifier.log`.

# Deploy to Nexus
`mvn deploy:deploy-file -X -DpomFile=../pom.xml -Dfile=swagger-rest-validator-<ver>-jar-with-dependencies.jar -DrepositoryId=lombard-risk-libraries -Durl=http://appolo1.gft.com:8081/repository/lombard-risk-libraries/`
