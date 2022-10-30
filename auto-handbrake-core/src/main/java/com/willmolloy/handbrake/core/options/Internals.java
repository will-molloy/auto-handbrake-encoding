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
  record OptionImpl(String key) implements FrameRateControl {}

  /**
   * Key value option implementation.
   *
   * @param key option key
   * @param value option value
   */
  record KeyStringValueOptionImpl(String key, String value) implements Preset, Encoder {}

  /**
   * Key value option implementation.
   *
   * @param key option key
   * @param value option value
   */
  // TODO anyway to reuse a generic KeyValueOptionImpl<T>?
  //  problem is the interfaces (Input, Preset, etc.) use different generic params
  record KeyPathValueOptionImpl(String key, Path value) implements Input, Output {}

  private Internals() {}
}
