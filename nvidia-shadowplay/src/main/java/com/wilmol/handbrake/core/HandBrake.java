package com.wilmol.handbrake.core;

import static com.google.common.base.Preconditions.checkNotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * HandBrake interface.
 *
 * @author <a href=https://wilmol.com>Will Molloy</a>
 */
public class HandBrake {

  private static final Logger log = LogManager.getLogger();

  private static final String PRESET = "Production Standard";

  private final Cli cli;

  public HandBrake(Cli cli) {
    this.cli = checkNotNull(cli);
  }

  /**
   * Runs HandBrake encoding.
   *
   * @param input input .mp4 file
   * @param output output .mp4 file
   * @return {@code true} if encoding was successful
   */
  public boolean encode(Path input, Path output) {
    if (Files.exists(output)) {
      log.warn("Output ({}) already exists", output);
      return true;
    }

    try {
      return cli.execute(
          List.of(
              "HandBrakeCLI", "--preset", quote(PRESET), "-i", quote(input), "-o", quote(output)));
    } catch (Exception e) {
      log.error("Error encoding: %s".formatted(input), e);
      return false;
    }
  }

  private String quote(Object s) {
    return "\"%s\"".formatted(s);
  }
}
