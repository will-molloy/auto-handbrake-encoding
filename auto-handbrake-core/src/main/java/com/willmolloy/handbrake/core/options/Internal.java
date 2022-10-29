package com.willmolloy.handbrake.core.options;

import java.nio.file.Path;

/**
 * This package-private class allows us to hide the record classes and force users to the static
 * factory methods defined in the sealed interfaces.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
final class Internal {

  /**
   * Input record.
   *
   * @param key option key
   * @param value option value
   */
  record InputImpl(String key, Path value) implements Input {
    InputImpl(Path value) {
      this("--input", value);
    }
  }

  /**
   * Output record.
   *
   * @param key option key
   * @param value option value
   */
  record OutputImpl(String key, Path value) implements Output {}

  /**
   * Preset record.
   *
   * @param key option key
   * @param value option value
   */
  record PresetImpl(String key, String value) implements Preset {
    PresetImpl(String value) {
      this("--preset", value);
    }
  }

  /**
   * Encoder record.
   *
   * @param key option key
   * @param value option value
   */
  record EncoderImpl(String key, String value) implements Encoder {
    EncoderImpl(String value) {
      this("--encoder", value);
    }
  }

  /**
   * Frame rate control record.
   *
   * @param key option key
   */
  record FrameRateControlImpl(String key) implements FrameRateControl {}

  private Internal() {}
}
