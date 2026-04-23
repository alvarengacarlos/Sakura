# syntax=docker/dockerfile:1

FROM amazoncorretto:25 AS build

WORKDIR /tmp/sakura

COPY . .

RUN dnf install -y tar gzip

RUN ./mvnw clean install

FROM amazoncorretto:25 AS databasemigration

ARG JDBC_URL

ENV AA=""

WORKDIR /opt/sakura

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
COPY databasemigration databasemigration

RUN dnf install -y tar gzip

WORKDIR /opt/sakura/databasemigration

RUN sed -i "s|^url=.*|url=${JDBC_URL}|" src/main/resources/liquibase.properties
RUN --mount=type=secret,id=db_username_secret \
    sed -i "s|^username=.*|username=$(cat /run/secrets/db_username_secret)|" src/main/resources/liquibase.properties
RUN --mount=type=secret,id=db_password_secret \
    sed -i "s|^password=.*|password=$(cat /run/secrets/db_password_secret)|" src/main/resources/liquibase.properties

RUN ./mvnw clean compile

FROM amazoncorretto:25 AS gatewayapi

WORKDIR /opt/gatewayapi

COPY --from=build /tmp/sakura/gatewayapi/target/gatewayapi-0.0.1.jar gatewayapi.jar

CMD [ "java", "-jar", "gatewayapi.jar"]

FROM amazoncorretto:25 AS imageanalyzer

WORKDIR /opt/imageanalyzer

COPY --from=build /tmp/sakura/imageanalyzer/target/imageanalyzer-0.0.1.jar imageanalyzer.jar

CMD [ "java", "-jar", "imageanalyzer.jar"]

FROM amazoncorretto:25 AS dataanalyzer

WORKDIR /opt/dataanalyzer

COPY --from=build /tmp/sakura/dataanalyzer/target/dataanalyzer-0.0.1.jar dataanalyzer.jar

CMD [ "java", "-jar", "dataanalyzer.jar"]

FROM amazoncorretto:25 AS notifier

WORKDIR /opt/notifier

COPY --from=build /tmp/sakura/notifier/target/notifier-0.0.1.jar notifier.jar

CMD [ "java", "-jar", "notifier.jar"]
