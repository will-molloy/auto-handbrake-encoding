package com.wilmol.handbrake.nvidia.shadowplay;

import com.google.common.base.Stopwatch;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Core app runner.
 *
 * @author <a href=https://wilmol.com>Will Molloy</a>
 */
class App {

  private static final Logger log = LogManager.getLogger();

  private static final String ORIGINAL_SUFFIX = ".mp4";
  private static final String ENCODED_SUFFIX = " - CFR 60 FPS.mp4";

  void run(Path recordingsPath, boolean deleteOriginalRecordings, boolean shutdownComputer)
      throws IOException {
    Stopwatch stopwatch = Stopwatch.createStarted();
    log.info(
        "run(recordingsPath={}, deleteOriginalRecordings={}, shutdownComputer={}) started",
        recordingsPath,
        deleteOriginalRecordings,
        shutdownComputer);

    List<Path> originalRecordings = scanFiles(recordingsPath);
    for (Path path : originalRecordings) {
      log.info("Detected: {}", path);
    }

    for (Path path : originalRecordings) {
      log.info("Processing: {}", path);
    }

    log.info("run finished - elapsed: {}", stopwatch.elapsed());
  }

  private List<Path> scanFiles(Path recordingsPath) throws IOException {
    return Files.walk(recordingsPath)
        .filter(Files::isRegularFile)
        .filter(
            path ->
                path.toString().endsWith(ORIGINAL_SUFFIX)
                    && !path.toString().endsWith(ENCODED_SUFFIX))
        .toList();
  }

  public static void main(String[] args) throws Exception {
    // Path to the directory containing the .mp4 recordings
    Path recordingsPath = Path.of("D:\\Videos\\Gameplay");

    // If original recordings should be deleted
    boolean deleteOriginalRecordings = false;

    // If the computer should be shutdown after encoding
    boolean shutdownComputer = false;

    new App().run(recordingsPath, deleteOriginalRecordings, shutdownComputer);
  }
}
