package com.willmolloy.handbrake.core.options;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * InputTest.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
class InputTest {

  @Test
  void testFactoryForwardsPathAndExpectedKey() {
    Path path = Path.of("123");

    Input input = Input.of(path);

    assertThat(input.key()).isEqualTo("--input");
    assertThat(input.value()).isSameInstanceAs(path);
  }
}
