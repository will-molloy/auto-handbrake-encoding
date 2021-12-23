package com.wilmol.handbrake.nvidia.shadowplay;

import static com.google.common.truth.Truth.assertThat;

import com.wilmol.handbrake.core.Cli;
import com.wilmol.handbrake.core.HandBrake;
import org.junit.jupiter.api.Test;

/**
 * Component test.
 *
 * @author <a href=https://wilmol.com>Will Molloy</a>
 */
class AppTest {

  @Test
  void test() throws Exception {
    App app = new App(new HandBrake(new Cli()));
    assertThat(app).isNotNull();
  }
}
