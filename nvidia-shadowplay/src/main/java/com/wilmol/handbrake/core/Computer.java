package com.wilmol.handbrake.core;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represents the computer.
 *
 * @author <a href=https://wilmol.com>Will Molloy</a>
 */
public class Computer {

  private static final Logger log = LogManager.getLogger();

  private final Cli cli;

  public Computer(Cli cli) {
    this.cli = checkNotNull(cli);
  }

  /** Sends shutdown command. */
  public void shutdown() {
    log.info("Shutting down");
    // TODO right now its windows only?
    cli.execute(List.of("shutdown", "-s", "-t", "30"));
  }
}
