# auto-handbrake-encoding

[![build](https://github.com/wilmol/auto-handbrake-encoding/workflows/build/badge.svg?event=push)](https://github.com/wilmol/auto-handbrake-encoding/actions?query=workflow%3Abuild)
[![codecov](https://codecov.io/gh/wilmol/auto-handbrake-encoding/branch/main/graph/badge.svg)](https://codecov.io/gh/wilmol/auto-handbrake-encoding)

Automating HandBrake encoding

## Requirements

- Java 17
- HandBrakeCLI

## Use cases

### Encoding Nvidia ShadowPlay videos with a CFR preset

#### Why?

- Nvidia ShadowPlay records video with a variable/peak frame rate (PFR), leading to audio sync issues in video editing software

#### How?

1. Recursively scans input directory for any `.mp4` files that haven't already been encoded
2. Encodes `.mp4` files with a Constant Frame Rate (CFR) preset
    - Encoded files are named with the suffix ` - CFR 60 FPS.mp4`
    - The preset is HandBrake's built-in "Production Max" preset (H.264)
      - It works with any video resolution
      - It works with any framerate
      - It creates quite a large file afterwards, but it's ideal for high quality video editing
      - I recommend deleting the encoded file after using it, and retaining the original archived file
3. Archives original videos, by renaming them with the suffix ` - Archived.mp4`
    - They won't be detected by the program again, if you want to encode again, remove this suffix first
4. (optional) Shuts computer down after encoding

#### Usage:

- Build:

```bash
./gradlew build
```

- Configure main [App class](nvidia-shadowplay/src/main/java/com/wilmol/handbrake/nvidia/shadowplay/App.java)
    - Point to directory containing `.mp4` files
    - Set flag if computer should shutdown after encoding


- Run main [App class](nvidia-shadowplay/src/main/java/com/wilmol/handbrake/nvidia/shadowplay/App.java):

```bash
./gradlew :nvidia-shadowplay:app
```
