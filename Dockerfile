FROM ghcr.io/navikt/baseimages/temurin:21

ENV JAVA_OPTS='-XX:MaxRAMPercentage=90'

COPY spoogle-backend/build/libs/*.jar ./
