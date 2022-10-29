package com.willmolloy.handbrake.core.options;

/**
 * HandBrake encoders.
 *
 * @see <a href=https://handbrake.fr/docs/en/latest/cli/command-line-reference.html>Video
 *     Options</a>
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
public final class Encoders {

  /** H.264 (CPU). */
  public static Option h264() {
    return new Internals.KeyValueOptionImpl<>("--encoder", "x264");
  }

  /**
   * H.265 (CPU).
   *
   * <p>More efficient, but very slow, compared to {@link #h264()}.
   */
  public static Option h265() {
    return new Internals.KeyValueOptionImpl<>("--encoder", "x265");
  }

  /**
   * H.264 (GPU).
   *
   * <p>Faster and more efficient, but lower quality, compared to {@link #h264()}.
   */
  public static Option h264Gpu() {
    return new Internals.KeyValueOptionImpl<>("--encoder", "nvenc_h264");
  }

  /**
   * H.265 (GPU).
   *
   * <p>Much faster and more efficient, but lower quality, compared to {@link #h265()}.
   */
  public static Option h265Gpu() {
    return new Internals.KeyValueOptionImpl<>("--encoder", "nvenc_h265");
  }

  private Encoders() {}
}
