# Stage 1: Build
# Compiles the project, runs generate-sources for protobuf, and packages the fat JAR.
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /build

# Copy dependency manifest first to exploit layer caching.
# Dependencies are re-downloaded only when pom.xml changes.
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B 2>/dev/null || true

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests -B

# Explode the JAR into layers for optimised Docker layer caching on subsequent builds.
RUN java -Djarmode=layertools -jar target/enterprise-greeting-delivery-system-*.jar extract --destination target/extracted

# Stage 2: Runtime
# Minimal JRE image. Does not include JDK, Maven, or build artifacts.
FROM eclipse-temurin:17-jre-alpine AS runtime

# Non-root service account. Running as root in a container violates least-privilege.
RUN addgroup -S egds && adduser -S egds -G egds
USER egds

WORKDIR /app

# Copy exploded layers in dependency-stability order (least volatile first).
COPY --from=builder /build/target/extracted/dependencies/ ./
COPY --from=builder /build/target/extracted/spring-boot-loader/ ./
COPY --from=builder /build/target/extracted/snapshot-dependencies/ ./
COPY --from=builder /build/target/extracted/application/ ./

# HTTP REST port
EXPOSE 8080
# gRPC (Protobuf binary) port
EXPOSE 9090

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
