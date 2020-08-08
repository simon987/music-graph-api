FROM maven:3-openjdk-11 as build
COPY /. /app/
WORKDIR /app/
RUN mvn package
WORKDIR /app/target

FROM openjdk:11-jre-slim

ENV NEO4J_ADDR "localhost:7687"
ENV PG_URL "localhost:5432/musicbrainz_db"
ENV PG_USERNAME "musicbrainz"
ENV PG_PASSWORD "musicbrainz"

COPY --from=build /app/target/music-graph-0.1-jar-with-dependencies.jar /app/
WORKDIR /app/
CMD ["java", "-jar", "/app/music-graph-0.1-jar-with-dependencies.jar", "-Xms300m", "-Xmx300m"]
