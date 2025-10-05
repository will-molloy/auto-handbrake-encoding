# base HandBrake + Java image
FROM eclipse-temurin:25-jre-alpine

# Install HandBrake from Alpine packages
RUN apk add --no-cache handbrake
