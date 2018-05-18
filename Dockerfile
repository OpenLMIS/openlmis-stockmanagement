FROM openlmis/service-base:1

COPY build/libs/*.jar /service.jar
COPY build/consul /consul
