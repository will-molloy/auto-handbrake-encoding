package com.wilmol.handbrake.nvidia.shadowplay;

import static com.google.common.truth.Truth8.assertThat;

import com.google.common.io.Resources;
import com.google.common.truth.StreamSubject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * AppIntegrationTest.
 *
 * @author <a href=https://wilmol.com>Will Molloy</a>
 */
class AppIntegrationTest {

  private Path testDirectory;
  private Path inputDirectory;
  private Path outputDirectory;
  private Path archiveDirectory;
  private Path testVideo;

  @BeforeEach
  void setUp() throws Exception {
    testDirectory = Path.of(this.getClass().getSimpleName());
    inputDirectory = testDirectory.resolve("Videos");
    outputDirectory = testDirectory.resolve("Encoded Videos");
    archiveDirectory = testDirectory.resolve("Archived Videos");

    testVideo = Path.of(Resources.getResource("test-video.mp4").toURI());

    Files.createDirectories(inputDirectory);
    Files.createDirectories(outputDirectory);
    Files.createDirectories(archiveDirectory);
  }

  @AfterEach
  void tearDown() throws IOException {
    FileUtils.deleteDirectory(testDirectory.toFile());
  }

  @Test
  void happyPath() throws IOException {
    // Given
    Files.createDirectories(inputDirectory.resolve("NestedFolder"));
    Files.copy(testVideo, inputDirectory.resolve("video1.mp4"));
    Files.copy(testVideo, inputDirectory.resolve("video2.mp4"));
    Files.copy(testVideo, inputDirectory.resolve("NestedFolder/video3.mp4"));

    // When
    runApp();

    // Then
    assertThatTestDirectory()
        .containsExactly(
            outputDirectory.resolve("video1 - CFR.mp4"),
            outputDirectory.resolve("video2 - CFR.mp4"),
            outputDirectory.resolve("NestedFolder/video3 - CFR.mp4"),
            //
            archiveDirectory.resolve("video1 - Archived.mp4"),
            archiveDirectory.resolve("video2 - Archived.mp4"),
            archiveDirectory.resolve("NestedFolder/video3 - Archived.mp4"));
  }

  @Test
  void incompleteEncodingsAndArchives() throws IOException {
    // Given
    Files.createDirectories(inputDirectory.resolve("NestedFolder"));
    Files.copy(testVideo, inputDirectory.resolve("video1.mp4"));
    Files.copy(testVideo, inputDirectory.resolve("video2.mp4"));
    Files.copy(testVideo, inputDirectory.resolve("NestedFolder/video3.mp4"));

    Files.createDirectories(outputDirectory.resolve("NestedFolder"));
    Files.copy(testVideo, outputDirectory.resolve("video1 - CFR (incomplete).mp4"));
    Files.copy(testVideo, outputDirectory.resolve("video2 - CFR (incomplete).mp4"));
    Files.copy(testVideo, outputDirectory.resolve("NestedFolder/video3 - CFR (incomplete).mp4"));

    Files.createDirectories(archiveDirectory.resolve("NestedFolder"));
    Files.copy(testVideo, archiveDirectory.resolve("video1 - Archived (incomplete).mp4"));
    Files.copy(testVideo, archiveDirectory.resolve("video2 - Archived (incomplete).mp4"));
    Files.copy(
        testVideo, archiveDirectory.resolve("NestedFolder/video3 - Archived (incomplete).mp4"));

    // When
    runApp();

    // Then
    assertThatTestDirectory()
        .containsExactly(
            outputDirectory.resolve("video1 - CFR.mp4"),
            outputDirectory.resolve("video2 - CFR.mp4"),
            outputDirectory.resolve("NestedFolder/video3 - CFR.mp4"),
            //
            archiveDirectory.resolve("video1 - Archived.mp4"),
            archiveDirectory.resolve("video2 - Archived.mp4"),
            archiveDirectory.resolve("NestedFolder/video3 - Archived.mp4"));
  }

  @Test
  void encodingsAlreadyExist() throws IOException {
    // Given
    Files.createDirectories(inputDirectory.resolve("NestedFolder"));
    Files.copy(testVideo, inputDirectory.resolve("video1.mp4"));
    Files.copy(testVideo, inputDirectory.resolve("video2.mp4"));
    Files.copy(testVideo, inputDirectory.resolve("NestedFolder/video3.mp4"));

    Files.createDirectories(outputDirectory.resolve("NestedFolder"));
    Files.copy(testVideo, outputDirectory.resolve("video1 - CFR.mp4"));
    Files.copy(testVideo, outputDirectory.resolve("video2 - CFR.mp4"));
    Files.copy(testVideo, outputDirectory.resolve("NestedFolder/video3 - CFR.mp4"));

    // When
    runApp();

    // Then
    assertThatTestDirectory()
        .containsExactly(
            outputDirectory.resolve("video1 - CFR.mp4"),
            outputDirectory.resolve("video2 - CFR.mp4"),
            outputDirectory.resolve("NestedFolder/video3 - CFR.mp4"),
            //
            archiveDirectory.resolve("video1 - Archived.mp4"),
            archiveDirectory.resolve("video2 - Archived.mp4"),
            archiveDirectory.resolve("NestedFolder/video3 - Archived.mp4"));
  }

