package com.willmolloy.handbrake.core.options;

/**
 * HandBrake presets.
 *
 * @see <a href=https://handbrake.fr/docs/en/latest/technical/official-presets.html>Official
 *     presets</a>
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
public final class Presets {
  private Presets() {}

  /**
   * Preset record.
   *
   * @param key option key
   * @param value option value
   */
  record Preset(String key, String value) implements Option {
    Preset(String value) {
      this("--preset", value);
    }
  }

  /** Production Max preset. */
  public static Preset productionMax() {
    return new Preset("Production Max");
  }

  /**
   * Production Standard preset.
   *
   * <p>More efficient and (IMO) indistinguishable quality, compared to {@link #productionMax()}.
   */
  public static Preset productionStandard() {
    return new Preset("Production Standard");
  }
}
