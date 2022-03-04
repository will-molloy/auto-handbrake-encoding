package com.wilmol.handbrake.nvidia.shadowplay;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Stopwatch;
import com.wilmol.handbrake.core.Cli;
import com.wilmol.handbrake.core.Computer;
import com.wilmol.handbrake.core.HandBrake;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Core app runner.
 *
 * @author <a href=https://wilmol.com>Will Molloy</a>
 */
class App {

  private static final Logger log = LogManager.getLogger();

  private final VideoEncoder videoEncoder;
  private final VideoArchiver videoArchiver;
  private final Computer computer;

  App(VideoEncoder videoEncoder, VideoArchiver videoArchiver, Computer computer) {
    this.videoEncoder = checkNotNull(videoEncoder);
    this.videoArchiver = checkNotNull(videoArchiver);
    this.computer = checkNotNull(computer);
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
      for (Path directory : List.of(inputDirectory, outputDirectory, archiveDirectory)) {
        deleteIncompleteEncodingsAndArchives(directory);
      }

      UnencodedVideo.Factory factory =
          new UnencodedVideo.Factory(inputDirectory, outputDirectory, archiveDirectory);
      List<UnencodedVideo> unencodedVideos = getUnencodedVideos(inputDirectory, factory);

      encodeAndArchiveVideos(unencodedVideos);
    } finally {
      log.info("run finished - elapsed: {}", stopwatch.elapsed());

      if (shutdownComputer) {
        computer.shutdown();
      }
    }
  }

  private void deleteIncompleteEncodingsAndArchives(Path directory) throws IOException {
    List<Path> tempFiles =
        Files.walk(directory)
            .filter(Files::isRegularFile)
            .filter(
                file ->
                    UnencodedVideo.isTempEncodedMp4(file) || UnencodedVideo.isTempArchivedMp4(file))
            .toList();
    if (!tempFiles.isEmpty()) {
      log.warn(
          "Detected {} incomplete encoding(s)/archives(s) in directory: {}",
          tempFiles.size(),
          directory);
      int i = 0;
      for (Path file : tempFiles) {
        log.warn("Deleting ({}/{}): {}", ++i, tempFiles.size(), file);
        Files.delete(file);
      }
    }
  }

  private List<UnencodedVideo> getUnencodedVideos(
      Path inputDirectory, UnencodedVideo.Factory factory) throws IOException {
    List<UnencodedVideo> videos =
        Files.walk(inputDirectory)
            .filter(Files::isRegularFile)
            .filter(UnencodedVideo::isMp4)
            // don't include paths that represent encoded or archived videos
            // if somebody wants to encode again, they'll need to remove the 'Archived' suffix
            .filter(
                path -> !UnencodedVideo.isEncodedMp4(path) && !UnencodedVideo.isArchivedMp4(path))
            .map(factory::newUnencodedVideo)
            .toList();
    log.info("Detected {} video(s) to encode in directory: {}", videos.size(), inputDirectory);
    return videos;
  }

  private void encodeAndArchiveVideos(List<UnencodedVideo> videos) {
    int i = 0;
    for (UnencodedVideo video : videos) {
      log.info("Detected ({}/{}): {}", ++i, videos.size(), video);
    }

    i = 0;
    List<CompletableFuture<?>> archiverFutures = new ArrayList<>();
    for (UnencodedVideo video : videos) {
      log.info("Encoding ({}/{}): {}", ++i, videos.size(), video);
      if (videoEncoder.encode(video)) {
        // run archiving async as it can be expensive (e.g. moving to another disk or NAS)
        // then while it's archiving it can encode the next video
        archiverFutures.add(videoArchiver.archiveAsync(video));
      }
    }

    for (CompletableFuture<?> future : archiverFutures) {
      future.join();
    }
  }

  public static void main(String... args) {
    try {
      checkArgument(args.length == 4, "Expected 4 args to main method");
      Path inputDirectory = Path.of(args[0]);
      Path outputDirectory = Path.of(args[1]);
      Path archiveDirectory = Path.of(args[2]);
      boolean shutdownComputer = Boolean.parseBoolean(args[3]);

      Cli cli = new Cli();
      HandBrake handBrake = new HandBrake(cli);
      VideoEncoder videoEncoder = new VideoEncoder(handBrake);
      VideoArchiver videoArchiver = new VideoArchiver();
      Computer computer = new Computer(cli);
      App app = new App(videoEncoder, videoArchiver, computer);

      app.run(inputDirectory, outputDirectory, archiveDirectory, shutdownComputer);
    } catch (Exception e) {
      log.fatal("Fatal error", e);
    }
  }
}
