package com.willmolloy.handbrake.cfr;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.IntStream.range;
import static java.util.stream.IntStream.rangeClosed;

import com.google.common.primitives.Booleans;
import java.util.List;
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
        rangeClosed(0, videos.size())
            .mapToObj(
                i -> {
                  if (i == 0) {
                    return new CountDownLatch(0);
                  } else {
                    return new CountDownLatch(1);
                  }
                })
            .toList();

    boolean[] results = new boolean[videos.size()];

    List<Thread> threads =
        range(0, videos.size()).mapToObj(i -> startJob(i, videos, latches, results)).toList();

    for (Thread thread : threads) {
      try {
        thread.join();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }

    return Booleans.asList(results).stream().reduce(true, Boolean::logicalAnd);
  }

  private Thread startJob(
      int i, List<UnencodedVideo> videos, List<CountDownLatch> latches, boolean[] results) {
    return Thread.ofVirtual()
        .name("job-", i + 1)
        .start(
            () -> {
              try {
                UnencodedVideo video = videos.get(i);
                CountDownLatch latch = latches.get(i);
                CountDownLatch nextLatch = latches.get(i + 1);

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
  }
}
