FROM navikt/java:17

ENV JAVA_OPTS='-XX:MaxRAMPercentage=90'

COPY spoogle-backend/build/libs/*.jar ./
