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
  void testExpectedKeysAndValues(Preset preset, String key, String value) {
    assertThat(preset.key()).isEqualTo(key);
    assertThat(preset.value()).isEqualTo(value);
  }

  static Stream<Arguments> testExpectedKeysAndValues() {
    return Stream.of(
        Arguments.of(new Preset.ProductionMax(), "--preset", "Production Max"),
        Arguments.of(new Preset.ProductionStandard(), "--preset", "Production Standard"));
  }
}
