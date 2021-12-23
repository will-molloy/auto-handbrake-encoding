package com.wilmol.handbrake.core;

import static com.google.common.base.Preconditions.checkNotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * HandBrake interface.
 *
 * @author <a href=https://wilmol.com>Will Molloy</a>
 */
public class HandBrake {

  private static final Logger log = LogManager.getLogger();

  private final Cli cli;

  public HandBrake(Cli cli) {
    this.cli = checkNotNull(cli);
  }

  /**
   * Runs HandBrake encoding.
   *
   * @param input input .mp4 file
   * @param output output .mp4 file
   * @param preset preset .json file
   * @return {@code true} if encoding was successful
   */
  public boolean encode(Path input, Path output, Path preset) {
    if (Files.exists(output)) {
      log.warn("Output ({}) already exists", output);
      return true;
    }

    try {
      return cli.executeCommand(
          "HandBrakeCLI",
          "--preset-import-file",
          quote(preset),
          "-i",
          quote(input),
          "-o",
          quote(output));
    } catch (Exception e) {
      log.error("Error encoding: %s".formatted(input), e);
      return false;
    }
  }

  private String quote(Object s) {
    return "\"%s\"".formatted(s);
  }
}
