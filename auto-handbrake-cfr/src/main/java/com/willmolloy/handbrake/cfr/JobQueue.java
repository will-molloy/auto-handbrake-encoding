package com.willmolloy.handbrake.cfr;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.IntStream.range;
import static java.util.stream.IntStream.rangeClosed;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Responsible for running the jobs.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
class JobQueue {

  private static final Logger log = LogManager.getLogger();

  private final VideoEncoder videoEncoder;
  private final VideoArchiver videoArchiver;

  JobQueue(VideoEncoder videoEncoder, VideoArchiver videoArchiver) {
    this.videoEncoder = checkNotNull(videoEncoder);
    this.videoArchiver = checkNotNull(videoArchiver);
  }

  boolean encodeAndArchiveVideos(List<UnencodedVideo> videos) {
    List<CountDownLatch> latches =
        // extra latch to avoid IOOB
        rangeClosed(0, videos.size())
            .mapToObj(
                i -> {
                  if (i == 0) {
                    // let the first job start immediately.
                    return new CountDownLatch(0);
                  } else {
                    return new CountDownLatch(1);
                  }
                })
            .toList();

    return range(0, videos.size())
        .mapToObj(i -> startJob(i, videos, latches))
        .map(CompletableFuture::join)
        .reduce(true, Boolean::logicalAnd);
  }

  private CompletableFuture<Boolean> startJob(
      int i, List<UnencodedVideo> videos, List<CountDownLatch> latches) {
    CompletableFuture<Boolean> future = new CompletableFuture<>();

    UnencodedVideo video = videos.get(i);
    CountDownLatch latch = latches.get(i);
    CountDownLatch nextLatch = latches.get(i + 1);

    Thread.ofVirtual()
        .name("job-", i + 1)
        .start(
            () -> {
              try {
                // ensure acquired in order so videos are processed in order
                latch.await();
                videoEncoder.acquire();
                nextLatch.countDown();

                log.info("Encoding ({}/{}): {}", i + 1, videos.size(), video);
                future.complete(videoEncoder.encode(video) && videoArchiver.archive(video));

              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
              }
            });

    return future;
  }
}
