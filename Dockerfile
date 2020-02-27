FROM openjdk:8u171-alpine3.7
RUN apk --no-cache add curl; addgroup -g 5000 nsl_user; adduser -u 5000 -D nsl_user -G nsl_user
COPY build/libs/*.war services.war
VOLUME /etc/nsl
EXPOSE 8080/tcp
USER nsl_user
RUN  mkdir /home/nsl_user/log
#CMD /bin/sh
CMD java ${JAVA_OPTS} -jar services.war
