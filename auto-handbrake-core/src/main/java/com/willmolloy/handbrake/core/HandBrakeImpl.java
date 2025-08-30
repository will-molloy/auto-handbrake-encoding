package com.willmolloy.handbrake.core;

import static com.google.common.base.Preconditions.checkNotNull;

import com.willmolloy.handbrake.core.options.Input;
import com.willmolloy.handbrake.core.options.Option;
import com.willmolloy.handbrake.core.options.Output;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * HandBrake implementation.
 *
 * @see HandBrake#newInstance
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
class HandBrakeImpl implements HandBrake {

  private static final Logger log = LogManager.getLogger();

  private final Cli cli;

  HandBrakeImpl(Cli cli) {
    this.cli = checkNotNull(cli);
  }

  @Override
  public boolean encode(Input input, Output output, Option... options) {
    if (Files.exists(output.path())) {
      log.warn("Output ({}) already exists", output.path());
    }

    List<String> command =
        getCommand(Stream.concat(Stream.of(input, output), Arrays.stream(options)));

    try {
      return cli.execute(command, new HandBrakeLogger());
    } catch (Exception e) {
      log.error("Error encoding: {}", input, e);
      return false;
    }
  }

  private List<String> getCommand(Stream<Option> options) {
    return Stream.concat(Stream.of("HandBrakeCLI"), options.flatMap(Option::handBrakeCliArgs))
        .toList();
  }
}
