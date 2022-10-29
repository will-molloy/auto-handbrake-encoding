package com.willmolloy.handbrake.core.options;

import java.nio.file.Path;

/**
 * HandBrake output option.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
public sealed interface Output extends Option permits Output.Internal.OutputImpl {
  String key();

  Path path();

  static Output of(Path path) {
    return new Internal.OutputImpl(path);
  }

  /** Internal detail, don't reference. */
  // can't make this private unfortunately
  final class Internal {

    /**
     * Output record.
     *
     * @param key option key
     * @param path option value
     */
    record OutputImpl(String key, Path path) implements Output {
      private OutputImpl(Path path) {
        this("--output", path);
      }
    }

    private Internal() {}
  }
}
