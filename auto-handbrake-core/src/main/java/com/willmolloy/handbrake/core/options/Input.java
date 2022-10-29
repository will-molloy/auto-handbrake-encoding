package com.willmolloy.handbrake.core.options;

import java.nio.file.Path;

/**
 * HandBrake input option.
 *
 * @see <a href=https://handbrake.fr/docs/en/latest/cli/command-line-reference.html>Source
 *     Options</a>
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
public sealed interface Input extends Option.KeyValueOption<Path>
    permits Internals.InputOutputOptionImpl {

  static Input of(Path path) {
    return new Internals.InputOutputOptionImpl("--input", path);
  }
}
