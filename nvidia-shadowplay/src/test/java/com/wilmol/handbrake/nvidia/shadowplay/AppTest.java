package com.wilmol.handbrake.nvidia.shadowplay;

import static com.google.common.truth.Truth.assertThat;

import com.wilmol.handbrake.core.Cli;
import com.wilmol.handbrake.core.HandBrake;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * AppTest.
 *
 * @author <a href=https://wilmol.com>Will Molloy</a>
 */
@ExtendWith(MockitoExtension.class)
class AppTest {

  @Mock private HandBrake handBrake;

  @Mock private Cli cli;

  @InjectMocks private App app;

  @Test
  void test() {
    assertThat(app).isNotNull();
  }
}
