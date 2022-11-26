package com.willmolloy.handbrake.cfr;

import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * AppTest.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
@ExtendWith(MockitoExtension.class)
class AppTest {

  @Mock private DirectoryScanner mockDirectoryScanner;
  @Mock private JobQueue mockJobQueue;
  @InjectMocks private App app;

  @Test
  void orchestratesScanningAndProcessing() throws IOException {
    // Given
    List<UnencodedVideo> videos = List.of();
    when(mockDirectoryScanner.scan()).thenReturn(videos);

    // When
    app.run();

    // Then
    verify(mockDirectoryScanner).scan();
    verify(mockJobQueue).process(same(videos));
  }
}
