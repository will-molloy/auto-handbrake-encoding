package com.willmolloy.handbrake.cfr;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Stopwatch;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
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
  boolean run(Path inputDirectory, Path outputDirectory, Path archiveDirectory) throws IOException {
    log.info(
        "run(inputDirectory={}, outputDirectory={}, archiveDirectory={}) started",
        inputDirectory,
        outputDirectory,
        archiveDirectory);
    logBreak();

    Stopwatch stopwatch = Stopwatch.createStarted();
    try {
      deleteIncompleteEncodingsAndArchives(inputDirectory, outputDirectory, archiveDirectory);

      UnencodedVideo.Factory factory =
          new UnencodedVideo.Factory(inputDirectory, outputDirectory, archiveDirectory);
      List<UnencodedVideo> unencodedVideos = getUnencodedVideos(inputDirectory, factory);

      return encodeAndArchiveVideos(unencodedVideos);
    } finally {
      log.info("Elapsed: {}", stopwatch.elapsed());
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
    log.info("Detected {} video(s) to encode", videos.size());
    int i = 0;
    for (UnencodedVideo video : videos) {
      log.info("Detected ({}/{}): {}", ++i, videos.size(), video);
    }
    logBreak();

    try (ExecutorService executor =
        Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("job-", 1).factory())) {
      List<CompletableFuture<Boolean>> futures = new ArrayList<>();
      AtomicInteger jobCount = new AtomicInteger();

      for (UnencodedVideo video : videos) {
        try {
          // small sleep to block the main thread so videos are encoded in order
          // TODO why does this work exactly? add comment once understanding completely...
          // without sleep threads pile up so nondeterministic which one gets the lock next
          // but with the sleep this still happens? Difference is they call acquire in order.
          // But the lock still isn't guaranteed to release in order... TODO need fair lock?
          Thread.sleep(1);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }

        // TODO (attempting to) ensure threads acquire in order
        synchronized (this) {
          CompletableFuture<Boolean> future =
              CompletableFuture.supplyAsync(
                  () -> {
                    videoEncoder.acquire();
                    log.info(
                        "Encoding ({}/{}): {}", jobCount.incrementAndGet(), videos.size(), video);
                    return videoEncoder.encode(video) && videoArchiver.archive(video);
                  },
                  executor);
          futures.add(future);
        }
      }
      return futures.stream().map(CompletableFuture::join).reduce(Boolean::logicalAnd).orElse(true);
    }
  }

  private static void logBreak() {
    log.info("-----------------------------------------------------------------------------------");
  }
}
