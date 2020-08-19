FROM maven:3.6.3-adoptopenjdk-11 AS builder

WORKDIR /app
COPY . /app/
RUN mvn clean install

FROM adoptopenjdk:11-jre-hotspot

ARG CRYPTIC_USER=nova
ARG CRYPTIC_GROUP=nova

ENV CRYPTIC_HOME /opt/cryptic
ENV DATA_DIR /data

RUN set -o errexit -o nounset \
    && groupadd --system --gid 1000 ${CRYPTIC_GROUP} \
    && useradd --system --gid ${CRYPTIC_GROUP} --uid 1000 --shell /bin/bash --create-home ${CRYPTIC_USER} \
    && mkdir -p ${DATA_DIR} \
    && chown --recursive ${CRYPTIC_USER}:${CRYPTIC_GROUP} ${DATA_DIR} \
    && chown --recursive ${CRYPTIC_USER}:${CRYPTIC_GROUP} /home/${CRYPTIC_USER}

WORKDIR ${DATA_DIR}
COPY --from=builder --chown=${CRYPTIC_USER}:${CRYPTIC_GROUP} /app/target/server-0.1.0-jar-with-dependencies.jar ${CRYPTIC_HOME}/server.jar

USER ${CRYPTIC_USER}

ENTRYPOINT ["java", "-jar", "/opt/cryptic/server.jar"]
