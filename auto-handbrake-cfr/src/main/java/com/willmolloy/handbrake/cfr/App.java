package com.willmolloy.handbrake.cfr;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Stopwatch;
import java.io.IOException;
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

  private final DirectoryScanner directoryScanner;
  private final JobQueue jobQueue;

  App(DirectoryScanner directoryScanner, JobQueue jobQueue) {
    this.directoryScanner = checkNotNull(directoryScanner);
    this.jobQueue = checkNotNull(jobQueue);
  }

  boolean run() throws IOException {
    log.info("run started");
    logBreak();

    Stopwatch stopwatch = Stopwatch.createStarted();
    try {
      directoryScanner.deleteIncompleteEncodingsAndArchives();
      List<UnencodedVideo> unencodedVideos = directoryScanner.getUnencodedVideos();
      logBreak();
      return jobQueue.encodeAndArchiveVideos(unencodedVideos);
    } finally {
      log.info("Elapsed: {}", stopwatch);
    }
  }

  private static void logBreak() {
    log.info("-----------------------------------------------------------------------------------");
  }
}
