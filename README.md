# auto-handbrake-encoding

[![build](https://github.com/will-molloy/auto-handbrake-encoding/workflows/build/badge.svg?branch=main)](https://github.com/will-molloy/auto-handbrake-encoding/actions?query=workflow%3Abuild)
[![integration-test](https://github.com/will-molloy/auto-handbrake-encoding/workflows/integration-test/badge.svg?branch=main)](https://github.com/will-molloy/auto-handbrake-encoding/actions?query=workflow%3Aintegration-test)
[![codecov](https://codecov.io/gh/will-molloy/auto-handbrake-encoding/branch/main/graph/badge.svg)](https://codecov.io/gh/will-molloy/auto-handbrake-encoding)

Automating HandBrake encoding

## Use cases

### Converting to CFR video

#### Why?

- You might have variable/peak frame rate (VFR/PFR) recordings, leading to audio sync issues in video editing software
  - This is a problem with NVIDIA ShadowPlay and AMD ReLive

#### How?

1. Recursively scans input directory for `.mp4` files to encode
2. Encodes `.mp4` files with a Constant Frame Rate (CFR) preset
    - Encoded files are named with the suffix `.cfr.mp4`
    - The preset used is "Production Standard" H.264
      - It works with any video resolution
      - It works with any framerate
      - It creates quite a large file afterwards, but it's ideal "as an intermediate format for video editing"
      - I recommend deleting the encoded file after using it, and retaining the original archived file
3. Archives original videos

#### Usage:

You can run with either Gradle directly or with Docker.

The app requires the following arguments:
- `inputDirectory` directory containing `.mp4` files to encode
- `outputDirectory` where you want encoded files to be saved
- `archiveDirectory` where you want archived files to be saved
- (These can all be the same directory, personally I record and encode to an SSD, then archive to NAS)

##### Run with Docker

1. Build:
   ```bash
   docker build -t handbrake .
   ```

2. Run:
   ```bash
   docker run --rm -v <INPUT_DIR>:/input -v <OUTPUT_DIR>:/output -v <ARCHIVE_DIR>:/archive handbrake
   ```
   - (If you need to mount a network drive, [this stackoverflow answer](https://stackoverflow.com/a/57510166/6122976) worked for me)

## Project layout

| Module                                       | Description                                                   |
|----------------------------------------------|---------------------------------------------------------------|
| [auto-handbrake-core](./auto-handbrake-core) | Core interface and classes required to run HandBrake via Java |
| [auto-handbrake-cfr](./auto-handbrake-cfr)   | CFR conversion, [described above](#converting-to-cfr-video)   |