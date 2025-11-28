# Stage 1: Build and Test
# Use a Maven image to compile and run tests
FROM maven:3.9-eclipse-temurin-17 AS builder

# Set working directory
WORKDIR /app

# Copy project files
COPY pom.xml .
COPY src ./src

# Build the application (runs 'clean', 'test', and 'package')
# If tests fail, the build will stop here.
RUN mvn clean package -DskipTests=false

# Stage 2: Runtime
# Use a lightweight Java Runtime Environment (JRE) forw
# We assume the maven-shade-plugin or simple classpath approach. 
# Since we used 'exec:java', we just need the classes and resources.
# However, copying the 'target' folder is easiest for a simple execution.
COPY --from=builder /app/target/classes /app/classes
COPY --from=builder /app/src/main/resources/sales_data.csv /app/classes/sales_data.csv

# Set the entrypoint to run the Main class
ENTRYPOINT ["java", "-cp", "/app/classes", "com.assignment2.Main"]

