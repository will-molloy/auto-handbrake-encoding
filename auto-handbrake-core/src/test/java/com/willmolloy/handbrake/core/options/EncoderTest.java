package com.willmolloy.handbrake.core.options;

import static com.google.common.truth.Truth.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * EncoderTest.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
class EncoderTest {

  @ParameterizedTest
  @MethodSource
  void testFactoriesExpectedKeysAndValues(Encoder encoder, String key, String value) {
    assertThat(encoder.key()).isEqualTo(key);
    assertThat(encoder.value()).isEqualTo(value);
  }

  static Stream<Arguments> testFactoriesExpectedKeysAndValues() {
    return Stream.of(
        Arguments.of(Encoder.h264(), "--encoder", "x264"),
        Arguments.of(Encoder.h265(), "--encoder", "x265"),
        Arguments.of(Encoder.h264Gpu(), "--encoder", "nvenc_h264"),
        Arguments.of(Encoder.h265Gpu(), "--encoder", "nvenc_h265"));
  }
}
