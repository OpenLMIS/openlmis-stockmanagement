#!/bin/bash

set +x
set -e

# prepare ERD folder on CI server

sudo mkdir -p /var/www/html/erd-stockmanagement
sudo chown -R $USER:$USER /var/www/html/erd-stockmanagement

# General steps:
# - Copy env file and remove demo data profiles (errors happen during startup when they are enabled)
# - Copy ERD generation docker-compose file and bring up service with db container and wait
# - Clean out existing ERD folder
# - Create output folder (SchemaSpy uses it to hold ERD files) and make sure it is writable by docker
# - Use SchemaSpy docker image to generate ERD files and send to output, wait
# - Bring down service and db container
# - Make sure output folder and its subfolders is owned by user (docker generated files/folders are owned by docker)
# - Move output to web folder
# - Clean out old zip file and re-generate it
# - Clean up files and folders
wget https://raw.githubusercontent.com/OpenLMIS/openlmis-ref-distro/master/settings-sample.env -O .env \
&& sed -i -e "s/^spring_profiles_active=demo-data,refresh-db/spring_profiles_active=/" .env \
&& wget https://raw.githubusercontent.com/OpenLMIS/openlmis-stockmanagement/master/docker-compose.erd-generation.yml -O docker-compose.yml \
&& (/usr/local/bin/docker-compose up &) \
&& sleep 90 \
&& sudo rm /var/www/html/erd-stockmanagement/* -rf \
&& sudo rm -rf output \
&& mkdir output \
&& chmod 777 output \
&& (docker run --rm --network ${COMPOSE_PROJECT_NAME//.}_default -v $WORKSPACE/erd/output:/output schemaspy/schemaspy:snapshot -t pgsql -host db -port 5432 -db open_lmis -s stockmanagement -u postgres -p p@ssw0rd -I "(data_loaded)|(schema_version)|(jv_.*)" -norows -hq &) \
&& sleep 30 \
&& /usr/local/bin/docker-compose down --volumes \
&& sudo chown -R $USER:$USER output \
&& mv output/* /var/www/html/erd-stockmanagement \
&& rm erd-stockmanagement.zip -f \
&& pushd /var/www/html/erd-stockmanagement \
&& zip -r $WORKSPACE/erd/erd-stockmanagement.zip . \
&& popd \
&& rmdir output \
&& rm .env \
&& rm docker-compose.yml
