FROM openjdk:8-jre-alpine
MAINTAINER John Wang<john@dashbase.io>

# Install some essentials and remove cache
RUN   apk update \
  &&   apk add bash wget

# Add the service itself
ARG JAR_FILE
ADD target/${JAR_FILE} prometheus-proxy-framework.jar
ADD config.yml /config.yml
ADD ./docker-entrypoint.sh /docker-entrypoint.sh

ENTRYPOINT ["/docker-entrypoint.sh"]