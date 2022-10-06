#!/bin/bash

# testing archive directory is non-empty
# ensures volume is mounted correctly
# hacky but it works (for my use case)
if [ -z "$(ls -A /archive)" ]; then
  echo "/archive directory empty, network drive not mounted?"
  exit 1
fi

if ! ./gradlew :nvidia-shadowplay:run -PinputDirectory="/input" -PoutputDirectory="/output" -ParchiveDirectory="/archive"; then
  exit 1
fi

exit 0
