package com.willmolloy.handbrake.adhoc;

import static com.google.common.truth.Truth8.assertThat;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * FileRenamerTest.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
@SuppressFBWarnings("UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
class FileRenamerTest {

  private Path testDirectory;

  private final FileRenamer fileRenamer = new FileRenamer();

  @BeforeEach
  void setUp() {
    testDirectory = Path.of("FileRenamerTest");
  }

  @AfterEach
  void tearDown() throws IOException {
    FileUtils.deleteDirectory(testDirectory.toFile());
  }

  @Test
  void renamesFilesWithMatchingSuffix() throws IOException {
    // Given
    Files.createDirectories(testDirectory.resolve("Nested"));
    Files.createFile(testDirectory.resolve("video1 - Old.mp4"));
    Files.createFile(testDirectory.resolve("My Vid2 - Old.mp4"));
    Files.createFile(testDirectory.resolve("Nested/Video3 - Old.mp4"));

    // When
    fileRenamer.changeSuffixes(testDirectory, " - Old.mp4", " - New.mp4");

    // Then
    assertThat(Files.walk(testDirectory).filter(Files::isRegularFile))
        .containsExactly(
            testDirectory.resolve("video1 - New.mp4"),
            testDirectory.resolve("My Vid2 - New.mp4"),
            testDirectory.resolve("Nested/Video3 - New.mp4"));
  }

  @Test
  void ignoresFilesNotMatchingSuffix() throws IOException {
    // Given
    Files.createDirectories(testDirectory.resolve("Nested"));
    Files.createFile(testDirectory.resolve("video1 - Archived.mp4"));
    Files.createFile(testDirectory.resolve("My Vid2 - New.mp4"));
    Files.createFile(testDirectory.resolve("Nested/Video3.mp4"));

    // When
    fileRenamer.changeSuffixes(testDirectory, " - Archived.mp4", ".mp4");

    // Then
    assertThat(Files.walk(testDirectory).filter(Files::isRegularFile))
        .containsExactly(
            testDirectory.resolve("video1.mp4"),
            testDirectory.resolve("My Vid2 - New.mp4"),
            testDirectory.resolve("Nested/Video3.mp4"));
  }
}
