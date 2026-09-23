FROM gradle:8.10.2-jdk21 AS build

WORKDIR /workspace
COPY . .
RUN gradle --no-daemon :apps:api:bootJar

FROM eclipse-temurin:21-jre

WORKDIR /app
RUN useradd --system --uid 10001 --no-create-home appuser
COPY --from=build /workspace/apps/api/build/libs/*.jar /app/api.jar
RUN chown -R appuser:appuser /app
USER 10001

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -Djava.security.egd=file:/dev/urandom"
ENV DEV_SEED_ENABLED=false

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/api.jar"]
