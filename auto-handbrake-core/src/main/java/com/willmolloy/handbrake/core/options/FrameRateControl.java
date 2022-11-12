package com.willmolloy.handbrake.core.options;

/**
 * HandBrake frame rate controls.
 *
 * @see <a href=https://handbrake.fr/docs/en/latest/cli/command-line-reference.html>Video
 *     Options</a>
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
public sealed interface FrameRateControl extends Option permits Internals.OptionImpl {

  /** Constant frame rate. */
  static FrameRateControl constant() {
    return new Internals.OptionImpl("--cfr");
  }

  /** Variable frame rate. */
  static FrameRateControl variable() {
    return new Internals.OptionImpl("--vfr");
  }

  /** Peak frame rate. */
  static FrameRateControl peak() {
    return new Internals.OptionImpl("--pfr");
  }
}
