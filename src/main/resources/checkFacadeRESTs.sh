#!/usr/bin/env bash

# Usage:
# > checkFacadeRESTs.sh -s <spec YAML uri> -u <facade url> [-f spec name filter]

while [ "$1" != "" ]; do
    case $1 in
            "-s")
                shift
                FULL_PATH=$1
                ;;
            "-u")
                shift
                echo "Option u has value $1"
                FACADE_URL=$1
                ;;
            "-f")
                shift
                echo "Unknown filter $1"
                FILTER=$1
                ;;
            *)
                echo "Unknown error when processing options"; exit 1
                ;;
        esac
        shift
    done

if [[ -z "${FACADE_URL}" ]] || [[ -z "${FULL_PATH}" ]]; then
    echo "Usage:"
    echo "> checkFacadeRESTs.sh -s <spec YAML uri> -u <facade url> -f [spec name filter]"
    exit -1
fi

REPO=$(grep -oE "\\.git$" <<< "${FULL_PATH}")

leng=${#REPO}
if [ $leng -ge 1 ]; then
    GIT_NAME=$(grep -oE "[^//]+$" <<< "${FULL_PATH}")
    DIR_NAME=$(basename "$GIT_NAME")
    DIR_NAME="${DIR_NAME%.*}"

    echo "GIT repo is '${GIT_NAME}'"
    echo "directory name is '${DIR_NAME}'"
    git clone $FULL_PATH
else
    DIR_NAME=$FULL_PATH
fi

mkdir /tmp/yamls
cp -R $DIR_NAME/* /tmp/yamls

if [ -z "$REST_VERIFIER_LOG" ]; then export REST_VERIFIER_LOG=verifier.log; fi

echo "java -Dlr.restwatch.rest.spec.path=/tmp -Dlr.restwatch.url=$FACADE_URL -jar swagger-rest-validator.jar $FILTER"
java -Dlr.restwatch.rest.spec.path=/tmp -Dlr.restwatch.url=$FACADE_URL -jar swagger-rest-validator.jar $FILTER > $REST_VERIFIER_LOG

EXIT_STATUS=$?
echo "Exit status is $EXIT_STATUS"

rm -rf /tmp/yamls

exit $EXIT_STATUS