package com.willmolloy.handbrake.core.options;

import java.util.stream.Stream;

/**
 * HandBrake options.
 *
 * @see <a href=https://handbrake.fr/docs/en/latest/cli/command-line-reference.html>Command line
 *     reference</a>
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
public sealed interface Option permits Input, Output, Preset, Encoder, FrameRateControl {

  Stream<String> handBrakeCliArgs();
}
