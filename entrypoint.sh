#!/bin/bash

# testing archive directory is non-empty
# ensures volume is mounted correctly
# hacky but it works (for my use case)
if [ -z "$(ls -A /archive)" ]; then
  echo "/archive directory empty, network drive not mounted?"
  exit 1
fi

./gradlew :auto-handbrake-cfr:run --no-daemon -PinputDirectory="/input" -PoutputDirectory="/output" -ParchiveDirectory="/archive"
