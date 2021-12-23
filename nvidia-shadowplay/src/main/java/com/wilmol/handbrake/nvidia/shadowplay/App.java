package com.wilmol.handbrake.nvidia.shadowplay;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Stopwatch;
import com.google.common.io.Resources;
import com.wilmol.handbrake.core.Cli;
import com.wilmol.handbrake.core.HandBrake;
import com.wilmol.handbrake.util.IndexedObject;
import java.io.IOException;
import java.net.URISyntaxException;
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

  private final Path preset = Path.of(Resources.getResource("presets/cfr-60fps.json").toURI());

  private final HandBrake handBrake;

  App(HandBrake handBrake) throws URISyntaxException {
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

    // delete any incomplete encodings from a previous run
    deleteIncompleteEncodings(videosPath);

    List<UnencodedVideo> unencodedVideos = getUnencodedVideos(videosPath);
    log.info("Detected {} unencoded videos(s)", unencodedVideos.size());

    if (deleteOriginalVideos) {
      // while 'List<UnencodedVideo> unencodedVideos' represents unencoded videos, the corresponding
      // encoded videos may already exist
      deleteVideosThatHaveAlreadyBeenEncoded(unencodedVideos);
    }

    encodeVideos(unencodedVideos, deleteOriginalVideos);

    log.info("run finished - elapsed: {}", stopwatch.elapsed());
  }

  private void deleteIncompleteEncodings(Path videosPath) throws IOException {
    List<IndexedObject<Path>> tempEncodings =
        IndexedObject.oneIndexed(
            Files.walk(videosPath)
                .filter(Files::isRegularFile)
                .filter(UnencodedVideo::isTempEncodedMp4)
                .toList());

    log.warn("Detected {} incomplete encoding(s)", tempEncodings.size());

    for (IndexedObject<Path> path : tempEncodings) {
      log.warn("({}/{}) Deleting: {}", path.index(), tempEncodings.size(), path.object());
      Files.delete(path.object());
    }
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
    List<IndexedObject<UnencodedVideo>> alreadyEncodedVideos =
        IndexedObject.oneIndexed(videos.stream().filter(UnencodedVideo::hasBeenEncoded).toList());

    log.info(
        "Detected {} unencoded video(s) that have already been encoded",
        alreadyEncodedVideos.size());

    for (IndexedObject<UnencodedVideo> video : alreadyEncodedVideos) {
      log.info(
          "({}/{}) Deleting: {}",
          video.index(),
          alreadyEncodedVideos.size(),
          video.object().originalPath());
      Files.delete(video.object().originalPath());
    }
  }

  private void encodeVideos(List<UnencodedVideo> videos, boolean deleteOriginalVideos)
      throws IOException {
    List<IndexedObject<UnencodedVideo>> videosToEncode =
        IndexedObject.oneIndexed(videos.stream().filter(video -> !video.hasBeenEncoded()).toList());

    log.info("Detected {} video(s) to encode", videosToEncode.size());

    for (IndexedObject<UnencodedVideo> video : videosToEncode) {
      log.info(
          "({}/{}) Detected: {}",
          video.index(),
          videosToEncode.size(),
          video.object().originalPath());
    }

    for (IndexedObject<UnencodedVideo> video : videosToEncode) {
      log.info(
          "({}/{}) Encoding: {}",
          video.index(),
          videosToEncode.size(),
          video.object().originalPath());
      encodeVideo(video.object(), deleteOriginalVideos);
    }
  }

  private void encodeVideo(UnencodedVideo video, boolean deleteOriginalVideos) throws IOException {
    // to avoid leaving encoded files in an 'incomplete' state, encode to a temp file in case
    // something goes wrong
    boolean encodeSuccessful =
        handBrake.encode(video.originalPath(), video.tempEncodedPath(), preset);

    if (encodeSuccessful) {
      // only delete the original after renaming the temp file, then it'll never reach a state where
      // the encoding is incomplete and the original doesn't exist
      Files.move(video.tempEncodedPath(), video.encodedPath());

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
