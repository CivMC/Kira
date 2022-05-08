FROM openjdk:17-alpine as build
WORKDIR /app

COPY . .
RUN ./mvnw install

FROM openjdk:17-alpine as run
WORKDIR /app

# TODO: Determine this jar name...
COPY --from=build /app/target/Kira-1.1.2.jar /app/kira.jar

ENTRYPOINT ["java","-jar","/app/kira.jar"]
