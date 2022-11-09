package com.willmolloy.handbrake.cfr;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Stopwatch;
import com.willmolloy.handbrake.core.HandBrake;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
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

  boolean run(Path inputDirectory, Path outputDirectory, Path archiveDirectory) throws Exception {
    Stopwatch stopwatch = Stopwatch.createStarted();
    log.info(
        "run(inputDirectory={}, outputDirectory={}, archiveDirectory={}) started",
        inputDirectory,
        outputDirectory,
        archiveDirectory);

    try {
      deleteIncompleteEncodingsAndArchives(inputDirectory, outputDirectory, archiveDirectory);

      UnencodedVideo.Factory factory =
          new UnencodedVideo.Factory(inputDirectory, outputDirectory, archiveDirectory);
      List<UnencodedVideo> unencodedVideos = getUnencodedVideos(inputDirectory, factory);

      return encodeAndArchiveVideos(unencodedVideos);
    } finally {
      log.info("run finished - elapsed: {}", stopwatch.elapsed());
    }
  }

  private void deleteIncompleteEncodingsAndArchives(
      Path inputDirectory, Path outputDirectory, Path archiveDirectory) throws IOException {
    List<Path> tempFiles =
        Stream.of(inputDirectory, outputDirectory, archiveDirectory)
            .distinct()
            .flatMap(
                directory -> {
                  try {
                    return Files.walk(directory)
                        .filter(Files::isRegularFile)
                        .filter(
                            file ->
                                UnencodedVideo.isTempEncodedMp4(file)
                                    || UnencodedVideo.isTempArchivedMp4(file));
                  } catch (IOException e) {
                    throw new UncheckedIOException(e);
                  }
                })
            .toList();

    if (!tempFiles.isEmpty()) {
      log.warn("Detected {} incomplete encoding(s)/archives(s)", tempFiles.size());
      int i = 0;
      for (Path file : tempFiles) {
        log.warn("Deleting ({}/{}): {}", ++i, tempFiles.size(), file);
        Files.deleteIfExists(file);
      }
    }
  }

  private List<UnencodedVideo> getUnencodedVideos(
      Path inputDirectory, UnencodedVideo.Factory factory) throws IOException {
    return Files.walk(inputDirectory)
        .filter(Files::isRegularFile)
        .filter(UnencodedVideo::isMp4)
        // don't include paths that represent encoded or archived videos
        // if somebody wants to encode again, they'll need to remove the 'Archived' suffix
        .filter(path -> !UnencodedVideo.isEncodedMp4(path) && !UnencodedVideo.isArchivedMp4(path))
        .map(factory::newUnencodedVideo)
        .toList();
  }

  private boolean encodeAndArchiveVideos(List<UnencodedVideo> videos) {
    boolean overallSuccess = true;

    log.info("Detected {} video(s) to encode", videos.size());
    int i = 0;
    for (UnencodedVideo video : videos) {
      log.info("Detected ({}/{}): {}", ++i, videos.size(), video);
    }

    i = 0;
    List<CompletableFuture<Boolean>> archiverFutures = new ArrayList<>();
    for (UnencodedVideo video : videos) {
      log.info("Encoding ({}/{}): {}", ++i, videos.size(), video);
      if (videoEncoder.encode(video)) {
        // run archiving async as it can be expensive (e.g. moving to another disk or NAS)
        // then while it's archiving it can encode the next video
        archiverFutures.add(videoArchiver.archiveAsync(video));
      } else {
        overallSuccess = false;
      }
    }

    for (CompletableFuture<Boolean> future : archiverFutures) {
      overallSuccess &= future.join();
    }

    return overallSuccess;
  }

  public static void main(String... args) {
    try {
      checkArgument(args.length == 3, "Expected 3 args to main method");
      Path inputDirectory = Path.of(args[0]);
      Path outputDirectory = Path.of(args[1]);
      Path archiveDirectory = Path.of(args[2]);

      VideoEncoder videoEncoder = new VideoEncoder(new HandBrake());
      VideoArchiver videoArchiver = new VideoArchiver();
      App app = new App(videoEncoder, videoArchiver);

      if (!app.run(inputDirectory, outputDirectory, archiveDirectory)) {
        System.exit(1);
      }
    } catch (Throwable t) {
      log.fatal("Fatal error", t);
      System.exit(1);
    }
  }
}
