package com.willmolloy.handbrake.core.options;

import static com.google.common.truth.Truth8.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * FrameRateControlTest.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
class FrameRateControlTest {

  @ParameterizedTest
  @MethodSource
  void testFactoriesExpectedHandBrakeCliArgs(
      FrameRateControl frameRateControl, String expectedKey) {
    assertThat(frameRateControl.handBrakeCliArgs()).containsExactly(expectedKey).inOrder();
  }

  static Stream<Arguments> testFactoriesExpectedHandBrakeCliArgs() {
    return Stream.of(
        Arguments.of(FrameRateControl.constant(), "--cfr"),
        Arguments.of(FrameRateControl.variable(), "--vfr"),
        Arguments.of(FrameRateControl.peak(), "--pfr"));
  }
}
