package com.willmolloy.handbrake.core.options;

import java.nio.file.Path;

/**
 * HandBrake input option.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
public sealed interface Input extends Option permits Input.Internal.InputImpl {
  String key();

  Path path();

  static Input of(Path path) {
    return new Internal.InputImpl(path);
  }

  /** Internal detail, don't reference. */
  // can't make this private unfortunately
  final class Internal {

    /**
     * Input record.
     *
     * @param key option key
     * @param path option value
     */
    record InputImpl(String key, Path path) implements Input {
      private InputImpl(Path path) {
        this("--input", path);
      }
    }

    private Internal() {}
  }
}
