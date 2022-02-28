package com.wilmol.handbrake.nvidia.shadowplay;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Stopwatch;
import com.wilmol.handbrake.core.Cli;
import com.wilmol.handbrake.core.HandBrake;
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

  public static void main(String[] args) {
    try {
      checkArgument(args.length == 4, "Expected 4 args to main method");
      Path inputDirectory = Path.of(args[0]);
      Path outputDirectory = Path.of(args[1]);
      Path archiveDirectory = Path.of(args[2]);
      boolean shutdownComputer = Boolean.parseBoolean(args[2]);

      Cli cli = new Cli();
      HandBrake handBrake = new HandBrake(cli);
      App app = new App(handBrake, cli);

      app.run(inputDirectory, outputDirectory, archiveDirectory, shutdownComputer);
    } catch (Exception e) {
      log.fatal("Fatal error", e);
    }
  }

  private static final Logger log = LogManager.getLogger();

  private final HandBrake handBrake;
  private final Cli cli;

  App(HandBrake handBrake, Cli cli) {
    this.handBrake = checkNotNull(handBrake);
    this.cli = checkNotNull(cli);
  }

  void run(
      Path inputDirectory, Path outputDirectory, Path archiveDirectory, boolean shutdownComputer)
      throws Exception {
    Stopwatch stopwatch = Stopwatch.createStarted();
    log.info(
        "run(inputDirectory={}, outputDirectory={}, archiveDirectory={}, shutdownComputer={}) started",
        inputDirectory,
        outputDirectory,
        archiveDirectory,
        shutdownComputer);

    try {
      Files.createDirectories(outputDirectory);
      Files.createDirectories(archiveDirectory);

      deleteIncompleteEncodings(inputDirectory);
      List<UnencodedVideo> unencodedVideos =
          getUnencodedVideos(inputDirectory, outputDirectory, archiveDirectory);
      archiveVideosThatHaveAlreadyBeenEncoded(unencodedVideos);
      encodeVideos(unencodedVideos);
    } finally {
      log.info("run finished - elapsed: {}", stopwatch.elapsed());

      if (shutdownComputer) {
        log.info("Shutting computer down");
        cli.execute(List.of("shutdown", "-s", "-t", "30"));
      }
    }
  }

  private void deleteIncompleteEncodings(Path inputDirectory) throws IOException {
    List<Path> tempEncodings =
        Files.walk(inputDirectory)
            .filter(Files::isRegularFile)
            .filter(UnencodedVideo::isTempEncodedMp4)
            .toList();
    if (tempEncodings.isEmpty()) {
      return;
    }

    log.warn("Detected {} incomplete encoding(s)", tempEncodings.size());

    int i = 0;
    for (Path path : tempEncodings) {
      log.warn("Deleting ({}/{}): {}", ++i, tempEncodings.size(), path);
      Files.delete(path);
    }
  }

  private List<UnencodedVideo> getUnencodedVideos(
      Path inputDirectory, Path outputDirectory, Path archiveDirectory) throws IOException {
    List<UnencodedVideo> unencodedVideos =
        Files.walk(inputDirectory)
            .filter(Files::isRegularFile)
            .filter(UnencodedVideo::isMp4)
            // don't include paths that represent encoded or archived videos
            // if somebody wants to encode again, they'll need to remove the 'Archived' suffix
            .filter(
                path -> !UnencodedVideo.isEncodedMp4(path) && !UnencodedVideo.isArchivedMp4(path))
            .map(
                path -> new UnencodedVideo(path, inputDirectory, outputDirectory, archiveDirectory))
            .toList();
    log.info("Detected {} unencoded videos(s)", unencodedVideos.size());
    return unencodedVideos;
  }

  // While 'List<UnencodedVideo> videos' represents unencoded videos (NOT encoded videos) the
  // corresponding encoded video may already exist
  private void archiveVideosThatHaveAlreadyBeenEncoded(List<UnencodedVideo> videos)
      throws IOException {
    List<UnencodedVideo> alreadyEncodedVideos =
        videos.stream().filter(UnencodedVideo::hasBeenEncoded).toList();
    if (alreadyEncodedVideos.isEmpty()) {
      return;
    }

    log.warn(
        "Detected {} unencoded video(s) that have already been encoded",
        alreadyEncodedVideos.size());

    int i = 0;
    for (UnencodedVideo video : alreadyEncodedVideos) {
      log.info(
          "Archiving ({}/{}): {} -> {}",
          ++i,
          alreadyEncodedVideos.size(),
          video.originalPath(),
          video.archivedPath());
      // TODO move the logic into UnencodedVideo
      Files.createDirectories(checkNotNull(video.archivedPath().getParent()));
      Files.move(video.originalPath(), video.archivedPath());
    }
  }

  private void encodeVideos(List<UnencodedVideo> videos) throws IOException {
    List<UnencodedVideo> videosToEncode =
        videos.stream().filter(video -> !video.hasBeenEncoded()).toList();

    log.info("Detected {} video(s) to encode", videosToEncode.size());

    int i = 0;
    for (UnencodedVideo video : videosToEncode) {
      log.info("Detected ({}/{}): {}", ++i, videosToEncode.size(), video.originalPath());
    }

    i = 0;
    for (UnencodedVideo video : videosToEncode) {
      log.info("Encoding ({}/{}): {}", ++i, videosToEncode.size(), video.originalPath());
      encodeVideo(video);
    }
  }

  private void encodeVideo(UnencodedVideo video) throws IOException {
    Stopwatch stopwatch = Stopwatch.createStarted();

    // to avoid leaving encoded files in an 'incomplete' state, encode to a temp file in case
    // something goes wrong
    boolean encodeSuccessful = handBrake.encode(video.originalPath(), video.tempEncodedPath());

    if (encodeSuccessful) {
      // only archive the original after renaming the temp file, then it'll never reach a state
      // where the encoding is incomplete and the original doesn't exist
      Files.move(video.tempEncodedPath(), video.encodedPath());
      log.info("Encoded: {} -> {}", video.originalPath(), video.encodedPath());

      Files.createDirectories(checkNotNull(video.archivedPath().getParent()));
      Files.move(video.originalPath(), video.archivedPath());
      log.info("Archived: {} -> {}", video.originalPath(), video.archivedPath());
    } else {
      log.error("Failed to encode: {}", video.originalPath());
    }

    log.info("Elapsed: {}", stopwatch.elapsed());
  }
}
