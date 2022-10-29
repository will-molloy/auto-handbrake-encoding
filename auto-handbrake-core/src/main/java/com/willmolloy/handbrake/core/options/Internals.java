package com.willmolloy.handbrake.core.options;

import java.nio.file.Path;

/**
 * This package-private class allows us to hide the record classes and force users to the static
 * factory methods.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
final class Internals {

  /**
   * Option implementation.
   *
   * @param key option key
   */
  record OptionImpl(String key) implements Option {}

  /**
   * Key value option implementation.
   *
   * @param key option key
   * @param value option value
   * @param <T> value type
   */
  record KeyValueOptionImpl<T>(String key, T value) implements Option.KeyValueOption<T> {}

  /**
   * Input/output option implementation.
   *
   * @param key option key
   * @param value option value
   */
  // require a separate implementation so Input/Output type can be exposed
  // would be nice to reuse the above but doesn't seem possible
  // only going to apply to Input/Output anyway - they're the only required options
  record InputOutputOptionImpl(String key, Path value) implements Input, Output {}

  private Internals() {}
}
