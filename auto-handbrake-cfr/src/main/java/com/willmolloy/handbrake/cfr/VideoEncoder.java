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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Responsible for encoding videos.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
class VideoEncoder {

  private static final Logger log = LogManager.getLogger();

  private final Executor executor =
      Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("video-encoder-", 1).factory());

  private final HandBrake handBrake;

  VideoEncoder(HandBrake handBrake) {
    this.handBrake = checkNotNull(handBrake);
  }

  /**
   * Encodes the given video.
   *
   * @param video video to encode
   * @return {@code true} if encoding was successful
   */
  public boolean encode(UnencodedVideo video) {
    // atm running encode on another thread purely to make the logs easier to follow (could set up
    // structured logging?)
    // TODO parts of the encode process would benefit from async - e.g. verifying temp file contents
    return CompletableFuture.supplyAsync(() -> doEncode(video), executor).join();
  }

  private boolean doEncode(UnencodedVideo video) {
    Stopwatch stopwatch = Stopwatch.createStarted();
    try {
      log.debug("Encoding: {} -> {}", video.originalPath(), video.encodedPath());

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

      if (handBrakeSuccessful) {
        if (Files.exists(video.encodedPath())) {
          log.info("Verifying encoded file contents");
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
