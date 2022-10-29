package com.willmolloy.handbrake.core.options;

/**
 * HandBrake options.
 *
 * @see <a href=https://handbrake.fr/docs/en/latest/cli/command-line-reference.html>Command line
 *     reference</a>
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
public sealed interface Option {

  /** Option with key and value. */
  sealed interface KeyValueOption extends Option permits Encoders.Encoder, Presets.Preset {

    String key();

    String value();
  }

  /** Option with value only. */
  sealed interface ValueOnlyOption extends Option permits FrameRateControls.FrameRateControl {
    String value();
  }
}
