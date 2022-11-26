package com.willmolloy.handbrake.cfr;

import com.google.common.io.Resources;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * AppTest.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
@SuppressFBWarnings("UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
@ExtendWith(MockitoExtension.class)
class AppTest {

  private Path testDirectory;
  private Path inputDirectory;
  private Path outputDirectory;
  private Path archiveDirectory;
  private Path testVideo;

  @Mock private VideoEncoder mockVideoEncoder;
  @Mock private VideoArchiver mockVideoArchiver;
  @InjectMocks private App app;

  @BeforeEach
  void setUp() throws Exception {
    testDirectory = Path.of(this.getClass().getSimpleName());
    inputDirectory = testDirectory.resolve("input");
    outputDirectory = testDirectory.resolve("output");
    archiveDirectory = testDirectory.resolve("archive");

    testVideo = Path.of(Resources.getResource("Big_Buck_Bunny_360_10s_1MB.mp4").toURI());

    Files.createDirectories(inputDirectory);
    Files.createDirectories(outputDirectory);
    Files.createDirectories(archiveDirectory);
  }

  @AfterEach
  void tearDown() throws IOException {
    FileUtils.deleteDirectory(testDirectory.toFile());
  }
}
