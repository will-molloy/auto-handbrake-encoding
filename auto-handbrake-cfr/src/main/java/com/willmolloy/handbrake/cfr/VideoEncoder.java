package com.willmolloy.handbrake.cfr;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.Stopwatch;
import com.google.common.io.MoreFiles;
import com.willmolloy.handbrake.cfr.util.Files2;
import com.willmolloy.handbrake.core.HandBrake;
import com.willmolloy.handbrake.core.options.Encoder;
import com.willmolloy.handbrake.core.options.FrameRateControl;
import com.willmolloy.handbrake.core.options.Input;
import com.willmolloy.handbrake.core.options.Output;
import com.willmolloy.handbrake.core.options.Preset;
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

  private final ReentrantLock lock = new ReentrantLock();

  private final HandBrake handBrake;

  VideoEncoder(HandBrake handBrake) {
    this.handBrake = checkNotNull(handBrake);
  }

  /** Acquires the instance. Must call before {@link #encode}. */
  void acquire() {
    lock.lock();
  }

  /**
   * Encodes the given video.
   *
   * @param video video to encode
   * @return {@code true} if encoding was successful
   */
  boolean encode(UnencodedVideo video) {
    checkState(lock.isHeldByCurrentThread(), "Not acquired");

    Stopwatch stopwatch = Stopwatch.createStarted();
    try {
      if (Files.exists(video.encodedPath())) {
        log.warn("Encoded file ({}) already exists", video.encodedPath());
      }

      MoreFiles.createParentDirectories(video.encodedPath());

      // to avoid leaving encoded files in an 'incomplete' state, encode to a temp file in case
      // something goes wrong
      boolean handBrakeSuccessful =
          handBrake.encode(
              Input.of(video.originalPath()),
              Output.of(video.tempEncodedPath()),
              Preset.productionStandard(),
              Encoder.h264(),
              FrameRateControl.constant());

      if (!handBrakeSuccessful) {
        log.error("Error encoding: {}", video);
        return false;
      }

      release();

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
    } catch (Exception e) {
      log.error("Error encoding: {}", video, e);
      return false;
    } finally {
      // ensure unlocked (i.e. if method returns exceptionally)
      release();
      log.info("Elapsed: {}", stopwatch);
    }
  }

  private void release() {
    if (lock.isHeldByCurrentThread()) {
      lock.unlock();
    }
  }
}
