# Container image that runs the code
FROM openjdk:8-jdk-buster

# Copies the main file from the action to the filesystem
COPY entrypoint.sh /entrypoint.sh

# Install git
RUN apt-get update && apt-get upgrade && apt-get install git

# Install bash
RUN apt-get add --no-cache bash

# Execute the action code
RUN chmod +x entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]
