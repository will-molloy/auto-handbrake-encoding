# auto-handbrake-encoding

[![build](https://github.com/wilmol/auto-handbrake-encoding/workflows/build/badge.svg?event=push)](https://github.com/wilmol/auto-handbrake-encoding/actions?query=workflow%3Abuild)
[![codecov](https://codecov.io/gh/wilmol/auto-handbrake-encoding/branch/main/graph/badge.svg)](https://codecov.io/gh/wilmol/auto-handbrake-encoding)

Automating HandBrake encoding

## Requirements

- Java 17
- HandBrakeCLI

## Use cases

### Encoding Nvidia ShadowPlay recordings with a CFR preset

#### Why?
- Nvidia ShadowPlay records with a variable/peak frame rate (VFR), leading to audio sync issues
- The recordings may also have a large file size

#### How?
1. Recursively scans directory for any `.mp4` files that haven't already been encoded
2. Encodes `.mp4` files with a Constant Frame Rate (CFR) preset at 60 FPS
   - Encoded files are named with the suffix `  - CFR 60 FPS.mp4`
   - The preset doesn't change anything else, it keeps the original resolution etc.
3. (optional) Deletes original recordings
4. (optional) Shuts computer down after encoding

#### Usage:
- Build:
```bash
./gradlew build
```

- Configure main [App class](nvidia-shadowplay/src/main/java/com/wilmol/handbrake/nvidia/shadowplay/App.java)
  - Point to directory containing `.mp4` files
  - Set flag if original recordings should be deleted
  - Set flag if computer should shutdown after encoding


- Run main [App class](nvidia-shadowplay/src/main/java/com/wilmol/handbrake/nvidia/shadowplay/App.java):
```bash
./gradlew :nvidia-shadowplay:app
```
