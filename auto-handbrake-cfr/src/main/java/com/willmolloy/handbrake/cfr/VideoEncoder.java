package com.willmolloy.handbrake.cfr;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Stopwatch;
import com.willmolloy.handbrake.cfr.util.Files2;
import com.willmolloy.handbrake.core.HandBrake;
import com.willmolloy.handbrake.core.options.Encoder;
import com.willmolloy.handbrake.core.options.FrameRateControl;
import com.willmolloy.handbrake.core.options.Input;
import com.willmolloy.handbrake.core.options.Output;
import com.willmolloy.handbrake.core.options.Preset;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.Semaphore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Responsible for encoding videos.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
class VideoEncoder {

  private static final Logger log = LogManager.getLogger();

  private static final int MAX_CONCURRENT_ENCODES = 1;

  private final Semaphore semaphore = new Semaphore(MAX_CONCURRENT_ENCODES);

  private final HandBrake handBrake;

  VideoEncoder(HandBrake handBrake) {
    this.handBrake = checkNotNull(handBrake);
  }

  /**
   * Acquires the instance to ensure within concurrent encode limit.
   *
   * <p>Must call before {@link #encode}.
   */
  // TODO ugly external synchronisation... how to deal with this??
  public void acquire() {
    try {
      semaphore.acquire();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Encodes the given video.
   *
   * @param video video to encode
   * @return {@code true} if encoding was successful
   */
  public boolean encode(UnencodedVideo video) {
    // TODO ensure acquired() first somehow - need lock rather than semaphore??

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
      // TODO what if exception is thrown? Would not release
      semaphore.release();

      if (handBrakeSuccessful) {
        if (Files.exists(video.encodedPath())) {
          log.info("Verifying existing encoded file contents");
          if (!Files2.contentsSimilar(video.encodedPath(), video.tempEncodedPath())) {
            log.error("Existing encoded file contents differ. Aborting encode process");
            return false;
          }
        }
        Files.move(
            video.tempEncodedPath(), video.encodedPath(), StandardCopyOption.REPLACE_EXISTING);
        log.info("Encoded: {}", video.encodedPath());
        return true;
      } else {
        log.error("Error encoding: {}", video);
        return false;
      }
    } catch (Exception e) {
      log.error("Error encoding: %s".formatted(video), e);
      return false;
    } finally {
      log.info("Elapsed: {}", stopwatch.elapsed());
    }
  }
}