  @Test
  void archivesAlreadyExist() throws IOException {
    // Given
    Files.createDirectories(inputDirectory.resolve("NestedFolder"));
    Files.copy(testVideo, inputDirectory.resolve("video1.mp4"));
    Files.copy(testVideo, inputDirectory.resolve("video2.mp4"));
    Files.copy(testVideo, inputDirectory.resolve("NestedFolder/video3.mp4"));

    Files.createDirectories(archiveDirectory.resolve("NestedFolder"));
    Files.copy(testVideo, archiveDirectory.resolve("video1 - Archived.mp4"));
    Files.copy(testVideo, archiveDirectory.resolve("video2 - Archived.mp4"));
    Files.copy(testVideo, archiveDirectory.resolve("NestedFolder/video3 - Archived.mp4"));

    // When
    runApp();

    // Then
    assertThatTestDirectory()
        .containsExactly(
            outputDirectory.resolve("video1 - CFR.mp4"),
            outputDirectory.resolve("video2 - CFR.mp4"),
            outputDirectory.resolve("NestedFolder/video3 - CFR.mp4"),
            //
            archiveDirectory.resolve("video1 - Archived.mp4"),
            archiveDirectory.resolve("video2 - Archived.mp4"),
            archiveDirectory.resolve("NestedFolder/video3 - Archived.mp4"));
  }

  @Test
  void incompleteEncodingsAndArchivesAndEncodingsAndArchivesAlreadyExist() throws IOException {
    // Given
    Files.createDirectories(inputDirectory.resolve("NestedFolder"));
    Files.copy(testVideo, inputDirectory.resolve("video1.mp4"));
    Files.copy(testVideo, inputDirectory.resolve("video2.mp4"));
    Files.copy(testVideo, inputDirectory.resolve("NestedFolder/video3.mp4"));

    Files.createDirectories(outputDirectory.resolve("NestedFolder"));
    Files.copy(testVideo, outputDirectory.resolve("video1 - CFR (incomplete).mp4"));
    Files.copy(testVideo, outputDirectory.resolve("video2 - CFR (incomplete).mp4"));
    Files.copy(testVideo, outputDirectory.resolve("NestedFolder/video3 - CFR (incomplete).mp4"));

    Files.createDirectories(archiveDirectory.resolve("NestedFolder"));
    Files.copy(testVideo, archiveDirectory.resolve("video1 - Archived (incomplete).mp4"));
    Files.copy(testVideo, archiveDirectory.resolve("video2 - Archived (incomplete).mp4"));
    Files.copy(
        testVideo, archiveDirectory.resolve("NestedFolder/video3 - Archived (incomplete).mp4"));

    Files.createDirectories(outputDirectory.resolve("NestedFolder"));
    Files.copy(testVideo, outputDirectory.resolve("video1 - CFR.mp4"));
    Files.copy(testVideo, outputDirectory.resolve("video2 - CFR.mp4"));
    Files.copy(testVideo, outputDirectory.resolve("NestedFolder/video3 - CFR.mp4"));

    Files.createDirectories(archiveDirectory.resolve("NestedFolder"));
    Files.copy(testVideo, archiveDirectory.resolve("video1 - Archived.mp4"));
    Files.copy(testVideo, archiveDirectory.resolve("video2 - Archived.mp4"));
    Files.copy(testVideo, archiveDirectory.resolve("NestedFolder/video3 - Archived.mp4"));

    // When
    runApp();

    // Then
    assertThatTestDirectory()
        .containsExactly(
            outputDirectory.resolve("video1 - CFR.mp4"),
            outputDirectory.resolve("video2 - CFR.mp4"),
            outputDirectory.resolve("NestedFolder/video3 - CFR.mp4"),
            //
            archiveDirectory.resolve("video1 - Archived.mp4"),
            archiveDirectory.resolve("video2 - Archived.mp4"),
            archiveDirectory.resolve("NestedFolder/video3 - Archived.mp4"));
  }

  private void runApp() {
    App.main(
        inputDirectory.toString(),
        outputDirectory.toString(),
        archiveDirectory.toString(),
        Boolean.toString(false));
  }

  private StreamSubject assertThatTestDirectory() throws IOException {
    return assertThat(Files.walk(testDirectory).filter(Files::isRegularFile));
  }
}
