package com.willmolloy.handbrake.core.options;

/**
 * HandBrake options.
 *
 * @see <a href=https://handbrake.fr/docs/en/latest/cli/command-line-reference.html>Command line
 *     reference</a>
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
public sealed interface Option permits Internals.OptionImpl, Option.KeyValueOption {

  String key();

  /**
   * Option with key and value.
   *
   * @param <T> value type
   */
  sealed interface KeyValueOption<T> extends Option
      permits Internals.KeyValueOptionImpl, Input, Output {

    T value();
  }
}
