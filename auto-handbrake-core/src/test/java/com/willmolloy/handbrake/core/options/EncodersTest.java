package com.willmolloy.handbrake.core.options;

import static com.google.common.truth.Truth.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * EncodersTest.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
class EncodersTest {

  @ParameterizedTest
  @MethodSource
  void testExpectedKeysAndValues(Option.KeyValueOption<String> encoder, String key, String value) {
    assertThat(encoder.key()).isEqualTo(key);
    assertThat(encoder.value()).isEqualTo(value);
  }

  static Stream<Arguments> testExpectedKeysAndValues() {
    return Stream.of(
        Arguments.of(Encoders.h264(), "--encoder", "x264"),
        Arguments.of(Encoders.h265(), "--encoder", "x265"),
        Arguments.of(Encoders.h264Gpu(), "--encoder", "nvenc_h264"),
        Arguments.of(Encoders.h265Gpu(), "--encoder", "nvenc_h265"));
  }
}
