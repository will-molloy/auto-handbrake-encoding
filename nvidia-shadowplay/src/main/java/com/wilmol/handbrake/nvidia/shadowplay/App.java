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
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
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
      deleteIncompleteEncodings(outputDirectory);
      deleteIncompleteArchives(archiveDirectory);

      List<UnencodedVideo> unencodedVideos =
          getUnencodedVideos(inputDirectory, outputDirectory, archiveDirectory);
      unencodedVideos = archiveVideosThatHaveAlreadyBeenEncoded(unencodedVideos);

      encodeVideos(unencodedVideos);
    } finally {
      log.info("run finished - elapsed: {}", stopwatch.elapsed());

      if (shutdownComputer) {
        computer.shutdown();
      }
    }
  }

  private void deleteIncompleteEncodings(Path outputDirectory) throws IOException {
    List<Path> tempEncodings =
        Files.walk(outputDirectory)
            .filter(Files::isRegularFile)
            .filter(UnencodedVideo.Factory::isTempEncodedMp4)
            .toList();

    if (!tempEncodings.isEmpty()) {
      log.warn("Detected {} incomplete encoding(s)", tempEncodings.size());

      int i = 0;
      for (Path path : tempEncodings) {
        log.warn("Deleting ({}/{}): {}", ++i, tempEncodings.size(), path);
        Files.delete(path);
      }
    }
  }

  private void deleteIncompleteArchives(Path archiveDirectory) throws IOException {
    List<Path> tempArchives =
        Files.walk(archiveDirectory)
            .filter(Files::isRegularFile)
            .filter(UnencodedVideo.Factory::isTempArchivedMp4)
            .toList();

    if (!tempArchives.isEmpty()) {
      log.warn("Detected {} incomplete archive(s)", tempArchives.size());

      int i = 0;
      for (Path path : tempArchives) {
        log.warn("Deleting ({}/{}): {}", ++i, tempArchives.size(), path);
        Files.delete(path);
      }
    }
  }

  private List<UnencodedVideo> getUnencodedVideos(
      Path inputDirectory, Path outputDirectory, Path archiveDirectory) throws IOException {
    UnencodedVideo.Factory factory =
        new UnencodedVideo.Factory(inputDirectory, outputDirectory, archiveDirectory);

    return Files.walk(inputDirectory)
        .filter(Files::isRegularFile)
        .filter(UnencodedVideo.Factory::isMp4)
        // don't include paths that represent encoded or archived videos
        // if somebody wants to encode again, they'll need to remove the 'Archived' suffix
        .filter(
            path ->
                !UnencodedVideo.Factory.isEncodedMp4(path)
                    && !UnencodedVideo.Factory.isArchivedMp4(path))
        .map(factory::newUnencodedVideo)
        .toList();
  }

  // the corresponding encoded video(s) may already exist
  private List<UnencodedVideo> archiveVideosThatHaveAlreadyBeenEncoded(
      List<UnencodedVideo> videos) {
    Map<Boolean, List<UnencodedVideo>> partition =
        videos.stream().collect(Collectors.partitioningBy(UnencodedVideo::hasBeenEncoded));
    List<UnencodedVideo> encodedVideos = partition.get(true);
    List<UnencodedVideo> unencodedVideos = partition.get(false);

    if (!encodedVideos.isEmpty()) {
      log.warn(
          "Detected {} unencoded video(s) that have already been encoded", encodedVideos.size());

      for (UnencodedVideo video : encodedVideos) {
        videoArchiver.archiveAsync(video).join();
      }
    }

    return unencodedVideos;
  }

  private void encodeVideos(List<UnencodedVideo> videos) {
    log.info("Detected {} video(s) to encode", videos.size());

    int i = 0;
    for (UnencodedVideo video : videos) {
      log.info("Detected ({}/{}): {}", ++i, videos.size(), video);
    }

    i = 0;
    List<CompletableFuture<?>> futures = new ArrayList<>();
    for (UnencodedVideo video : videos) {
      log.info("Encoding ({}/{}): {}", ++i, videos.size(), video);
      if (videoEncoder.encode(video)) {
        futures.add(videoArchiver.archiveAsync(video));
      }
    }

    for (CompletableFuture<?> future : futures) {
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
