package com.wilmol.handbrake.nvidia.shadowplay;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Stopwatch;
import com.google.common.io.Resources;
import com.wilmol.handbrake.core.Cli;
import com.wilmol.handbrake.core.HandBrake;
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

  public static void main(String[] args) {
    try {
      Path videosPath = Path.of("D:\\Videos\\Gameplay");
      boolean deleteOriginalVideos = true;
      boolean shutdownComputer = false;

      Cli cli = new Cli();
      HandBrake handBrake = new HandBrake(cli);
      App app = new App(handBrake, cli);

      app.run(videosPath, deleteOriginalVideos, shutdownComputer);
    } catch (Exception e) {
      log.fatal("Fatal error", e);
    }
  }

  private static final Logger log = LogManager.getLogger();

  private final Path preset;

  private final HandBrake handBrake;

  private final Cli cli;

  App(HandBrake handBrake, Cli cli) throws URISyntaxException {
    this.preset =
        Path.of(Resources.getResource("presets/custom-production-lossless-cfr-60fps.json").toURI());
    this.handBrake = checkNotNull(handBrake);
    this.cli = checkNotNull(cli);
  }

  void run(Path videosPath, boolean deleteOriginalVideos, boolean shutdownComputer)
      throws Exception {
    Stopwatch stopwatch = Stopwatch.createStarted();
    log.info(
        "run(videosPath={}, deleteOriginalVideos={}, shutdownComputer={}) started",
        videosPath,
        deleteOriginalVideos,
        shutdownComputer);

    if (shutdownComputer) {
      Runtime.getRuntime()
          .addShutdownHook(
              new Thread(
                  () -> {
                    log.info("Shutting computer down");
                    cli.execute(List.of("shutdown", "-s", "-t", "0"));
                  }));
    }

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
    List<Path> tempEncodings =
        Files.walk(videosPath)
            .filter(Files::isRegularFile)
            .filter(UnencodedVideo::isTempEncodedMp4)
            .toList();

    log.warn("Detected {} incomplete encoding(s)", tempEncodings.size());

    for (int i = 0; i < tempEncodings.size(); i++) {
      Path path = tempEncodings.get(i);

      log.warn("({}/{}) Deleting: {}", i + 1, tempEncodings.size(), path);
      Files.delete(path);
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
      encodeVideo(video, deleteOriginalVideos);
    }
  }

  private void encodeVideo(UnencodedVideo video, boolean deleteOriginalVideos) throws IOException {
    Stopwatch stopwatch = Stopwatch.createStarted();

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

    log.info("Elapsed: {}", stopwatch.elapsed());
  }
}
