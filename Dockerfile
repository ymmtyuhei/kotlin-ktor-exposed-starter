FROM alpine:3.10
FROM openjdk:11.0.4-jre

ARG IMAGE_BUILD_AT
ENV IMAGE_BUILD_AT $IMAGE_BUILD_AT

RUN mkdir /app

COPY ./build/libs/kotlin-ktor-exposed-starter.jar /app/kotlin-ktor-exposed-starter.jar
WORKDIR /app

CMD [\
"java",\
"-server",\
"-XX:+UseContainerSupport",\
"-XX:InitialRAMPercentage=50",\
"-XX:MaxRAMPercentage=50",\
"-XX:MinRAMPercentage=50",\
"-XX:+UseG1GC",\
"-XX:MaxGCPauseMillis=100",\
"-XX:+UseStringDeduplication",\
"-jar",\
"kotlin-ktor-exposed-starter.jar",\
"-config=application-release.conf"\
]%