# Etapa 1: Construcción
FROM maven:3.9-eclipse-temurin-17 AS builder

# Crear carpeta de la app
WORKDIR /app

# Copiar pom.xml y descargar dependencias (para cache eficiente)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar el código fuente y compilar
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Imagen final ligera
FROM eclipse-temurin:17-jre

# Crear carpeta de la app
WORKDIR /app

# Copiar el JAR compilado desde la etapa anterior
COPY --from=builder /app/target/*.jar app.jar

# Exponer el puerto del orquestador (según tu application.properties)
EXPOSE 8082

# Comando de inicio
ENTRYPOINT ["java","-jar","app.jar"]
