package com.willmolloy.handbrake.cfr;

import static com.google.common.base.Preconditions.checkNotNull;

import com.willmolloy.handbrake.cfr.util.Timer;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
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

  /**
   * Runs the app.
   *
   * @param inputDirectory directory containing unencoded files
   * @param outputDirectory directory to contain encoded files
   * @param archiveDirectory directory to contain archived files
   * @return {@code true} if all encoding and archiving was successful
   */
  boolean run(Path inputDirectory, Path outputDirectory, Path archiveDirectory) {
    log.info(
        "run(inputDirectory={}, outputDirectory={}, archiveDirectory={}) started",
        inputDirectory,
        outputDirectory,
        archiveDirectory);
    logBreak();

    return Timer.time(
            () -> {
              try {
                deleteIncompleteEncodingsAndArchives(
                    inputDirectory, outputDirectory, archiveDirectory);

                UnencodedVideo.Factory factory =
                    new UnencodedVideo.Factory(inputDirectory, outputDirectory, archiveDirectory);
                List<UnencodedVideo> unencodedVideos = getUnencodedVideos(inputDirectory, factory);

                return encodeAndArchiveVideos(unencodedVideos);
              } catch (IOException e) {
                throw new UncheckedIOException(e);
              }
            },
            log)
        .get();
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
      logBreak();
    }
  }

  private List<UnencodedVideo> getUnencodedVideos(
      Path inputDirectory, UnencodedVideo.Factory factory) throws IOException {
    return Files.walk(inputDirectory)
        .filter(Files::isRegularFile)
        .filter(UnencodedVideo::isMp4)
        // don't include paths that represent already encoded videos
        .filter(path -> !UnencodedVideo.isEncodedMp4(path))
        .map(factory::newUnencodedVideo)
        .sorted(Comparator.comparing(video -> video.originalPath().toString()))
        .toList();
  }

  private boolean encodeAndArchiveVideos(List<UnencodedVideo> videos) {
    boolean overallSuccess = true;

    log.info("Detected {} video(s) to encode", videos.size());
    int i = 0;
    for (UnencodedVideo video : videos) {
      log.info("Detected ({}/{}): {}", ++i, videos.size(), video);
    }
    logBreak();

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

    return archiverFutures.stream()
        .map(CompletableFuture::join)
        .reduce(overallSuccess, Boolean::logicalAnd);
  }

  private static void logBreak() {
    log.info("-----------------------------------------------------------------------------------");
  }
}
