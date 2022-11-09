package com.willmolloy.handbrake.cfr;

import static com.google.common.base.Preconditions.checkArgument;

import com.willmolloy.handbrake.core.HandBrake;
import java.nio.file.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Main entrypoint.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
final class Main {
  private Main() {}

  private static final Logger log = LogManager.getLogger();

  public static void main(String... args) {
    try {
      checkArgument(args.length == 3, "Expected 3 args to main method");
      Path inputDirectory = Path.of(args[0]);
      Path outputDirectory = Path.of(args[1]);
      Path archiveDirectory = Path.of(args[2]);

      VideoEncoder videoEncoder = new VideoEncoder(new HandBrake());
      VideoArchiver videoArchiver = new VideoArchiver();
      App app = new App(videoEncoder, videoArchiver);

      if (!app.run(inputDirectory, outputDirectory, archiveDirectory)) {
        System.exit(1);
      }
    } catch (Throwable t) {
      log.fatal("Fatal error", t);
      System.exit(1);
    }
  }
}
