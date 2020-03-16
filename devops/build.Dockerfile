ARG OPENJDK_TAG=8u212
FROM openjdk:${OPENJDK_TAG} AS build

ARG SBT_VERSION=1.2.8

LABEL MAINTAINER="Gonzalo R Canosa"
LABEL MAINTAINER_EMAIL="grcanosa.com"

WORKDIR /work
# Install sbt
ADD devops/software/sbt-${SBT_VERSION}.deb /work

RUN dpkg -i sbt-${SBT_VERSION}.deb && \
        rm -r sbt-${SBT_VERSION}.deb && \
        sbt sbtVersion

#Install git
#RUN apt-get install git

#COMPILE PROJECT
WORKDIR /build
COPY *.sbt /build/
COPY project/*.sbt /build/project/
COPY project/*.scala /build/project/
RUN sbt update

ADD src /build/src/
RUN sbt compile assembly

FROM openjdk:${OPENJDK_TAG}

COPY --from=build /build/src/bot_apps/target/scala-2.12/*assembly.jar /app/bot.jar
