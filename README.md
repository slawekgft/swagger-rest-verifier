
# Description
This project checks if YAML specs in the Facade project are coherent with what given Facade instance presents by theirs
REST services.
It simply checks if instance REST service is backward compatible with YAML files at provided location.

If you build from sources:
> mvn clean package
> rm restwatcher-1.0-SNAPSHOT.jar
> mv restwatcher-1.0-jar-with-dependencies.jar restwatcher-1.0.jar
> jar xf restwatcher-1.0-SNAPSHOT.jar checkFacadeRESTs.sh
> chmod 766 checkFacadeRESTs.sh

Location is injected as in this example (use `lombard.risk.rest.spec.path` env var):
> `./checkFacadeRESTs.sh -s git@git.gft.com:lr/facade-contracts.git -u http://localhost:9000/api`
or
> './checkFacadeRESTs.sh -s ~/IdeaProjects/LR/f-c/ -u http://localhost:9000/api'

Add `-f login` if you want to check only `login` api. All files must have proper resource path configred!
See examples in Facade routes file.

All interfaces described by found *.yml, *.yaml files will be processed.

