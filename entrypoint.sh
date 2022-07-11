#!/bin/bash

# testing archive directory is non-empty
# ensures volume is mounted correctly
# hacky but it works (for my use case)
if [ -z "$(ls -A /archive)" ]; then
  echo "/archive directory empty, network drive not mounted?"
  sleep infinity
  exit 1
fi

if not ./gradlew :nvidia-shadowplay:run -PinputDirectory="/input" -PoutputDirectory="/output" -ParchiveDirectory="/archive"; then
  sleep infinity
  exit 1
fi
