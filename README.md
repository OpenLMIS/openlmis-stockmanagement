# OpenLMIS Service Template
This template is meant to be a starting point for developing a new 
OpenLMIS 3.x Independent Service.

## Prerequisites
* Docker 1.11+
* Docker Compose 1.6+

All other dependencies, such as Java, are delivered automatically via the Docker image. It is unnecessary to install them locally to run the service, though often helpful to do so for the sake of development. See the _Tech_ section of [openlmis/dev](https://hub.docker.com/r/openlmis/dev/) for a list of these optional dependencies.

## Quick Start
1. Fork/clone this repository from GitHub.

 ```shell
 git clone https://github.com/OpenLMIS/openlmis-template-service.git <openlmis-your-service-name>
 ```
2. Respectively change all instances of `openlmis-template-service` and
`template-service` within the project to `openlmis-your-service-name` and
`your-service-name`.
3. Change all instances of the default version number ("0.0.1") in the project to your
version number.
4. Change the gradle build file to add any dependencies 
(e.g. JPA, PostgreSQL).
5. Add Java code to the template.
6. Add an environment file called `.env` to the root folder of the project, with the required 
project settings and credentials. For a starter environment file, you can use [this 
one](https://github.com/OpenLMIS/openlmis-config/blob/master/.env). e.g.

 ```shell
 cd <openlmis-your-service-name>
 curl -LO https://raw.githubusercontent.com/OpenLMIS/openlmis-config/master/.env
 ```
7. Develop w/ Docker by running `docker-compose run --service-ports <your-service-name>`.
See [Developing w/ Docker](#devdocker).
8. You should now be in an interactive shell inside the newly created development 
environment, start the Service with: `gradle bootRun`
9. Go to `http://<yourDockerIPAddress>:8080/` to see the service name 
and version. Note that you can determine yourDockerIPAddress by running `docker-machine ip`.
10. Go to `http://<yourDockerIPAddress>:8080/api/` to see the APIs.

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
> docker-compose run --service-ports <your-service-name>
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
> docker-compose -f docker-compose.builder.yml run --service-ports template-service
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
