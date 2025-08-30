package com.willmolloy.handbrake.core.options;

import static com.google.common.truth.Truth.assertThat;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * InputTest.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
class InputTest {

  @Test
  void testFactoryForwardsPathAndExpectedHandBrakeCliArgs() {
    Path path = Path.of("123");

    Input input = Input.of(path);

    assertThat(input.path()).isSameInstanceAs(path);
    assertThat(input.handBrakeCliArgs()).containsExactly("--input", path.toString()).inOrder();
  }
}
