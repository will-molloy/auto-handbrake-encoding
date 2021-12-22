package com.wilmol.handbrake.nvidia.shadowplay;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Core app runner.
 *
 * @author <a href=https://wilmol.com>Will Molloy</a>
 */
class App {

  private static final Logger log = LogManager.getLogger();

  void run() {
    log.info("Hello world");
  }

  public static void main(String[] args) {
    new App().run();
  }
}
