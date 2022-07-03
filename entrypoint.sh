#!/bin/bash

# testing archive directory is non-empty
# ensures volume is mounted correctly
# hacky but it works (for my use case)
if [ -z "$(ls -A /archive)" ]; then
  echo "/archive directory empty, network drive not mounted?"
  exit 1
fi


./gradlew :nvidia-shadowplay:run -PinputDirectory="/input" -PoutputDirectory="/output" -ParchiveDirectory="/archive"
