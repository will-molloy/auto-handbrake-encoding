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

1. Recursively scans input directory for `.mp4` files to encode
2. Encodes `.mp4` files with a Constant Frame Rate (CFR) preset
    - Encoded files are named with the suffix ` - CFR.mp4`
    - The preset used is HandBrake's built-in "Production Standard" preset (H.264)
      - It works with any video resolution
      - It works with any framerate
      - It creates quite a large file afterwards, but it's ideal "as an intermediate format for video editing"
      - I recommend deleting the encoded file after using it, and retaining the original archived file
3. Archives original videos
    - Archived files are named with the suffix ` - Archived.mp4`
    - They won't be detected by the program again, if you want to encode again, remove this suffix first
4. (optional) Shuts computer down after running (useful if for example running overnight)

#### Usage:

1. Build via Gradle:
   ```bash
   ./gradlew build
   ```

2. Configure [Gradle task](nvidia-shadowplay/build.gradle)
    - Set `inputDirectory` to directory containing `.mp4` files to encode
    - Set `outputDirectory` where you want encoded files to be saved
    - Set `archiveDirectory` where you want archived files to be saved
    - (These can all be the same directory, personally I record and encode to an SSD, then archive to NAS)


3. Run via Gradle:
   ```bash
   ./gradlew :nvidia-shadowplay:run
   ```
   or to shutdown afterwards (e.g. running overnight):
   ```bash
   ./gradlew :nvidia-shadowplay:runAndThenShutdownComputer
   ```
