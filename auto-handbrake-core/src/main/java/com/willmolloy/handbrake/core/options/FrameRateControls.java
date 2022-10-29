package com.willmolloy.handbrake.core.options;

/**
 * HandBrake frame rate controls.
 *
 * @see <a href=https://handbrake.fr/docs/en/latest/cli/command-line-reference.html>Video
 *     Options</a>
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
public final class FrameRateControls {

  /** Constant frame rate. */
  public static Option cfr() {
    return new Internals.OptionImpl("--cfr");
  }

  private FrameRateControls() {}
}
