#!/bin/bash

set +x
sudo rm -f .env

curl -o .env -L https://raw.githubusercontent.com/OpenLMIS/openlmis-ref-distro/master/settings-sample.env

sed -i '' -e "s#spring_profiles_active=.*#spring_profiles_active=#" .env  2>/dev/null || true
sed -i '' -e "s#^BASE_URL=.*#BASE_URL=http://localhost#" .env  2>/dev/null || true
sed -i '' -e "s#^VIRTUAL_HOST=.*#VIRTUAL_HOST=localhost#" .env  2>/dev/null || true

SONAR_LOGIN_TEMP=$(echo $SONAR_LOGIN | cut -f2 -d=)
SONAR_PASSWORD_TEMP=$(echo $SONAR_PASSWORD | cut -f2 -d=)
echo "SONAR_LOGIN=$SONAR_LOGIN_TEMP" >> .env
echo "SONAR_PASSWORD=$SONAR_PASSWORD_TEMP" >> .env
echo "SONAR_BRANCH=$GIT_BRANCH" >> .env

docker-compose -f docker-compose.builder.yml run sonar
docker-compose -f docker-compose.builder.yml down --volumes

sudo rm -vrf .envrf .env