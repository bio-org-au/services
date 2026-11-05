# Tomcat 9 on JDK 17 (AWS Corretto) running the container war.
FROM tomcat:9.0-jdk17-corretto

# The war is deployed as nsl#services.war, which Tomcat maps to context /nsl/services
COPY build/libs/nsl#services-tomcat.war /usr/local/tomcat/webapps/nsl#services.war

ENV JAVA_OPTS="-Xms1G -Xmx2G -XX:MaxMetaspaceSize=512m -Dfile.encoding=UTF-8"

EXPOSE 8080
EXPOSE 8443

# The corretto image is Amazon Linux based: no useradd/groupadd, so write the
# passwd entry by hand. The app resolves its config via ~/.nsl, which needs a real
# user.home - with no entry for uid 5000, HOME collapses to "/" and the config is
# never found.
# logback.groovy also writes nsl.log to the working directory (CATALINA_HOME), so
# the whole Tomcat tree has to be writable by the non-root user.
RUN printf 'nsl_user:x:5000:5000::/home/nsl_user:/sbin/nologin\n' >> /etc/passwd \
 && printf 'nsl_user:x:5000:\n' >> /etc/group \
 && mkdir -p /home/nsl_user/log /home/nsl_user/.nsl /usr/local/tomcat/conf/Catalina/localhost \
 && chown -R 5000:5000 /usr/local/tomcat /home/nsl_user

# Config lives in ~/.nsl (grails.config.locations in application.yml). Declaring it
# as VOLUME would mask the directory with an anonymous volume, so just document it:
#   docker run -v $HOME/.nsl:/home/nsl_user/.nsl:ro ...
# /etc/nsl is also mounted for the supporting services' config files.
RUN mkdir -p /etc/nsl && chown 5000:5000 /etc/nsl

USER 5000:5000
WORKDIR /usr/local/tomcat
