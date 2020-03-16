FROM base_bot_docker AS build

RUN echo "hello"


FROM openjdk:8u212

ARG BOT

COPY --from=build /build/src/${BOT}/target/scala-2.12/*assembly.jar /app/bot.jar

