package com.wilmol.handbrake.nvidia.shadowplay;

import com.google.common.io.Resources;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * Component test.
 *
 * @author <a href=https://wilmol.com>Will Molloy</a>
 */
class AppTest {

  private final App app = new App();

  @Test
  void test() throws Exception {
    Path recordingsPath = Path.of(Resources.getResource("AppTest/dummy.mp4").toURI());

    app.run(recordingsPath, false, false);
  }
}
