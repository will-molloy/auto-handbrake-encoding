package com.willmolloy.handbrake.core.options;

import static com.google.common.truth.Truth.assertThat;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * OutputTest.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
class OutputTest {

  @Test
  void testFactoryForwardsPathAndExpectedHandBrakeCliArgs() {
    Path path = Path.of("123");

    Output output = Output.of(path);

    assertThat(output.path()).isSameInstanceAs(path);
    assertThat(output.handBrakeCliArgs()).containsExactly("--output", path.toString()).inOrder();
  }
}
