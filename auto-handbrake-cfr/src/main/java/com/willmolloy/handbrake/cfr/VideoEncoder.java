package com.willmolloy.handbrake.cfr;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Stopwatch;
import com.willmolloy.handbrake.core.HandBrake;
import com.willmolloy.handbrake.core.options.Encoder;
import com.willmolloy.handbrake.core.options.FrameRateControl;
import com.willmolloy.handbrake.core.options.Input;
import com.willmolloy.handbrake.core.options.Output;
import com.willmolloy.handbrake.core.options.Preset;
import java.nio.file.Files;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Responsible for encoding videos.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
public class VideoEncoder {

  private static final Logger log = LogManager.getLogger();

  private final HandBrake handBrake;

  public VideoEncoder(HandBrake handBrake) {
    this.handBrake = checkNotNull(handBrake);
  }

  /**
   * Encodes the given video.
   *
   * @param video video to encode
   * @return {@code true} if encoding was successful
   */
  public boolean encode(UnencodedVideo video) {
    Stopwatch stopwatch = Stopwatch.createStarted();
    try {
      log.debug("Encoding: {} -> {}", video.originalPath(), video.encodedPath());

      if (Files.exists(video.encodedPath())) {
        log.error("Encoded file ({}) already exists. Aborting", video.encodedPath());
        return false;
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
        Files.move(video.tempEncodedPath(), video.encodedPath());
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
