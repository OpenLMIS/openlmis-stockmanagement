# OpenLMIS Stock Management Service
This service allows users to create/update stock cards and stock movements.

## Prerequisites
* Java 1.8+
* Docker 1.11+
* Docker Compose 1.6+

All other dependencies, such as Java, are delivered automatically via the Docker image. It is unnecessary to install them locally to run the service, though often helpful to do so for the sake of development. See the _Tech_ section of [openlmis/dev](https://hub.docker.com/r/openlmis/dev/) for a list of these optional dependencies.

## Quick Start
1. Fork/clone this repository from GitHub.
 ```shell
 git clone https://github.com/OpenLMIS/openlmis-stockmanagement.git
 ```
2. Add an environment file called `.env` to the root folder of the project, with the required 
project settings and credentials. For a starter environment file, you can use [this 
one](https://raw.githubusercontent.com/OpenLMIS/openlmis-ref-distro/master/settings-sample.env). e.g.
 ```shell
 curl -o .env -L https://raw.githubusercontent.com/OpenLMIS/openlmis-ref-distro/master/settings-sample.env
 ```
3. Develop w/ Docker by running `docker-compose run --service-ports stockmanagement`.
See [Developing w/ Docker](#devdocker).
4. You should now be in an interactive shell inside the newly created development 
environment, start the Service with: `gradle bootRun`
5. Go to `http://<yourDockerIPAddress>:8080/` to see the service name 
and version. Note that you can determine yourDockerIPAddress by running `docker-machine ip`.
6. Go to `http://<yourDockerIPAddress>:8080/stockmanagement/docs` to see the APIs.

## <a name="building">Building & Testing</a>
Gradle is our usual build tool.  This template includes common tasks 
that most Services will find useful:

- `clean` to remove build artifacts
- `build` to build all source. `build`, after building sources, also runs unit tests. Build will be successful only if all tests pass.
- `generateMigration -PmigrationName=<yourMigrationName>` to create a
"blank" database migration file. The file
will be generated under `src/main/resources/db/migration`. Put your
migration SQL into it.
- `test` to run unit tests
- `integrationTest` to run integration tests
- `sonarqube` to execute the SonarQube analysis.

The **test results** are shown in the console.

While Gradle is our usual build tool, OpenLMIS v3+ is a collection of 
Independent Services where each Gradle build produces 1 Service. 
To help work with these Services, we use Docker to develop, build and 
publish these.

See [Developing with Docker](#devdocker). 

## <a name="devdocker">Developing with Docker</a>
OpenLMIS utilizes Docker to help with development, building, publishing
and deployment of OpenLMIS Services. This helps keep development to 
deployment environments clean, consistent and reproducible and 
therefore using Docker is recommended for all OpenLMIS projects.

To enable development in Docker, OpenLMIS publishes a couple Docker 
Images:

- [openlmis/dev](https://hub.docker.com/r/openlmis/dev/) - for Service 
development.  Includes the JDK & Gradle plus common build tools.
- [openlmis/postgres](https://hub.docker.com/r/openlmis/postgres/) - for 
quickly standing up a shared PostgreSQL DB

In addition to these Images, each Service includes Docker Compose 
instructions to:

- standup a development environment (run Gradle)
- build a lean image of itself suitable for deployment
- publish its deployment image to a Docker Repository

### <a name="devenv">Development Environment</a>
Launches into shell with Gradle & JDK available suitable for building 
Service.  PostgreSQL connected suitable for testing. If you run the 
Service, it should be available on port 8080.

Before starting the development environment, make sure you have a `.env` file as outlined in the 
Quick Start instructions.

```shell
> docker-compose run --service-ports stockmanagement
$ gradle clean build
$ gradle bootRun
```

### <a name="buildimage">Build Deployment Image</a>
The specialized docker-compose.builder.yml is geared toward CI and build 
servers for automated building, testing and docker image generation of 
the service.

Before building the deployment image, make sure you have a `.env` file as outlined in the Quick
Start instructions.

```shell
> docker-compose -f docker-compose.builder.yml run builder
> docker-compose -f docker-compose.builder.yml build image
```

### Publish to Docker Repository
TODO

### <a name="dockerfiles">Docker's file details</a>
A brief overview of the purpose behind each docker related file

- `Dockerfile`:  build a deployment ready image of this service 
suitable for publishing.
- `docker-compose.yml`:  base docker-compose file.  Defines the 
basic composition from the perspective of working on this singular 
vertical service.  These aren't expected to be used in the 
composition of the Reference Distribution.
- `docker-compose.override.yml`:  extends the `docker-compose.yml`
base definition to provide for the normal usage of docker-compose
inside of a single Service:  building a development environment.
Wires this Service together with a DB for testing, a gradle cache
volume and maps tomcat's port directly to the host. More on how this
file works: https://docs.docker.com/compose/extends/
- `docker-compose.builder.yml`:  an alternative docker-compose file
suitable for CI type of environments to test & build this Service
and generate a publishable/deployment ready Image of the service.
- `docker-compose.prod.yml`:  Docker-compose file suitable for production.
Contains nginx-proxy image and virtual host configuration of each service.

### <a name="nginx">Running complete application with nginx proxy</a>
1. Enter desired `VIRTUAL_HOST` for each service in the `docker-compose.prod.yml` file.
2. Start up containers
```shell
> docker-compose -f docker-compose.yml -f docker-compose.prod.yml up
```
3. The application should be available at port 80.

### <a name="logging">Logging</a>
Logging is implemented using SLF4J in the code, Logback in Spring Boot, and routed to an 
external Syslog server. There is a default configuration XML (logback.xml) in the resources 
folder. To configure the log level for the development environment, simply modify the logback.xml
to suit your needs.

Configuring log level for a production environment is a bit more complex, as the code has already
been packaged into a Spring Boot jar file. However, the default log configuration XML can be 
overridden by setting the Spring Boot logging.config property to an external logback.xml when the
jar is executed. The container needs to be run with a JAVA_OPTS environment variable set to a 
logback.xml location, and with a volume with the logback.xml mounted to that location. Some docker 
compose instructions have been provided to demonstrate this.

1. Build the deployment image. (See [Build Deployment Image](#buildimage))
2. Get a logback.xml file and modify it to suit your log level configuration.
3. Modify `docker-compose.builder.yml` to point to your logback.xml location.
  a. Under `volumes`, where it shows two logback.xml locations separated by a colon, change the 
  location before the colon.
4. Run the command below.

```shell
> docker-compose -f docker-compose.builder.yml run --service-ports stockmanagement
```

### <a name="internationalization">Internationalization (i18n)</a>
Internationalization is implemented by the definition of two beans found in the Application 
class, localeResolver and messageSource. (Alternatively, they could be defined in an application 
context XML file.) The localeResolver determines the locale, using a cookie named `lang` in the 
request, with `en` (for English) as the default. The messageSource determines where to find the 
message files.

Note there is a custom message source interface, ExposedMessageSource, with a corresponding class
ExposedMessageSourceImpl. These provide a method to get all the messages in a locale-specific 
message file.

See the MessageController class for examples on how to get messages.

Additionally, [Transifex](https://www.transifex.com/) has been integrated into the development and 
build process. In order to sync with the project's resources in Transifex, you must provide 
values for the following keys: `TRANSIFEX_USER`, `TRANSIFEX_PASSWORD`.

For the development environment in Docker, you can sync with Transifex by running the
`sync_transifex.sh` script. This will upload your source messages file to the Transifex project 
and download translated messages files.

The build process has syncing with Transifex seamlessly built-in.

### <a name="debugging">Debugging</a>
To debug the Spring Boot application, use the `--debug-jvm` option.

```shell
$ gradle bootRun --debug-jvm
```

This will enable debugging for the application, listening on port 5005, which the container has 
exposed. Note that the process starts suspended, so the application will not start up until the 
debugger has connected.

## Environment variables

Environment variables common to all services are listed here: https://github.com/OpenLMIS/openlmis-template-service/blob/master/README.md#environment-variables

## Steps to create test data for performance testing

1. Log on to the server(ssh)

2. Find container id of posgres: `docker ps`

3. Go to DB
   * `docker exec -it [postgres container id] bash`
   * `psql -U postgres`
   * `\connect open_lmis`
4. Run the following sql
( You can change the number (1..100) to choose how many orderables to create )
```sql
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
DO
$do$
BEGIN
FOR i IN 1..100 LOOP 
 INSERT INTO referencedata.orderables(id, dispensingunit, fullproductname, packroundingthreshold, netcontent, code, roundtozero) VALUES
 (gen_random_uuid(), '10 tab strip', 'test' || i, 0, 1, 'c120' || i, 'false');
 INSERT INTO referencedata.program_orderables(id, active, displayorder, dosesperpatient, fullsupply, priceperpack, orderabledisplaycategoryid, orderableid, programid) VALUES
 (gen_random_uuid(), 'true', 0, 1, 'true', 5.20, '15b8ef1f-a5d6-42dd-95bf-bb68a4504e82', (select id from referencedata.orderables where fullproductname = 'test' || i ), 'dce17f2e-af3e-40ad-8e00-3496adef44c3');
 INSERT INTO referencedata.facility_type_approved_products(id, emergencyorderpoint, maxperiodsofstock, minperiodsofstock, facilitytypeid, orderableid, programid) VALUES
 (gen_random_uuid(), 1, 3, 1.5, 'ac1d268b-ce10-455f-bf87-9c667da8f060', (select id from referencedata.orderables where fullproductname = 'test' || i), 'dce17f2e-af3e-40ad-8e00-3496adef44c3');
END LOOP;
END
$do$;
```

After execution of this sql, there should be 100 new orderables approved for program: "Famility planning" and facility type: "Health Center".

5. Find 'create_stock_cards_and_stock_card_line_items.js' in 'perf_test' directory of this repository.

Run `npm install request --save` in the directory that contains the js file.

Then use nodejs to run the js file.
`node create_stock_cards_and_stock_card_line_items.js [url, for example: https://test.openlmis.org]`

After execution of this js file, user: "srmanager1" will have access to 126 stock cards.

6. Copy the following sql to the DB and run
 * Make sure the number of `j` equals to the number of the orderables.
 * You can change the number of `i` to choose how many stock card line items created for each stock card.
```sql
DO
$do$
BEGIN
FOR j IN 1..100 LOOP
FOR i IN 1..99 LOOP
INSERT INTO stockmanagement.stock_card_line_items(id, documentnumber, occurreddate, processeddate, quantity, userid, origineventid, stockcardid)
VALUES(gen_random_uuid(), 'Testit' || i, '2017-05-17 06:39:50.717','2017-05-17 06:40:14.155', 10, 'c994d1ea-47f7-435d-9d4d-42fb54197698', (select distinct origineventid from stockmanagement.stock_cards where facilityid='176c4276-1fb1-4507-8ad2-cdfba0f47445'), (select id from stockmanagement.stock_cards where orderableid in (select id from referencedata.orderables where fullproductname = 'test' || j)));
END LOOP;
END LOOP;
END
$do$;
```

After execution of this sql, there will be 100 stock card line items for each stock card.

7. Now you can go to the web pages, log in as "srmanager1" and conduct performance tests.

## Production by Spring Profile

By default when this service is started, it will clean its schema in the database before migrating
it. This is meant for use during the normal development cycle. For production data, this obviously
is not desired as it would remove all of the production data. To change the default clean & migrate
behavior to just be a migrate behavior (which is still desired for production use), we use a Spring
Profile named `production`. To use this profile, it must be marked as Active. The easiest way to
do so is to add to the .env file:

```java
spring_profiles_active=production
```

This will set the similarly named environment variable and limit the profile in use.  The
expected use-case for this is when this service is deployed through the
[Reference Distribution](https://github.com/openlmis/openlmis-ref-distro).
