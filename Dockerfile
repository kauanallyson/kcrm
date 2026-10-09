# syntax=docker/dockerfile:1

# Build: compila e extrai o jar em camadas, para que dependências (que mudam pouco) fiquem em cache
FROM maven:3.9-eclipse-temurin-26 AS build
WORKDIR /build
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -B -q dependency:go-offline
COPY src src
RUN --mount=type=cache,target=/root/.m2 mvn -B -q package -DskipTests \
    && java -Djarmode=tools -jar target/kcrm-*.jar extract --layers --launcher --destination extracted

# Runtime: só a JRE, sem código-fonte nem Maven
FROM eclipse-temurin:25-jre-alpine
RUN addgroup -S kcrm && adduser -S kcrm -G kcrm
WORKDIR /app
COPY --from=build /build/extracted/dependencies/ ./
COPY --from=build /build/extracted/spring-boot-loader/ ./
COPY --from=build /build/extracted/snapshot-dependencies/ ./
COPY --from=build /build/extracted/application/ ./
USER kcrm

# Heap proporcional ao limite de memória do container, e não à memória do host
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080 8081
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
