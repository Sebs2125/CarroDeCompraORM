# ─────────────────────────────────────────────
# STAGE 1 — BUILD
# Compila el fat JAR con Gradle
# ─────────────────────────────────────────────
FROM gradle:8.5-jdk21 AS build

WORKDIR /app

# Copiar archivos de configuración primero (mejor cache de capas Docker)
COPY build.gradle settings.gradle ./

# Descargar dependencias (se cachea si build.gradle no cambia)
RUN gradle dependencies --no-daemon || true

# Copiar el código fuente completo
COPY src ./src

# Compilar y generar el fat JAR
# El plugin 'jar' con zipTree ya está configurado en tu build.gradle
RUN gradle jar --no-daemon -x test

# ─────────────────────────────────────────────
# STAGE 2 — RUNTIME
# Imagen mínima solo con JRE para ejecutar
# ─────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# Usuario no-root por seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copiar el fat JAR generado en el stage anterior
# El nombre viene de: group-version en build.gradle → CarroDeCompraORM-1.0-SNAPSHOT.jar
COPY --from=build /app/build/libs/CarroDeCompraORM-1.0-SNAPSHOT.jar app.jar

# Ajustar permisos
RUN chown appuser:appgroup app.jar

USER appuser

# Puerto donde corre Javalin (definido en Main.java → .start(8080))
EXPOSE 8080

# Variables de entorno — se sobreescriben en docker-compose.yml
ENV PORT=8080
ENV DB_URL=jdbc:h2:tcp://h2-server:9092/~/carritodb
ENV DB_USER=sa
ENV DB_PASS=sa

# Arrancar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]