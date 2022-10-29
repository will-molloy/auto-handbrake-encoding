package com.willmolloy.handbrake.core.options;

/**
 * HandBrake frame rate controls.
 *
 * @see <a href=https://handbrake.fr/docs/en/latest/cli/command-line-reference.html>Video
 *     Options</a>
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
public sealed interface FrameRateControl extends Option permits Internal.OptionImpl {

  /** Constant frame rate. */
  static FrameRateControl cfr() {
    return new Internal.OptionImpl("--cfr");
  }
}
