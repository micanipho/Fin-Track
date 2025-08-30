FROM openjdk:21-slim
LABEL authors="Nhlakanipho Masilela"

COPY target/*jar app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]