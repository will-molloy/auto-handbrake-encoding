package com.willmolloy.handbrake.core.options;

import static com.google.common.truth.Truth8.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * PresetTest.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
class PresetTest {

  @ParameterizedTest
  @MethodSource
  void testFactoriesExpectedHandBrakeCliArgs(
      Preset preset, String expectedKey, String expectedValue) {
    assertThat(preset.handBrakeCliArgs()).containsExactly(expectedKey, expectedValue).inOrder();
  }

  static Stream<Arguments> testFactoriesExpectedHandBrakeCliArgs() {
    return Stream.of(
        Arguments.of(Preset.productionMax(), "--preset", "Production Max"),
        Arguments.of(Preset.productionStandard(), "--preset", "Production Standard"));
  }
}
