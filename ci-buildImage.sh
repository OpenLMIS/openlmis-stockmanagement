#!/bin/bash

set +x
set -e

function finish {
  docker-compose -f docker-compose.builder.yml down --volumes
}
trap finish EXIT

sudo rm -f .env
mkdir .signing/
cp $SECRING_FILE .signing/secring.gpg
cp $ENV_FILE .env
if [ "$GIT_BRANCH" != "master" ]; then
    sed -i '' -e "s#^TRANSIFEX_PUSH=.*#TRANSIFEX_PUSH=false#" .env  2>/dev/null || true
fi

docker-compose -f docker-compose.builder.yml run -e BUILD_NUMBER=$BUILD_NUMBER -e GIT_BRANCH=$GIT_BRANCH \
-e SIGNING_KEYID=$SIGNING_KEYID -e SIGNING_PASSWORD=$SIGNING_PASSWORD -e OSSRH_USERNAME=$OSSRH_USERNAME -e OSSRH_PASSWORD=$OSSRH_PASSWORD builder
docker-compose -f docker-compose.builder.yml build image
docker tag openlmis/stockmanagement:latest openlmis/stockmanagement:$STAGING_VERSION
docker push openlmis/stockmanagement:$STAGING_VERSION