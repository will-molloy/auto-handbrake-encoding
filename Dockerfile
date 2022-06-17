# Use HandBrake base image (Alpine Linux)
# Installing it otherwise (i.e. on Java base image) is a pain
FROM jlesage/handbrake:latest

# Update packages
RUN apk update && \
    apk add --upgrade apk-tools && \
    apk upgrade --available && \
    sync

# Install OpenJDK 17
RUN apk --no-cache add openjdk17 --repository=https://dl-cdn.alpinelinux.org/alpine/latest-stable/community

COPY . /app
WORKDIR /app

RUN ./gradlew build

VOLUME /input
VOLUME /output
VOLUME /archive

# TODO remove sleep
ENTRYPOINT ./gradlew :nvidia-shadowplay:run -PinputDirectory="/input" -PoutputDirectory="/output" -ParchiveDirectory="/archive" && sleep 99999
