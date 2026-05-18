FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-21

ENV TZ="Europe/Oslo"
ENV JAVA_OPTS='-XX:MaxRAMPercentage=90'

WORKDIR /app

COPY spoogle-backend/build/install/app/ /app/

ENTRYPOINT ["java", "-cp", "/app/lib/*", "no.nav.helse.spoogle.AppKt"]
CMD []
