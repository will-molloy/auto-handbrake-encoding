package com.willmolloy.handbrake.core;

import com.willmolloy.handbrake.core.options.Input;
import com.willmolloy.handbrake.core.options.Option;
import com.willmolloy.handbrake.core.options.Output;

/**
 * HandBrake interface.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
public interface HandBrake {

  /**
   * Runs HandBrake encoding.
   *
   * @param input input file
   * @param output output file
   * @param options HandBrake options
   * @return {@code true} if encoding was successful
   */
  boolean encode(Input input, Output output, Option... options);

  static HandBrake newInstance() {
    return new HandBrakeImpl(new Cli(ProcessBuilder::new));
  }
}
