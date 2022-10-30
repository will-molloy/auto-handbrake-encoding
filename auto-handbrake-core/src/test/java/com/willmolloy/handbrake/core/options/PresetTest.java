package com.willmolloy.handbrake.core.options;

import static com.google.common.truth.Truth.assertThat;

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
  void testFactoriesExpectedKeysAndValues(Preset preset, String key, String value) {
    assertThat(preset.key()).isEqualTo(key);
    assertThat(preset.value()).isEqualTo(value);
  }

  static Stream<Arguments> testFactoriesExpectedKeysAndValues() {
    return Stream.of(
        Arguments.of(Preset.productionMax(), "--preset", "Production Max"),
        Arguments.of(Preset.productionStandard(), "--preset", "Production Standard"));
  }
}
