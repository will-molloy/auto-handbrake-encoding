package com.willmolloy.handbrake.core.options;

import static com.google.common.truth.Truth.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * FrameRateControlsTest.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
class FrameRateControlsTest {

  @ParameterizedTest
  @MethodSource
  void testExpectedKeys(Option frameRateControl, String value) {
    assertThat(frameRateControl.key()).isEqualTo(value);
  }

  static Stream<Arguments> testExpectedKeys() {
    return Stream.of(Arguments.of(FrameRateControls.cfr(), "--cfr"));
  }
}
