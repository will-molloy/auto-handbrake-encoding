package com.willmolloy.handbrake.core.options;

/**
 * HandBrake encoders.
 *
 * @see <a href=https://handbrake.fr/docs/en/latest/cli/command-line-reference.html>Video
 *     Options</a>
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
public final class Encoders {
  private Encoders() {}

  /**
   * Encoder record.
   *
   * @param key option key
   * @param value option value
   */
  record Encoder(String key, String value) implements Option.KeyValueOption {
    Encoder(String value) {
      this("--encoder", value);
    }
  }

  /** H.264 (CPU). */
  public static Encoder h264() {
    return new Encoder("x264");
  }

  /**
   * H.265 (CPU).
   *
   * <p>More efficient, but slow, compared to {@link #h264()}.
   */
  public static Encoder h265() {
    return new Encoder("x265");
  }

  /**
   * H.264 (GPU).
   *
   * <p>Faster and more efficient, but lower quality, compared to {@link #h264()}.
   */
  public static Encoder h264Gpu() {
    return new Encoder("nvenc_h264");
  }

  /**
   * H.265 (GPU).
   *
   * <p>Faster and more efficient, but lower quality, compared to {@link #h265()}.
   */
  public static Encoder h265Gpu() {
    return new Encoder("nvenc_h265");
  }
}
