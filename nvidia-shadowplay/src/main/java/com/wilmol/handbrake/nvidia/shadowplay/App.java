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

    List<UnencodedVideo> unencodedVideos = getUnencodedVideos(videosPath);
    log.info("Detected {} unencoded videos(s)", unencodedVideos.size());

    if (deleteOriginalVideos) {
      // while 'List<UnencodedVideo> unencodedVideos' represents unencoded videos,
      // the corresponding encoded videos may already exist
      deleteVideosThatHaveAlreadyBeenEncoded(unencodedVideos);
    }

    encodeVideos(unencodedVideos, deleteOriginalVideos);

    log.info("run finished - elapsed: {}", stopwatch.elapsed());
  }

  private List<UnencodedVideo> getUnencodedVideos(Path videosPath) throws IOException {
    return Files.walk(videosPath)
        .filter(Files::isRegularFile)
        // don't include paths that represent encoded videos
        .filter(path -> UnencodedVideo.isMp4(path) && !UnencodedVideo.isEncodedMp4(path))
        .map(UnencodedVideo::new)
        .toList();
  }

  private void deleteVideosThatHaveAlreadyBeenEncoded(List<UnencodedVideo> videos)
      throws IOException {
    List<UnencodedVideo> alreadyEncodedVideos =
        videos.stream().filter(UnencodedVideo::hasBeenEncoded).toList();

    log.info(
        "Detected {} unencoded video(s) that have already been encoded",
        alreadyEncodedVideos.size());

    for (int i = 0; i < alreadyEncodedVideos.size(); i++) {
      UnencodedVideo video = alreadyEncodedVideos.get(i);

      log.info("({}/{}) Deleting: {}", i + 1, alreadyEncodedVideos.size(), video.originalPath());
      Files.delete(video.originalPath());
    }
  }

  private void encodeVideos(List<UnencodedVideo> videos, boolean deleteOriginalVideos)
      throws IOException {
    List<UnencodedVideo> videosToEncode =
        videos.stream().filter(video -> !video.hasBeenEncoded()).toList();

    log.info("Detected {} video(s) to encode", videosToEncode.size());

    for (int i = 0; i < videosToEncode.size(); i++) {
      UnencodedVideo video = videosToEncode.get(i);

      log.info("({}/{}) Detected: {}", i + 1, videosToEncode.size(), video.originalPath());
    }

    for (int i = 0; i < videosToEncode.size(); i++) {
      UnencodedVideo video = videosToEncode.get(i);

      log.info("({}/{}) Encoding: {}", i + 1, videosToEncode.size(), video.originalPath());
      boolean encodeSuccessful = handBrake.encode(video);

      if (encodeSuccessful) {
        log.info("Encoded: {}", video.encodedPath());

        if (deleteOriginalVideos) {
          log.info("Deleting: {}", video.originalPath());
          Files.delete(video.originalPath());
        }
      } else {
        log.error("Encode failed: {}", video.originalPath());

        if (deleteOriginalVideos) {
          log.error("Skipping deletion of: {}", video.originalPath());
        }
      }
    }
  }

  public static void main(String[] args) {
    try {
      Path videosPath = Path.of("D:\\Videos\\Gameplay");
      boolean deleteOriginalVideos = true;
      boolean shutdownComputer = false;

      App app = new App(new HandBrake(new Cli()));

      app.run(videosPath, deleteOriginalVideos, shutdownComputer);
    } catch (Exception e) {
      log.fatal("Fatal error", e);
    }
  }
}
