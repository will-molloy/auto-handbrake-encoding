package com.wilmol.handbrake.nvidia.shadowplay;

import static com.google.common.base.Preconditions.checkNotNull;

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

  private final HandBrake handBrake;

  App(HandBrake handBrake) {
    this.handBrake = checkNotNull(handBrake);
  }

  void run(Path videosPath, boolean deleteOriginalVideos, boolean shutdownComputer)
      throws Exception {
    Stopwatch stopwatch = Stopwatch.createStarted();
    log.info(
        "run(videosPath={}, deleteOriginalVideos={}, shutdownComputer={}) started",
        videosPath,
        deleteOriginalVideos,
        shutdownComputer);

    List<Video> videos = getVideosToEncode(videosPath);
    log.info("Detected {} video(s) to encode", videos.size());
    for (Video video : videos) {
      log.info("Detected: {}", video.originalPath());
    }

    for (int i = 0; i < videos.size(); i++) {
      Video video = videos.get(i);
      log.info("Encoding ({}/{}): {}", i + 1, videos.size(), video.originalPath());
      handBrake.encode(video);
      log.info("Encoded ({}/{}): {}", i + 1, videos.size(), video.encodedPath());
    }

    log.info("run finished - elapsed: {}", stopwatch.elapsed());
  }

  private List<Video> getVideosToEncode(Path videosPath) throws IOException {
    return Files.walk(videosPath)
        .filter(Files::isRegularFile)
        .filter(path -> Video.isMp4(path) && !Video.isEncoded(path))
        .map(Video::new)
        .filter(video -> !video.isEncoded())
        .toList();
  }

  public static void main(String[] args) {
    Path videosPath = Path.of("D:\\Videos\\Gameplay");
    boolean deleteOriginalVideos = false;
    boolean shutdownComputer = false;

    try {
      App app = new App(new HandBrake(new Cli()));

      app.run(videosPath, deleteOriginalVideos, shutdownComputer);
    } catch (Exception e) {
      log.fatal("Fatal error", e);
    }
  }
}
