package com.wilmol.handbrake.nvidia.shadowplay;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * HandBrake interface.
 *
 * @author <a href=https://wilmol.com>Will Molloy</a>
 */
class HandBrake {

  private static final Logger log = LogManager.getLogger();

  private final Path preset = Path.of(Resources.getResource("presets/cfr-60fps.json").toURI());

  private final Cli cli;

  HandBrake(Cli cli) throws URISyntaxException {
    this.cli = checkNotNull(cli);
  }

  void encode(Video video) throws IOException, InterruptedException {
    if (video.isEncoded()) {
      log.warn("Encoded video ({}) already exists", video.encodedPath());
      return;
    }

    cli.executeCommand(
        "HandBrakeCLI.exe --preset-import-file \"%s\" -i \"%s\" -o \"%s\""
            .formatted(preset, video.originalPath(), video.encodedPath()));
  }
}
