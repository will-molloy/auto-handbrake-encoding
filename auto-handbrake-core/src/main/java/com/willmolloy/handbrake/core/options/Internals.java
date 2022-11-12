package com.willmolloy.handbrake.core.options;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * Since the interfaces are sealed, we don't want users creating their own {@link Option} class. And
 * because this class is package-private, it hides the implementations and forces users to the
 * static factory methods.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
final class Internals {

  /**
   * Generic option implementation.
   *
   * @param optionArgs HandBrakeCLI option args
   */
  record OptionImpl(List<String> optionArgs) implements Preset, Encoder, FrameRateControl {
    OptionImpl(String... optionArgs) {
      this(List.of(optionArgs));
    }

    @Override
    public Stream<String> handBrakeCliArgs() {
      return optionArgs.stream();
    }
  }

  /**
   * Option implementation specific to input/output.
   *
   * @param key HandBrakeCLI option key
   * @param path HandBrakeCLI option value (a path)
   */
  // Input/Output need their own implementation as they expose the Path
  // could change the HandBrake interface to simply accept Path so this isn't required, but that API
  // is nicer with a custom interface IMO
  record InputOutputOptionImpl(String key, Path path) implements Input, Output {
    @Override
    public Stream<String> handBrakeCliArgs() {
      return Stream.of(key, path.toString());
    }
  }

  private Internals() {}
}
