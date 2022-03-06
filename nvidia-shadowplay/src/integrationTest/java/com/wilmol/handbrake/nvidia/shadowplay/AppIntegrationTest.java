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
  @Order(0)
  void singleVideo() throws IOException {
    // Given
    // video to encode
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("my video.mp4"));

    // When
    runApp(inputDirectory, inputDirectory, inputDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            inputDirectory.resolve("my video - CFR.mp4"),
            // archive
            inputDirectory.resolve("my video - Archived.mp4"));
  }

  @Test
  @Order(1)
  void singleVideo_encodeToDifferentDirectory() throws IOException {
    // Given
    // video to encode
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("recording.mp4"));

    Path outputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay Encoded"));

    // When
    runApp(inputDirectory, outputDirectory, inputDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            outputDirectory.resolve("recording - CFR.mp4"),
            // archive
            inputDirectory.resolve("recording - Archived.mp4"));
  }

  @Test
  @Order(2)
  void singleVideo_archiveToDifferentDirectory() throws IOException {
    // Given
    // video to encode
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("vid1.mp4"));

    Path archiveDirectory = createDirectoryAt(testDirectory.resolve("Gameplay Archive"));

    // When
    runApp(inputDirectory, inputDirectory, archiveDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            inputDirectory.resolve("vid1 - CFR.mp4"),
            // archive
            archiveDirectory.resolve("vid1 - Archived.mp4"));
  }

  @Test
  @Order(3)
  void singleVideo_encodeAndArchiveToDifferentDirectory() throws IOException {
    // Given
    // video to encode
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("recording1.mp4"));

    Path outputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay Encoded"));

    Path archiveDirectory = createDirectoryAt(testDirectory.resolve("Gameplay Archive"));

    // When
    runApp(inputDirectory, outputDirectory, archiveDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            outputDirectory.resolve("recording1 - CFR.mp4"),
            // archive
            archiveDirectory.resolve("recording1 - Archived.mp4"));
  }

  @Test
  @Order(10)
  void singleVideo_nestedDirectoryStructure() throws IOException {
    // Given
    // video to encode
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("League of Legends/ranked_game1.mp4"));

    // When
    runApp(inputDirectory, inputDirectory, inputDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            inputDirectory.resolve("League of Legends/ranked_game1 - CFR.mp4"),
            // archive
            inputDirectory.resolve("League of Legends/ranked_game1 - Archived.mp4"));
  }

  @Test
  @Order(11)
  void singleVideo_nestedDirectoryStructure_encodeToDifferentDirectory() throws IOException {
    // Given
    // video to encode
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("StarCraft II/protoss.mp4"));

    Path outputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay Encoded"));

    // When
    runApp(inputDirectory, outputDirectory, inputDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            outputDirectory.resolve("StarCraft II/protoss - CFR.mp4"),
            // archive
            inputDirectory.resolve("StarCraft II/protoss - Archived.mp4"));
  }

  @Test
  @Order(12)
  void singleVideo_nestedDirectoryStructure_archiveToDifferentDirectory() throws IOException {
    // Given
    // video to encode
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("Path of Exile/vaal spark templar.mp4"));

    Path archiveDirectory = createDirectoryAt(testDirectory.resolve("Gameplay Archive"));

    // When
    runApp(inputDirectory, inputDirectory, archiveDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            inputDirectory.resolve("Path of Exile/vaal spark templar - CFR.mp4"),
            // archive
            archiveDirectory.resolve("Path of Exile/vaal spark templar - Archived.mp4"));
  }

  @Test
  @Order(13)
  void singleVideo_nestedDirectoryStructure_encodeAndArchiveToDifferentDirectory()
      throws IOException {
    // Given
    // video to encode
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("Halo Infinite/Legendary Campaign/1st mission.mp4"));

    Path outputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay Encoded"));

    Path archiveDirectory = createDirectoryAt(testDirectory.resolve("Gameplay Archive"));

    // When
    runApp(inputDirectory, outputDirectory, archiveDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            outputDirectory.resolve("Halo Infinite/Legendary Campaign/1st mission - CFR.mp4"),
            // archive
            archiveDirectory.resolve(
                "Halo Infinite/Legendary Campaign/1st mission - Archived.mp4"));
  }

  @Test
  @Order(20)
  void severalVideos_nestedDirectoryStructure() throws IOException {
    // Given
    // videos to encode
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
            // encodings
            inputDirectory.resolve("recording1 - CFR.mp4"),
            inputDirectory.resolve("recording2 - CFR.mp4"),
            inputDirectory.resolve("Nested/recording3 - CFR.mp4"),
            inputDirectory.resolve("Nested1/Nested2/recording4 - CFR.mp4"),
            // archives
            inputDirectory.resolve("recording1 - Archived.mp4"),
            inputDirectory.resolve("recording2 - Archived.mp4"),
            inputDirectory.resolve("Nested/recording3 - Archived.mp4"),
            inputDirectory.resolve("Nested1/Nested2/recording4 - Archived.mp4"));
  }

  @Test
  @Order(21)
  void severalVideos_nestedDirectoryStructure_encodeToDifferentDirectory() throws IOException {
    // Given
    // videos to encode
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
            // encodings
            outputDirectory.resolve("recording1 - CFR.mp4"),
            outputDirectory.resolve("recording2 - CFR.mp4"),
            outputDirectory.resolve("Nested/recording3 - CFR.mp4"),
            outputDirectory.resolve("Nested1/Nested2/recording4 - CFR.mp4"),
            // archives
            inputDirectory.resolve("recording1 - Archived.mp4"),
            inputDirectory.resolve("recording2 - Archived.mp4"),
            inputDirectory.resolve("Nested/recording3 - Archived.mp4"),
            inputDirectory.resolve("Nested1/Nested2/recording4 - Archived.mp4"));
  }

  @Test
  @Order(22)
  void severalVideos_nestedDirectoryStructure_archiveToDifferentDirectory() throws IOException {
    // Given
    // videos to encode
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
            // encodings
            inputDirectory.resolve("recording1 - CFR.mp4"),
            inputDirectory.resolve("recording2 - CFR.mp4"),
            inputDirectory.resolve("Nested/recording3 - CFR.mp4"),
            inputDirectory.resolve("Nested1/Nested2/recording4 - CFR.mp4"),
            // archives
            archiveDirectory.resolve("recording1 - Archived.mp4"),
            archiveDirectory.resolve("recording2 - Archived.mp4"),
            archiveDirectory.resolve("Nested/recording3 - Archived.mp4"),
            archiveDirectory.resolve("Nested1/Nested2/recording4 - Archived.mp4"));
  }

  @Test
  @Order(23)
  void severalVideos_nestedDirectoryStructure_encodeAndArchiveToDifferentDirectory()
      throws IOException {
    // Given
    // videos to encode
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
            // encodings
            outputDirectory.resolve("recording1 - CFR.mp4"),
            outputDirectory.resolve("recording2 - CFR.mp4"),
            outputDirectory.resolve("Nested/recording3 - CFR.mp4"),
            outputDirectory.resolve("Nested1/Nested2/recording4 - CFR.mp4"),
            // archives
            archiveDirectory.resolve("recording1 - Archived.mp4"),
            archiveDirectory.resolve("recording2 - Archived.mp4"),
            archiveDirectory.resolve("Nested/recording3 - Archived.mp4"),
            archiveDirectory.resolve("Nested1/Nested2/recording4 - Archived.mp4"));
  }

  @Test
  @Order(30)
  void singleVideo_and_deletesIncompleteEncodingsAndArchives() throws IOException {
    // Given
    // video to encode
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("my video.mp4"));

    // incomplete encodings
    createVideoAt(inputDirectory.resolve("recording - CFR (incomplete).mp4"));
    createVideoAt(inputDirectory.resolve("recording2 - CFR (incomplete).mp4"));

    // incomplete archives
    createVideoAt(inputDirectory.resolve("vid - Archived (incomplete).mp4"));
    createVideoAt(inputDirectory.resolve("vid2 - Archived (incomplete).mp4"));

    // When
    runApp(inputDirectory, inputDirectory, inputDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            inputDirectory.resolve("my video - CFR.mp4"),
            // archive
            inputDirectory.resolve("my video - Archived.mp4"));
  }

  @Test
  @Order(31)
  void
      singleVideo_encodeAndArchiveToDifferentDirectory_and_deletesIncompleteEncodingsAndArchivesInAllDirectories()
          throws IOException {
    // Given
    // video to encode
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("my video.mp4"));

    Path outputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay Encoded"));

    Path archiveDirectory = createDirectoryAt(testDirectory.resolve("Gameplay Archive"));

    // incomplete encodings
    createVideoAt(inputDirectory.resolve("recording - CFR (incomplete).mp4"));
    createVideoAt(outputDirectory.resolve("recording - CFR (incomplete).mp4"));
    createVideoAt(archiveDirectory.resolve("recording - CFR (incomplete).mp4"));

    // incomplete archives
    createVideoAt(inputDirectory.resolve("vid - Archived (incomplete).mp4"));
    createVideoAt(outputDirectory.resolve("vid - Archived (incomplete).mp4"));
    createVideoAt(archiveDirectory.resolve("vid - Archived (incomplete).mp4"));

    // When
    runApp(inputDirectory, outputDirectory, archiveDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            outputDirectory.resolve("my video - CFR.mp4"),
            // archive
            archiveDirectory.resolve("my video - Archived.mp4"));
  }

  @Test
  @Order(40)
  void singleVideo_and_retainsCompleteEncodingsAndArchives() throws IOException {
    // Given
    // video to encode
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("my video.mp4"));

    // unrelated encodings
    createVideoAt(inputDirectory.resolve("recording - CFR.mp4"));
    createVideoAt(inputDirectory.resolve("recording2 - CFR.mp4"));

    // unrelated archives
    createVideoAt(inputDirectory.resolve("recording - Archived.mp4"));
    createVideoAt(inputDirectory.resolve("recording2 - Archived.mp4"));

    // When
    runApp(inputDirectory, inputDirectory, inputDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            inputDirectory.resolve("my video - CFR.mp4"),
            // archive
            inputDirectory.resolve("my video - Archived.mp4"),
            // unrelated encodings
            inputDirectory.resolve("recording - CFR.mp4"),
            inputDirectory.resolve("recording2 - CFR.mp4"),
            // unrelated archives
            inputDirectory.resolve("recording - Archived.mp4"),
            inputDirectory.resolve("recording2 - Archived.mp4"));
  }

  @Test
  @Order(41)
  void
      singleVideo_encodeAndArchiveToDifferentDirectory_and_retainsCompleteEncodingsAndArchivesInAllDirectories()
          throws IOException {
    // Given
    // video to encode
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("my video.mp4"));

    Path outputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay Encoded"));

    Path archiveDirectory = createDirectoryAt(testDirectory.resolve("Gameplay Archive"));

    // unrelated encodings
    createVideoAt(inputDirectory.resolve("recording - CFR.mp4"));
    createVideoAt(outputDirectory.resolve("recording - CFR.mp4"));
    createVideoAt(archiveDirectory.resolve("recording - CFR.mp4"));

    // unrelated archives
    createVideoAt(inputDirectory.resolve("recording - Archived.mp4"));
    createVideoAt(outputDirectory.resolve("recording - Archived.mp4"));
    createVideoAt(archiveDirectory.resolve("recording - Archived.mp4"));

    // When
    runApp(inputDirectory, outputDirectory, archiveDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            outputDirectory.resolve("my video - CFR.mp4"),
            // archive
            archiveDirectory.resolve("my video - Archived.mp4"),
            // unrelated encodings
            inputDirectory.resolve("recording - CFR.mp4"),
            outputDirectory.resolve("recording - CFR.mp4"),
            archiveDirectory.resolve("recording - CFR.mp4"),
            // unrelated archives
            inputDirectory.resolve("recording - Archived.mp4"),
            outputDirectory.resolve("recording - Archived.mp4"),
            archiveDirectory.resolve("recording - Archived.mp4"));
  }

  @Test
  @Order(50)
  void singleVideo_encodingAlreadyExists() throws IOException {
    // Given
    // video to encode
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("my video.mp4"));

    // already encoded
    createVideoAt(inputDirectory.resolve("my video - CFR.mp4"));

    // When
    runApp(inputDirectory, inputDirectory, inputDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            inputDirectory.resolve("my video - CFR.mp4"),
            // archive
            inputDirectory.resolve("my video - Archived.mp4"));
  }

  @Test
  @Order(51)
  void singleVideo_archiveAlreadyExists() throws IOException {
    // Given
    // video to encode
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("my video.mp4"));

    // already archived
    createVideoAt(inputDirectory.resolve("my video - Archived.mp4"));

    // When
    runApp(inputDirectory, inputDirectory, inputDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            inputDirectory.resolve("my video - CFR.mp4"),
            // archive
            inputDirectory.resolve("my video - Archived.mp4"));
  }

  @Test
  @Order(52)
  void singleVideo_encodingAndArchiveAlreadyExists() throws IOException {
    // Given
    // video to encode
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("my video.mp4"));

    // already encoded
    createVideoAt(inputDirectory.resolve("my video - CFR.mp4"));

    // already archived
    createVideoAt(inputDirectory.resolve("my video - Archived.mp4"));

    // When
    runApp(inputDirectory, inputDirectory, inputDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            inputDirectory.resolve("my video - CFR.mp4"),
            // archive
            inputDirectory.resolve("my video - Archived.mp4"));
  }

  @Test
  @Order(100)
  void
      severalVideos_nestedDirectoryStructure_and_someEncodingsAndArchivesAlreadyExists_and_deletesIncompleteEncodingsAndArchives_and_retainsCompleteEncodingsAndArchives()
          throws IOException {
    // Given
    // videos to encode
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("recording1.mp4"));
    createVideoAt(inputDirectory.resolve("recording2.mp4"));
    createVideoAt(inputDirectory.resolve("Nested/recording3.mp4"));
    createVideoAt(inputDirectory.resolve("Nested1/Nested2/recording4.mp4"));

    // already encoded
    createVideoAt(inputDirectory.resolve("recording2 - CFR.mp4"));
    createVideoAt(inputDirectory.resolve("Nested/recording3 - CFR.mp4"));

    // already archived
    createVideoAt(inputDirectory.resolve("recording1 - Archived.mp4"));
    createVideoAt(inputDirectory.resolve("Nested/recording3 - Archived.mp4"));

    // incomplete encodings
    createVideoAt(inputDirectory.resolve("recording1 - CFR (incomplete).mp4"));
    createVideoAt(
        inputDirectory.resolve("Nested1/Nested2/Nested3/other video - CFR (incomplete).mp4"));

    // incomplete archives
    createVideoAt(inputDirectory.resolve("recording2 - Archived (incomplete).mp4"));
    createVideoAt(
        inputDirectory.resolve("Nested1/Nested2/Nested3/random video - Archived (incomplete).mp4"));

    // unrelated encodings
    createVideoAt(inputDirectory.resolve("Starcraft II/protoss - CFR.mp4"));
    createVideoAt(inputDirectory.resolve("Starcraft II/Campaign/terran1 - CFR.mp4"));

    // unrelated archives
    createVideoAt(inputDirectory.resolve("League of Legends/ryze - Archived.mp4"));
    createVideoAt(inputDirectory.resolve("Path of Exile/Old builds/Discharge CoC - Archived.mp4"));

    // When
    runApp(inputDirectory, inputDirectory, inputDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            // encodings
            inputDirectory.resolve("recording1 - CFR.mp4"),
            inputDirectory.resolve("recording2 - CFR.mp4"),
            inputDirectory.resolve("Nested/recording3 - CFR.mp4"),
            inputDirectory.resolve("Nested1/Nested2/recording4 - CFR.mp4"),
            // archives
            inputDirectory.resolve("recording1 - Archived.mp4"),
            inputDirectory.resolve("recording2 - Archived.mp4"),
            inputDirectory.resolve("Nested/recording3 - Archived.mp4"),
            inputDirectory.resolve("Nested1/Nested2/recording4 - Archived.mp4"),
            // unrelated encodings
            inputDirectory.resolve("Starcraft II/protoss - CFR.mp4"),
            inputDirectory.resolve("Starcraft II/Campaign/terran1 - CFR.mp4"),
            // unrelated archives
            inputDirectory.resolve("League of Legends/ryze - Archived.mp4"),
            inputDirectory.resolve("Path of Exile/Old builds/Discharge CoC - Archived.mp4"));
  }

  @Test
  @Order(101)
  void
      severalVideos_nestedDirectoryStructure_encodeAndArchiveToDifferentDirectory_and_someEncodingsAndArchivesAlreadyExists_and_deletesIncompleteEncodingsAndArchivesInAllDirectories_and_retainsCompleteEncodingsAndArchivesInAllDirectories()
          throws IOException {
    // Given
    // videos to encode
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("recording1.mp4"));
    createVideoAt(inputDirectory.resolve("recording2.mp4"));
    createVideoAt(inputDirectory.resolve("Nested/recording3.mp4"));
    createVideoAt(inputDirectory.resolve("Nested1/Nested2/recording4.mp4"));

    Path outputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay Encoded"));

    Path archiveDirectory = createDirectoryAt(testDirectory.resolve("Gameplay Archive"));

    // already encoded
    createVideoAt(outputDirectory.resolve("recording2 - CFR.mp4"));
    createVideoAt(outputDirectory.resolve("Nested/recording3 - CFR.mp4"));

    // already archived
    createVideoAt(archiveDirectory.resolve("recording1 - Archived.mp4"));
    createVideoAt(archiveDirectory.resolve("Nested/recording3 - Archived.mp4"));

    // incomplete encodings
    createVideoAt(inputDirectory.resolve("recording1 - CFR (incomplete).mp4"));
    createVideoAt(outputDirectory.resolve("recording1 - CFR (incomplete).mp4"));
    createVideoAt(archiveDirectory.resolve("recording1 - CFR (incomplete).mp4"));

    // incomplete archives
    createVideoAt(inputDirectory.resolve("recording2 - Archived (incomplete).mp4"));
    createVideoAt(outputDirectory.resolve("recording2 - Archived (incomplete).mp4"));
    createVideoAt(archiveDirectory.resolve("recording2 - Archived (incomplete).mp4"));

    // unrelated encodings
    createVideoAt(inputDirectory.resolve("recording - CFR.mp4"));
    createVideoAt(outputDirectory.resolve("recording - CFR.mp4"));
    createVideoAt(archiveDirectory.resolve("recording - CFR.mp4"));

    // unrelated archives
    createVideoAt(inputDirectory.resolve("recording - Archived.mp4"));
    createVideoAt(outputDirectory.resolve("recording - Archived.mp4"));
    createVideoAt(archiveDirectory.resolve("recording - Archived.mp4"));

    // When
    runApp(inputDirectory, outputDirectory, archiveDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            // encodings
            outputDirectory.resolve("recording1 - CFR.mp4"),
            outputDirectory.resolve("recording2 - CFR.mp4"),
            outputDirectory.resolve("Nested/recording3 - CFR.mp4"),
            outputDirectory.resolve("Nested1/Nested2/recording4 - CFR.mp4"),
            // archives
            archiveDirectory.resolve("recording1 - Archived.mp4"),
            archiveDirectory.resolve("recording2 - Archived.mp4"),
            archiveDirectory.resolve("Nested/recording3 - Archived.mp4"),
            archiveDirectory.resolve("Nested1/Nested2/recording4 - Archived.mp4"),
            // unrelated encodings
            inputDirectory.resolve("recording - CFR.mp4"),
            outputDirectory.resolve("recording - CFR.mp4"),
            archiveDirectory.resolve("recording - CFR.mp4"),
            // unrelated archives
            inputDirectory.resolve("recording - Archived.mp4"),
            outputDirectory.resolve("recording - Archived.mp4"),
            archiveDirectory.resolve("recording - Archived.mp4"));
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
