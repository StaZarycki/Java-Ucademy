# Stage 1: Build the application
# We use the official Java 26 JDK image and grab Maven from the official maven image
FROM maven:3.9.6-eclipse-temurin-17 AS maven_bin
FROM eclipse-temurin:26-jdk-alpine AS build
WORKDIR /app

# Copy Maven binaries from the maven image to our Java 26 image
COPY --from=maven_bin /usr/share/maven /usr/share/maven
COPY --from=maven_bin /usr/local/bin/mvn-entrypoint.sh /usr/local/bin/mvn-entrypoint.sh
RUN ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

# Copy the pom.xml and source code
COPY pom.xml .
COPY src ./src

# Package the application skipping tests
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:26-jre-alpine
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]