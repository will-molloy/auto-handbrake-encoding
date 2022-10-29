package com.willmolloy.handbrake.core.options;

/**
 * HandBrake presets.
 *
 * @see <a href=https://handbrake.fr/docs/en/latest/technical/official-presets.html>Official
 *     presets</a>
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
public final class Presets {

  /** Production Max preset. */
  public static Option productionMax() {
    return new Internals.KeyValueOptionImpl<>("--preset", "Production Max");
  }

  /**
   * Production Standard preset.
   *
   * <p>More efficient and (IMO) indistinguishable quality, compared to {@link #productionMax()}.
   */
  public static Option productionStandard() {
    return new Internals.KeyValueOptionImpl<>("--preset", "Production Standard");
  }

  private Presets() {}
}
