package com.wilmol.handbrake.nvidia.shadowplay;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.truth.Truth8.assertThat;

import com.google.common.io.Resources;
import com.google.common.truth.StreamSubject;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * AppIntegrationTest.
 *
 * <p>Requires HandBrakeCLI to be installed.
 *
 * @author <a href=https://wilmol.com>Will Molloy</a>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AppIntegrationTest {

  private Path testDirectory;
  private Path testVideo;

  @BeforeEach
  void setUp() throws Exception {
    testDirectory = Path.of(this.getClass().getSimpleName());
    testVideo = Path.of(Resources.getResource("test-video.mp4").toURI());
  }

  @AfterEach
  void tearDown() throws IOException {
    FileUtils.deleteDirectory(testDirectory.toFile());
  }

  @Test
  @Order(1)
  void singleVideo() throws IOException {
    // Given
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("my video.mp4"));

    // When
    runApp(inputDirectory, inputDirectory, inputDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            inputDirectory.resolve("my video - CFR.mp4"),
            inputDirectory.resolve("my video - Archived.mp4"));
  }

  @Test
  @Order(2)
  void singleVideo_encodeToDifferentDirectory() throws IOException {
    // Given
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("recording.mp4"));

    Path outputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay Encoded"));

    // When
    runApp(inputDirectory, outputDirectory, inputDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            outputDirectory.resolve("recording - CFR.mp4"),
            inputDirectory.resolve("recording - Archived.mp4"));
  }

  @Test
  @Order(3)
  void singleVideo_archiveToDifferentDirectory() throws IOException {
    // Given
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("vid1.mp4"));

    Path archiveDirectory = createDirectoryAt(testDirectory.resolve("Gameplay Archive"));

    // When
    runApp(inputDirectory, inputDirectory, archiveDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            inputDirectory.resolve("vid1 - CFR.mp4"),
            archiveDirectory.resolve("vid1 - Archived.mp4"));
  }

  @Test
  @Order(4)
  void singleVideo_encodeAndArchiveToDifferentDirectory() throws IOException {
    // Given
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("recording1.mp4"));

    Path outputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay Encoded"));

    Path archiveDirectory = createDirectoryAt(testDirectory.resolve("Gameplay Archive"));

    // When
    runApp(inputDirectory, outputDirectory, archiveDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            outputDirectory.resolve("recording1 - CFR.mp4"),
            archiveDirectory.resolve("recording1 - Archived.mp4"));
  }

  @Test
  @Order(5)
  void singleVideo_nestedDirectoryStructure() throws IOException {
    // Given
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("League of Legends/ranked_game1.mp4"));

    // When
    runApp(inputDirectory, inputDirectory, inputDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            inputDirectory.resolve("League of Legends/ranked_game1 - CFR.mp4"),
            inputDirectory.resolve("League of Legends/ranked_game1 - Archived.mp4"));
  }

  @Test
  @Order(6)
  void singleVideo_nestedDirectoryStructure_encodeToDifferentDirectory() throws IOException {
    // Given
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("StarCraft II/protoss.mp4"));

    Path outputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay Encoded"));

    // When
    runApp(inputDirectory, outputDirectory, inputDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            outputDirectory.resolve("StarCraft II/protoss - CFR.mp4"),
            inputDirectory.resolve("StarCraft II/protoss - Archived.mp4"));
  }

  @Test
  @Order(7)
  void singleVideo_nestedDirectoryStructure_archiveToDifferentDirectory() throws IOException {
    // Given
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("Path of Exile/vaal spark templar.mp4"));

    Path archiveDirectory = createDirectoryAt(testDirectory.resolve("Gameplay Archive"));

    // When
    runApp(inputDirectory, inputDirectory, archiveDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            inputDirectory.resolve("Path of Exile/vaal spark templar - CFR.mp4"),
            archiveDirectory.resolve("Path of Exile/vaal spark templar - Archived.mp4"));
  }

  @Test
  @Order(8)
  void singleVideo_nestedDirectoryStructure_encodeAndArchiveToDifferentDirectory()
      throws IOException {
    // Given
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("Halo Infinite/Legendary Campaign/1st mission.mp4"));

    Path outputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay Encoded"));

    Path archiveDirectory = createDirectoryAt(testDirectory.resolve("Gameplay Archive"));

    // When
    runApp(inputDirectory, outputDirectory, archiveDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            outputDirectory.resolve("Halo Infinite/Legendary Campaign/1st mission - CFR.mp4"),
            archiveDirectory.resolve(
                "Halo Infinite/Legendary Campaign/1st mission - Archived.mp4"));
  }

  @Test
  @Order(9)
  void severalVideos_nestedDirectoryStructure() throws IOException {
    // Given
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("recording1.mp4"));
    createVideoAt(inputDirectory.resolve("recording2.mp4"));
    createVideoAt(inputDirectory.resolve("Nested/recording3.mp4"));
    createVideoAt(inputDirectory.resolve("Nested1/Nested2/recording4.mp4"));

    // When
    runApp(inputDirectory, inputDirectory, inputDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            inputDirectory.resolve("recording1 - CFR.mp4"),
            inputDirectory.resolve("recording2 - CFR.mp4"),
            inputDirectory.resolve("Nested/recording3 - CFR.mp4"),
            inputDirectory.resolve("Nested1/Nested2/recording4 - CFR.mp4"),
            inputDirectory.resolve("recording1 - Archived.mp4"),
            inputDirectory.resolve("recording2 - Archived.mp4"),
            inputDirectory.resolve("Nested/recording3 - Archived.mp4"),
            inputDirectory.resolve("Nested1/Nested2/recording4 - Archived.mp4"));
  }

  @Test
  @Order(10)
  void severalVideos_nestedDirectoryStructure_encodeToDifferentDirectory() throws IOException {
    // Given
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("recording1.mp4"));
    createVideoAt(inputDirectory.resolve("recording2.mp4"));
    createVideoAt(inputDirectory.resolve("Nested/recording3.mp4"));
    createVideoAt(inputDirectory.resolve("Nested1/Nested2/recording4.mp4"));

    Path outputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay Encoded"));

    // When
    runApp(inputDirectory, outputDirectory, inputDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            outputDirectory.resolve("recording1 - CFR.mp4"),
            outputDirectory.resolve("recording2 - CFR.mp4"),
            outputDirectory.resolve("Nested/recording3 - CFR.mp4"),
            outputDirectory.resolve("Nested1/Nested2/recording4 - CFR.mp4"),
            inputDirectory.resolve("recording1 - Archived.mp4"),
            inputDirectory.resolve("recording2 - Archived.mp4"),
            inputDirectory.resolve("Nested/recording3 - Archived.mp4"),
            inputDirectory.resolve("Nested1/Nested2/recording4 - Archived.mp4"));
  }

  @Test
  @Order(11)
  void severalVideos_nestedDirectoryStructure_archiveToDifferentDirectory() throws IOException {
    // Given
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("recording1.mp4"));
    createVideoAt(inputDirectory.resolve("recording2.mp4"));
    createVideoAt(inputDirectory.resolve("Nested/recording3.mp4"));
    createVideoAt(inputDirectory.resolve("Nested1/Nested2/recording4.mp4"));

    Path archiveDirectory = createDirectoryAt(testDirectory.resolve("Gameplay Archive"));

    // When
    runApp(inputDirectory, inputDirectory, archiveDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            inputDirectory.resolve("recording1 - CFR.mp4"),
            inputDirectory.resolve("recording2 - CFR.mp4"),
            inputDirectory.resolve("Nested/recording3 - CFR.mp4"),
            inputDirectory.resolve("Nested1/Nested2/recording4 - CFR.mp4"),
            archiveDirectory.resolve("recording1 - Archived.mp4"),
            archiveDirectory.resolve("recording2 - Archived.mp4"),
            archiveDirectory.resolve("Nested/recording3 - Archived.mp4"),
            archiveDirectory.resolve("Nested1/Nested2/recording4 - Archived.mp4"));
  }

  @Test
  @Order(12)
  void severalVideos_nestedDirectoryStructure_encodeAndArchiveToDifferentDirectory()
      throws IOException {
    // Given
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("recording1.mp4"));
    createVideoAt(inputDirectory.resolve("recording2.mp4"));
    createVideoAt(inputDirectory.resolve("Nested/recording3.mp4"));
    createVideoAt(inputDirectory.resolve("Nested1/Nested2/recording4.mp4"));

    Path outputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay Encoded"));

    Path archiveDirectory = createDirectoryAt(testDirectory.resolve("Gameplay Archive"));

    // When
    runApp(inputDirectory, outputDirectory, archiveDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            outputDirectory.resolve("recording1 - CFR.mp4"),
            outputDirectory.resolve("recording2 - CFR.mp4"),
            outputDirectory.resolve("Nested/recording3 - CFR.mp4"),
            outputDirectory.resolve("Nested1/Nested2/recording4 - CFR.mp4"),
            archiveDirectory.resolve("recording1 - Archived.mp4"),
            archiveDirectory.resolve("recording2 - Archived.mp4"),
            archiveDirectory.resolve("Nested/recording3 - Archived.mp4"),
            archiveDirectory.resolve("Nested1/Nested2/recording4 - Archived.mp4"));
  }

  @Test
  @Order(13)
  void singleVideo_and_deletesIncompleteEncodingsAndArchives() throws IOException {
    // Given
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("my video.mp4"));

    createVideoAt(inputDirectory.resolve("recording - CFR (incomplete).mp4"));
    createVideoAt(inputDirectory.resolve("recording2 - CFR (incomplete).mp4"));

    createVideoAt(inputDirectory.resolve("vid - Archived (incomplete).mp4"));
    createVideoAt(inputDirectory.resolve("vid2 - Archived (incomplete).mp4"));

    // When
    runApp(inputDirectory, inputDirectory, inputDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            inputDirectory.resolve("my video - CFR.mp4"),
            inputDirectory.resolve("my video - Archived.mp4"));
  }

  @Test
  @Order(14)
  void singleVideo_and_retainsCompleteEncodingsAndArchives() throws IOException {
    // Given
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("my video.mp4"));

    createVideoAt(inputDirectory.resolve("recording - CFR.mp4"));
    createVideoAt(inputDirectory.resolve("recording2 - CFR.mp4"));

    createVideoAt(inputDirectory.resolve("recording - Archived.mp4"));
    createVideoAt(inputDirectory.resolve("recording2 - Archived.mp4"));

    // When
    runApp(inputDirectory, inputDirectory, inputDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            inputDirectory.resolve("my video - CFR.mp4"),
            inputDirectory.resolve("my video - Archived.mp4"),
            inputDirectory.resolve("recording - CFR.mp4"),
            inputDirectory.resolve("recording2 - CFR.mp4"),
            inputDirectory.resolve("recording - Archived.mp4"),
            inputDirectory.resolve("recording2 - Archived.mp4")
        );
  }

  @Test
  @Order(15)
  void singleVideo_encodingAlreadyExists() throws IOException {
    // Given
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("my video.mp4"));

    createVideoAt(inputDirectory.resolve("my video - CFR.mp4"));

    // When
    runApp(inputDirectory, inputDirectory, inputDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            inputDirectory.resolve("my video - CFR.mp4"),
            inputDirectory.resolve("my video - Archived.mp4"));
  }

  @Test
  @Order(16)
  void singleVideo_archiveAlreadyExists() throws IOException {
    // Given
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("my video.mp4"));

    createVideoAt(inputDirectory.resolve("my video - Archived.mp4"));

    // When
    runApp(inputDirectory, inputDirectory, inputDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            inputDirectory.resolve("my video - CFR.mp4"),
            inputDirectory.resolve("my video - Archived.mp4"));
  }

  @Test
  @Order(17)
  void singleVideo_encodingAndArchiveAlreadyExists() throws IOException {
    // Given
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("my video.mp4"));

    createVideoAt(inputDirectory.resolve("my video - Archived.mp4"));

    // When
    runApp(inputDirectory, inputDirectory, inputDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            inputDirectory.resolve("my video - CFR.mp4"),
            inputDirectory.resolve("my video - Archived.mp4"));
  }

  @CanIgnoreReturnValue
  private Path createDirectoryAt(Path path) throws IOException {
    // not returning result of Files.createDirectories - it's absolute rather than relative, which
    // breaks the tests
    Files.createDirectories(path);
    return path;
  }

  @CanIgnoreReturnValue
  private Path createVideoAt(Path path) throws IOException {
    Files.createDirectories(checkNotNull(path.getParent()));
    Files.copy(testVideo, path);
    return path;
  }

  private void runApp(Path inputDirectory, Path outputDirectory, Path archiveDirectory) {
    App.main(
        inputDirectory.toString(),
        outputDirectory.toString(),
        archiveDirectory.toString(),
        Boolean.FALSE.toString());
  }

  private StreamSubject assertThatTestDirectory() throws IOException {
    return assertThat(Files.walk(testDirectory).filter(Files::isRegularFile));
  }
}
