package com.willmolloy.handbrake.core.options;

/**
 * HandBrake encoders.
 *
 * @see <a href=https://handbrake.fr/docs/en/latest/cli/command-line-reference.html>Video
 *     Options</a>
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
public sealed interface Encoder extends Option {

  @Override
  default String key() {
    return "--encoder";
  }

  /** H.264 (CPU). */
  record H264() implements Encoder {

    @Override
    public String value() {
      return "x264";
    }
  }

  /**
   * H.265 (CPU).
   *
   * <p>More efficient, but slow, compared to {@link H264}.
   */
  record H265() implements Encoder {

    @Override
    public String value() {
      return "x265";
    }
  }

  /**
   * H.264 (GPU).
   *
   * <p>Faster and more efficient, but lower quality, compared to {@link H264}.
   */
  record H264Gpu() implements Encoder {

    @Override
    public String value() {
      return "nvenc_h264";
    }
  }

  /**
   * H.265 (GPU).
   *
   * <p>Faster and more efficient, but lower quality, compared to {@link H265}.
   */
  record H265Gpu() implements Encoder {

    @Override
    public String value() {
      return "nvenc_h265";
    }
  }
}
