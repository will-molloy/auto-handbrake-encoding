package com.willmolloy.handbrake.cfr;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Stopwatch;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Core app runner.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
class App {

  private static final Logger log = LogManager.getLogger();

  private final VideoEncoder videoEncoder;
  private final VideoArchiver videoArchiver;

  App(VideoEncoder videoEncoder, VideoArchiver videoArchiver) {
    this.videoEncoder = checkNotNull(videoEncoder);
    this.videoArchiver = checkNotNull(videoArchiver);
  }

  /**
   * Runs the app.
   *
   * @param inputDirectory directory containing unencoded files
   * @param outputDirectory directory to contain encoded files
   * @param archiveDirectory directory to contain archived files
   * @return {@code true} if all encoding and archiving was successful
   */
  boolean run(Path inputDirectory, Path outputDirectory, Path archiveDirectory) throws IOException {
    log.info(
        "run(inputDirectory={}, outputDirectory={}, archiveDirectory={}) started",
        inputDirectory,
        outputDirectory,
        archiveDirectory);
    logBreak();

    Stopwatch stopwatch = Stopwatch.createStarted();
    try {
      DirectoryScanner directoryScanner =
          new DirectoryScanner(inputDirectory, outputDirectory, archiveDirectory);
      JobQueue jobQueue = new JobQueue(videoEncoder, videoArchiver);

      directoryScanner.deleteIncompleteEncodingsAndArchives();
      List<UnencodedVideo> unencodedVideos = directoryScanner.getUnencodedVideos();

      logBreak();

      return jobQueue.encodeAndArchiveVideos(unencodedVideos);
    } finally {
      log.info("Elapsed: {}", stopwatch.elapsed());
    }
  }

  private static void logBreak() {
    log.info("-----------------------------------------------------------------------------------");
  }
}
