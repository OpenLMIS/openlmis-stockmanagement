FROM openlmis/service-base:3

COPY build/libs/*.jar /service.jar
COPY src/main/resources/db/demo-data/*.csv /demo-data/
COPY build/schema /schema
COPY build/consul /consul
