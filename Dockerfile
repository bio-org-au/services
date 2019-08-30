FROM openjdk:8u171-alpine3.7
RUN apk --no-cache add curl
COPY build/libs/*.war services.war
VOLUME /etc/nsl
EXPOSE 8080/tcp
#CMD /bin/sh
CMD java ${JAVA_OPTS} -jar services.war
