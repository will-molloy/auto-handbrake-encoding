package com.willmolloy.handbrake.cfr;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.primitives.Booleans;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;
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

  boolean process(List<UnencodedVideo> videos) {
    List<CountDownLatch> latches =
        // extra latch to avoid IOOB
        IntStream.rangeClosed(0, videos.size())
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

    boolean[] results = new boolean[videos.size()];

    List<Thread> threads =
        IntStream.range(0, videos.size())
            .mapToObj(i -> startJob(i, videos, latches, results))
            .toList();

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
    UnencodedVideo video = videos.get(i);
    CountDownLatch latch = latches.get(i);
    CountDownLatch nextLatch = latches.get(i + 1);

    return Thread.ofVirtual()
        .name("job-", i + 1)
        .start(
            () -> {
              try {
                // latch ensures videos are processed in order
                // while lock ensures a single instance of handbrake is running (at one time)

                // all jobs await the latch, until the previous job has started
                // then a single job awaits the lock, until the previous job has finished encoding

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
