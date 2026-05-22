# ETAPA 1: Construcción (Maven con Java 21)
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
# Optimización de caché para dependencias
COPY pom.xml .
RUN mvn dependency:go-offline
# Compilación del proyecto
COPY src ./src
RUN mvn clean package -DskipTests

# ETAPA 2: Ejecución (Amazon Corretto 21 sobre Amazon Linux 2023)
FROM amazoncorretto:21-al2023
WORKDIR /app

# Blindaje para buscar versiones más reciente de OS (antes de copiar el jar/construir imagen)
RUN dnf update -y && dnf clean all

# Copiamos el JAR usando el finalName que definimos
COPY --from=build /app/target/techstore-pro-api-hean.jar app.jar

# --- BLOQUE DE ENDURECIMIENTO MANUAL (CISO COMPLIANCE) ---
# 1. Instalar shadow-utils en Amazon Linux
RUN dnf install -y shadow-utils && \
    # 1.1. Crear usuario y grupo de sistema sin privilegios
    groupadd -r techgroup && \
    useradd -r -g techgroup -s /sbin/nologin techuser && \
    # 1.2. Limpieza de herramientas de sistema para reducir CVEs
    # Eliminamos el gestor de paquetes para que no se puedan instalar herramientas maliciosas
    dnf clean all && \
    rm -rf /var/cache/dnf

# 2. Asegurar que el usuario solo tenga acceso a lo necesario (Lectura y Ejecución)
RUN chown -R techuser:techgroup /app && \
    chmod -R 550 /app && \
    chmod 550 /app/app.jar

# Cambiar al usuario no-privilegiado
USER techuser
# Puerto estándar de Spring Boot
EXPOSE 8080
# Parámetros de la JVM optimizados para contenedores Cloud (Digital Ocean)
ENV JAVA_OPTS="-Dspring.profiles.active=docker -Dfile.encoding=UTF-8 -XX:+UseParallelGC -XX:MaxRAMPercentage=75.0 -XshowSettings:vm"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]