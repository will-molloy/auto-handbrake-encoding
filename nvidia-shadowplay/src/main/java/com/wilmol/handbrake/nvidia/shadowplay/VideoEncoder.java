package com.wilmol.handbrake.nvidia.shadowplay;

import static com.google.common.base.Preconditions.checkNotNull;

import com.wilmol.handbrake.core.HandBrake;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Responsible for encoding videos.
 *
 * @author <a href=https://wilmol.com>Will Molloy</a>
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
    try {
      log.info("Encoding: {} -> {}", video.originalPath(), video.encodedPath());

      Files.createDirectories(checkNotNull(video.encodedPath().getParent()));

      // to avoid leaving encoded files in an 'incomplete' state, encode to a temp file in case
      // something goes wrong
      boolean encodeSuccessful = handBrake.encode(video.originalPath(), video.tempEncodedPath());

      if (encodeSuccessful) {
        Files.move(video.tempEncodedPath(), video.encodedPath());
        log.info("Encoded: {} -> {}", video.originalPath(), video.encodedPath());
        return true;
      } else {
        log.error("Failed to encode: {}", video.originalPath());
        return false;
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
