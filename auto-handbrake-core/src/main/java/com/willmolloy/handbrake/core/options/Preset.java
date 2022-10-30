package com.willmolloy.handbrake.core.options;

/**
 * HandBrake presets.
 *
 * @see <a href=https://handbrake.fr/docs/en/latest/technical/official-presets.html>Official
 *     presets</a>
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
public sealed interface Preset extends Option.KeyValueOption<String>
    permits Internals.KeyStringValueOptionImpl {

  /** Production Max preset. */
  static Preset productionMax() {
    return preset("Production Max");
  }

  /**
   * Production Standard preset.
   *
   * <p>More efficient and (IMO) indistinguishable quality, compared to {@link #productionMax()}.
   */
  static Preset productionStandard() {
    return preset("Production Standard");
  }

  private static Preset preset(String value) {
    return new Internals.KeyStringValueOptionImpl("--preset", value);
  }
}
