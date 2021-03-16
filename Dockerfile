ARG JAVA_VERSION=11
ARG JVM_IMPL=hotspot

FROM maven:3-adoptopenjdk-${JAVA_VERSION} AS builder

WORKDIR /app
COPY . /app/
RUN mvn clean install

FROM adoptopenjdk:${JAVA_VERSION}-jre-${JVM_IMPL}

ENV CRYPTIC_HOME /opt/cryptic
ENV DATA_DIR /data

RUN set -o errexit -o nounset \
    && mkdir -p ${DATA_DIR} \

WORKDIR ${DATA_DIR}
COPY --from=builder /app/target/server-0.1.0-jar-with-dependencies.jar ${CRYPTIC_HOME}/server.jar

EXPOSE 1239
EXPOSE 8080
EXPOSE 80

ENTRYPOINT ["java", "-jar", "/opt/cryptic/server.jar"]
