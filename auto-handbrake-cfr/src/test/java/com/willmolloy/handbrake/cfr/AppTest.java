package com.willmolloy.handbrake.cfr;

import static org.mockito.Mockito.inOrder;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
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
  void scansAndEncodesVideos() throws IOException {
    // When
    app.run();

    // Then
    InOrder inOrder = inOrder(mockDirectoryScanner, mockJobQueue);
    inOrder.verify(mockDirectoryScanner).deleteIncompleteEncodingsAndArchives();
    inOrder.verify(mockDirectoryScanner).getUnencodedVideos();
    inOrder.verify(mockJobQueue).encodeAndArchiveVideos(List.of());
  }
}
