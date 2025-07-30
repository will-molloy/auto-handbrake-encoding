# base HandBrake + Java image

FROM amazoncorretto:21

# Install HandBrake from Ubuntu packages
RUN apt-get update && \
    apt-get install -y handbrake-cli && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*
