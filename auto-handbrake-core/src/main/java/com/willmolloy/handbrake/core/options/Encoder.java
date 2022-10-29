package com.willmolloy.handbrake.core.options;

/**
 * HandBrake encoders.
 *
 * @see <a href=https://handbrake.fr/docs/en/latest/cli/command-line-reference.html>Video
 *     Options</a>
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
public sealed interface Encoder extends Option.KeyValueOption<String> permits Internal.EncoderImpl {

  /** H.264 (CPU). */
  static Encoder h264() {
    return new Internal.EncoderImpl("x264");
  }

  /**
   * H.265 (CPU).
   *
   * <p>More efficient, but slow, compared to {@link #h264()}.
   */
  static Encoder h265() {
    return new Internal.EncoderImpl("x265");
  }

  /**
   * H.264 (GPU).
   *
   * <p>Faster and more efficient, but lower quality, compared to {@link #h264()}.
   */
  static Encoder h264Gpu() {
    return new Internal.EncoderImpl("nvenc_h264");
  }

  /**
   * H.265 (GPU).
   *
   * <p>Faster and more efficient, but lower quality, compared to {@link #h265()}.
   */
  static Encoder h265Gpu() {
    return new Internal.EncoderImpl("nvenc_h265");
  }
}
