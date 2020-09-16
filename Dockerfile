# Container image that runs the code
FROM alpine:3.12

# Copies the main file from the action to the filesystem
COPY entrypoint.sh /entrypoint.sh

# Install git
RUN apk update && apk upgrade && apk add --no-cache git

# Install bash
RUN apk add --no-cache bash

# Execute the action code
RUN chmod +x entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]
