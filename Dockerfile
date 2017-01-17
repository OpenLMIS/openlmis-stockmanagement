FROM anapsix/alpine-java:jre8

COPY build/libs/*.jar /service.jar
COPY build/demo-data /demo-data
COPY build/consul /consul
COPY run.sh /run.sh

RUN chmod +x run.sh \
  && apk update \
  && apk add nodejs \
  && mv consul/package.json package.json \
  && npm install

EXPOSE 8080
CMD ./run.sh
