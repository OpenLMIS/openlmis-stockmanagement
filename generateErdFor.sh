#!/bin/bash

docker pull openlmis/$1
#the image might be built on the jenkins slave, so we need to pull here to make sure it's using the latest

sudo mkdir -p /var/www/html/erd-$1
sudo chown -R $USER:$USER /var/www/html/erd-$1

wget http://nchc.dl.sourceforge.net/project/schemaspy/schemaspy/SchemaSpy%205.0.0/schemaSpy_5.0.0.jar -O schemaSpy_5.0.0.jar
wget http://central.maven.org/maven2/org/postgresql/postgresql/9.4-1201-jdbc41/postgresql-9.4-1201-jdbc41.jar -O postgresql-9.4-1201.jdbc41.jar

wget https://raw.githubusercontent.com/OpenLMIS/openlmis-config/master/.env -O .env \
&& wget https://raw.githubusercontent.com/OpenLMIS/openlmis-$1/master/docker-compose.erd-generation.yml -O docker-compose.erd-generation.yml \
&& (/usr/local/bin/docker-compose -f docker-compose.erd-generation.yml up &) \
&& sleep 90 \
&& sudo rm /var/www/html/erd-$1/* -rf \
&& (sudo java -jar schemaSpy_5.0.0.jar -t pgsql -host localhost:$2 -db open_lmis -u postgres -p $DBPASSWORD -dp postgresql-9.4-1201.jdbc41.jar -all -schemaSpec '^(?!pg_catalog$|information_schema$).*' -hq -o /var/www/html/erd-requisition &) \
&& sleep 30 \
&& /usr/local/bin/docker-compose -f docker-compose.erd-generation.yml down --volumes \
&& rm erd-$1.zip -f \
&& pushd /var/www/html/erd-$1 \
&& zip -r $WORKSPACE/erd-$1.zip * \
&& popd \
&& rm .env \
&& rm docker-compose.erd-generation.yml