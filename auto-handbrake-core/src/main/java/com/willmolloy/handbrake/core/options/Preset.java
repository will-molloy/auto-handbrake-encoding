package com.willmolloy.handbrake.core.options;

/**
 * HandBrake presets.
 *
 * @see <a href=https://handbrake.fr/docs/en/latest/technical/official-presets.html>Official
 *     presets</a>
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
public sealed interface Preset extends Option {

  @Override
  default String key() {
    return "--preset";
  }

  /** Production Max preset. */
  record ProductionMax() implements Preset {

    @Override
    public String value() {
      return "Production Max";
    }
  }

  /**
   * Production Standard preset.
   *
   * <p>More efficient and (IMO) indistinguishable quality, compared to {@link ProductionMax}.
   */
  record ProductionStandard() implements Preset {

    @Override
    public String value() {
      return "Production Standard";
    }
  }
}
