package com.willmolloy.handbrake.cfr;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.IntStream.range;
import static java.util.stream.IntStream.rangeClosed;

import com.google.common.base.Stopwatch;
import com.google.common.primitives.Booleans;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
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
      for (int i : range(0, tempFiles.size()).toArray()) {
        log.warn("Deleting ({}/{}): {}", i + 1, tempFiles.size(), tempFiles.get(i));
        Files.deleteIfExists(tempFiles.get(i));
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
    for (int i : range(0, videos.size()).toArray()) {
      log.info("Detected ({}/{}): {}", i + 1, videos.size(), videos.get(i));
    }
    logBreak();

    List<CountDownLatch> latches = new ArrayList<>();
    for (int i : rangeClosed(0, videos.size()).toArray()) {
      if (i == 0) {
        latches.add(new CountDownLatch(0));
      } else {
        latches.add(new CountDownLatch(1));
      }
    }

    List<Thread> threads = new ArrayList<>();
    boolean[] results = new boolean[videos.size()];

    for (int i : range(0, videos.size()).toArray()) {
      UnencodedVideo video = videos.get(i);
      CountDownLatch latch = latches.get(i);
      CountDownLatch nextLatch = latches.get(i + 1);

      Thread thread =
          Thread.ofVirtual()
              .name("job-", i)
              .start(
                  () -> {
                    try {
                      // ensure acquired in order so videos are processed in order
                      latch.await();
                      videoEncoder.acquire();
                      nextLatch.countDown();

                      log.info("Encoding ({}/{}): {}", i + 1, videos.size(), video);
                      results[i] = videoEncoder.encode(video) && videoArchiver.archive(video);
                    } catch (InterruptedException e) {
                      Thread.currentThread().interrupt();
                    }
                  });

      threads.add(thread);
    }

    for (Thread thread : threads) {
      try {
        thread.join();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }

    return Booleans.asList(results).stream().reduce(true, Boolean::logicalAnd);
  }

  private static void logBreak() {
    log.info("-----------------------------------------------------------------------------------");
  }
}
