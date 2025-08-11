# base HandBrake + Java image
FROM eclipse-temurin:21.0.8_9-jre-alpine

# Install HandBrake from Alpine packages
RUN apk add --no-cache handbrake
