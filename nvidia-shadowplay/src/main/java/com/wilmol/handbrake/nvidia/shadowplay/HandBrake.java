package com.wilmol.handbrake.nvidia.shadowplay;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.io.Resources;
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

  /**
   * Encodes the given video.
   *
   * @param video video to encode
   * @return true if encoding was successful
   */
  boolean encode(UnencodedVideo video) {
    if (video.hasBeenEncoded()) {
      log.warn("Video ({}) has already been encoded", video.originalPath());
      return true;
    }

    try {
      cli.executeCommand(
          "HandBrakeCLI.exe --preset-import-file \"%s\" -i \"%s\" -o \"%s\""
              .formatted(preset, video.originalPath(), video.encodedPath()));
      return true;
    } catch (Exception e) {
      log.error("Error encoding video: %s".formatted(video.originalPath()), e);
      return false;
    }
  }
}
