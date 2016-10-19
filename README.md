
# Description
This project checks if YAML specs in the Facade project are coherent with what given Facade instance presents by theirs
REST services.
It simply checks if instance REST service is backward compatible with YAML files at provided location.
Location is injected as in this example (use `lombard.risk.rest.spec.path` env var):
> `java -cp ... com.gft.lr.CheckRestSpecApplication -Dlombard.risk.rest.spec.path=/src/LR/facade/lr-facade`

Currently Facade instance should be available at `http://localhost:9000/`.

All interfaces described by found *.yml files will be processed. By default *.yml files are located in `/public/interfacespec`.
