# Use HandBrake base image (Alpine Linux)
# Installing it otherwise (i.e. on Java base image) is a pain
FROM jlesage/handbrake:latest

# Update packages
RUN apk update \
    && apk add --upgrade apk-tools \
    && apk upgrade --available \
    && sync

# Install OpenJDK 17
RUN apk --no-cache add openjdk17 --repository=https://dl-cdn.alpinelinux.org/alpine/latest-stable/community

COPY . /app
WORKDIR /app

RUN ./gradlew clean build

VOLUME /input
VOLUME /output
VOLUME /archive

ENTRYPOINT ./entrypoint.sh
