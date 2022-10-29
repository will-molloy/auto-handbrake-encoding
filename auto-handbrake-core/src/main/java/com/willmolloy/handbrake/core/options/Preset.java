package com.willmolloy.handbrake.core.options;

/**
 * HandBrake presets.
 *
 * @see <a href=https://handbrake.fr/docs/en/latest/technical/official-presets.html>Official
 *     presets</a>
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
public sealed interface Preset extends Option.KeyValueOption<String>
    permits Internal.KeyStringValueOptionImpl {

  /** Production Max preset. */
  static Preset productionMax() {
    return new Internal.KeyStringValueOptionImpl("--preset", "Production Max");
  }

  /**
   * Production Standard preset.
   *
   * <p>More efficient and (IMO) indistinguishable quality, compared to {@link #productionMax()}.
   */
  static Preset productionStandard() {
    return new Internal.KeyStringValueOptionImpl("--preset", "Production Standard");
  }
}
