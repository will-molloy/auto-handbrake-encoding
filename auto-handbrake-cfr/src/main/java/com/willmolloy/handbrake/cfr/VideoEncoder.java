package com.willmolloy.handbrake.cfr;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Stopwatch;
import com.willmolloy.handbrake.core.HandBrake;
import com.willmolloy.handbrake.core.options.Encoders;
import com.willmolloy.handbrake.core.options.FrameRateControls;
import com.willmolloy.handbrake.core.options.Input;
import com.willmolloy.handbrake.core.options.Output;
import com.willmolloy.handbrake.core.options.Presets;
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
      log.info("Encoding: {} -> {}", video.originalPath(), video.encodedPath());

      if (Files.exists(video.encodedPath())) {
        log.warn("Encoded file ({}) already exists", video.encodedPath());
        return true;
      }

      Files.createDirectories(checkNotNull(video.encodedPath().getParent()));

      // to avoid leaving encoded files in an 'incomplete' state, encode to a temp file in case
      // something goes wrong
      boolean encodeSuccessful =
          handBrake.encode(
              Input.of(video.originalPath()),
              Output.of(video.tempEncodedPath()),
              Presets.productionStandard(),
              Encoders.h264(),
              FrameRateControls.cfr());

      if (encodeSuccessful) {
        Files.move(video.tempEncodedPath(), video.encodedPath());
        log.info("Encoded: {} - elapsed: {}", video.encodedPath(), stopwatch.elapsed());
        return true;
      } else {
        log.error("Error encoding: {}", video);
        return false;
      }
    } catch (Exception e) {
      log.error("Error encoding: %s".formatted(video), e);
      return false;
    }
  }
}
