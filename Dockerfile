FROM openlmis/service-base:1

COPY build/libs/*.jar /service.jar
COPY demo-data /demo-data
COPY build/schema /schema
COPY build/consul /consul
