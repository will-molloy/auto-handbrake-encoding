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
  public static FrameRateControl cfr() {
    return new FrameRateControl("--cfr");
  }

  /**
   * Frame rate control record.
   *
   * @param value option value
   */
  record FrameRateControl(String value) implements Option.ValueOnlyOption {}

  private FrameRateControls() {}
}
