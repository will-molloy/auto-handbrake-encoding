package com.willmolloy.handbrake.cfr;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Stopwatch;
import com.willmolloy.handbrake.cfr.util.Files2;
import com.willmolloy.handbrake.core.HandBrake;
import com.willmolloy.handbrake.core.options.Encoder;
import com.willmolloy.handbrake.core.options.FrameRateControl;
import com.willmolloy.handbrake.core.options.Input;
import com.willmolloy.handbrake.core.options.Output;
import com.willmolloy.handbrake.core.options.Preset;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Responsible for encoding videos.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
class VideoEncoder {

  private static final Logger log = LogManager.getLogger();

  private final ReentrantLock lock = new ReentrantLock(true);

  private final HandBrake handBrake;

  VideoEncoder(HandBrake handBrake) {
    this.handBrake = checkNotNull(handBrake);
  }

  /** Acquires the instance. Must call before {@link #encode}. */
  public void acquire() {
    log.info("acquire()");
    lock.lock();
  }

  /**
   * Encodes the given video.
   *
   * @param video video to encode
   * @return {@code true} if encoding was successful
   */
  public boolean encode(UnencodedVideo video) {
    checkArgument(lock.isHeldByCurrentThread(), "Not acquired");

    Stopwatch stopwatch = Stopwatch.createStarted();
    try {
      if (Files.exists(video.encodedPath())) {
        log.warn("Encoded file ({}) already exists", video.encodedPath());
      }

      Files.createDirectories(checkNotNull(video.encodedPath().getParent()));

      // to avoid leaving encoded files in an 'incomplete' state, encode to a temp file in case
      // something goes wrong
      boolean handBrakeSuccessful =
          handBrake.encode(
              Input.of(video.originalPath()),
              Output.of(video.tempEncodedPath()),
              Preset.productionStandard(),
              Encoder.h264(),
              FrameRateControl.constant());
      // simulate lag
      Thread.sleep(1_000);

      release();

      if (!handBrakeSuccessful) {
        log.error("Error encoding: {}", video);
        return false;
      }

      // simulate lag
      Thread.sleep(1_000);

      if (Files.exists(video.encodedPath())) {
        log.info("Verifying existing encoded file contents");
        if (!Files2.contentsSimilar(video.encodedPath(), video.tempEncodedPath())) {
          log.error("Existing encoded file contents differ. Aborting encode process");
          return false;
        }
      }

      Files.move(video.tempEncodedPath(), video.encodedPath(), StandardCopyOption.REPLACE_EXISTING);

      log.info("Encoded: {}", video.encodedPath());
      return true;
    } catch (InterruptedException | IOException | RuntimeException e) {
      log.error("Error encoding: %s".formatted(video), e);
      return false;
    } finally {
      // ensure unlocked (i.e. if method returns exceptionally)
      release();
      log.info("Elapsed: {}", stopwatch.elapsed());
    }
  }

  private void release() {
    log.info("release()");
    if (lock.isHeldByCurrentThread()) {
      lock.unlock();
    }
  }
}
