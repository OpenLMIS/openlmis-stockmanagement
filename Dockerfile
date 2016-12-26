FROM anapsix/alpine-java:jre8

COPY build/libs/*.jar /service.jar

EXPOSE 8080
CMD java $JAVA_OPTS -jar /service.jar