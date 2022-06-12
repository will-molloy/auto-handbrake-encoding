package com.willmolloy.handbrake.nvidia.shadowplay;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.truth.Truth.assertThat;

import com.google.common.io.Resources;
import com.google.common.truth.Correspondence;
import com.google.common.truth.IterableSubject;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.IntStream;
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
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AppIntegrationTest {

  private Path testDirectory;
  private Path testVideo;
  private Path testVideoEncoded;
  private Path testVideo2;

  @BeforeEach
  void setUp() throws Exception {
    testDirectory = Path.of(this.getClass().getSimpleName());
    testVideo = Path.of(Resources.getResource("Big_Buck_Bunny_360_10s_1MB.mp4").toURI());
    testVideoEncoded =
        Path.of(
            Resources.getResource("Big_Buck_Bunny_360_10s_1MB_encoded_Production_Standard.mp4")
                .toURI());
    testVideo2 = Path.of(Resources.getResource("Big_Buck_Bunny_360_10s_2MB.mp4").toURI());
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
    createVideoAt(inputDirectory.resolve("my video.mp4"), testVideo);

    // When
    runApp(inputDirectory, inputDirectory, inputDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            new PathAndContents(inputDirectory.resolve("my video.cfr.mp4"), testVideoEncoded),
            // archive
            new PathAndContents(inputDirectory.resolve("my video.archived.mp4"), testVideo));
  }

  @Test
  @Order(1)
  void singleVideo_encodeToDifferentDirectory() throws IOException {
    // Given
    // video to encode
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("recording.mp4"), testVideo);

    Path outputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay Encoded"));

    // When
    runApp(inputDirectory, outputDirectory, inputDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            new PathAndContents(outputDirectory.resolve("recording.cfr.mp4"), testVideoEncoded),
            // archive
            new PathAndContents(inputDirectory.resolve("recording.archived.mp4"), testVideo));
  }

  @Test
  @Order(2)
  void singleVideo_archiveToDifferentDirectory() throws IOException {
    // Given
    // video to encode
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("vid1.mp4"), testVideo);

    Path archiveDirectory = createDirectoryAt(testDirectory.resolve("Gameplay Archive"));

    // When
    runApp(inputDirectory, inputDirectory, archiveDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            new PathAndContents(inputDirectory.resolve("vid1.cfr.mp4"), testVideoEncoded),
            // archive
            new PathAndContents(archiveDirectory.resolve("vid1.archived.mp4"), testVideo));
  }

  @Test
  @Order(3)
  void singleVideo_encodeAndArchiveToDifferentDirectory() throws IOException {
    // Given
    // video to encode
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("recording1.mp4"), testVideo);

    Path outputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay Encoded"));

    Path archiveDirectory = createDirectoryAt(testDirectory.resolve("Gameplay Archive"));

    // When
    runApp(inputDirectory, outputDirectory, archiveDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            new PathAndContents(outputDirectory.resolve("recording1.cfr.mp4"), testVideoEncoded),
            // archive
            new PathAndContents(archiveDirectory.resolve("recording1.archived.mp4"), testVideo));
  }

  @Test
  @Order(10)
  void singleVideo_nestedDirectoryStructure() throws IOException {
    // Given
    // video to encode
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("League of Legends/ranked_game1.mp4"), testVideo);

    // When
    runApp(inputDirectory, inputDirectory, inputDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            new PathAndContents(
                inputDirectory.resolve("League of Legends/ranked_game1.cfr.mp4"),
                testVideoEncoded),
            // archive
            new PathAndContents(
                inputDirectory.resolve("League of Legends/ranked_game1.archived.mp4"),
                testVideo));
  }

  @Test
  @Order(11)
  void singleVideo_nestedDirectoryStructure_encodeToDifferentDirectory() throws IOException {
    // Given
    // video to encode
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("StarCraft II/protoss.mp4"), testVideo);

    Path outputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay Encoded"));

    // When
    runApp(inputDirectory, outputDirectory, inputDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            new PathAndContents(
                outputDirectory.resolve("StarCraft II/protoss.cfr.mp4"), testVideoEncoded),
            // archive
            new PathAndContents(
                inputDirectory.resolve("StarCraft II/protoss.archived.mp4"), testVideo));
  }

  @Test
  @Order(12)
  void singleVideo_nestedDirectoryStructure_archiveToDifferentDirectory() throws IOException {
    // Given
    // video to encode
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("Path of Exile/vaal spark templar.mp4"), testVideo);

    Path archiveDirectory = createDirectoryAt(testDirectory.resolve("Gameplay Archive"));

    // When
    runApp(inputDirectory, inputDirectory, archiveDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            new PathAndContents(
                inputDirectory.resolve("Path of Exile/vaal spark templar.cfr.mp4"),
                testVideoEncoded),
            // archive
            new PathAndContents(
                archiveDirectory.resolve("Path of Exile/vaal spark templar.archived.mp4"),
                testVideo));
  }

  @Test
  @Order(13)
  void singleVideo_nestedDirectoryStructure_encodeAndArchiveToDifferentDirectory()
      throws IOException {
    // Given
    // video to encode
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(
        inputDirectory.resolve("Halo Infinite/Legendary Campaign/1st mission.mp4"), testVideo);

    Path outputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay Encoded"));

    Path archiveDirectory = createDirectoryAt(testDirectory.resolve("Gameplay Archive"));

    // When
    runApp(inputDirectory, outputDirectory, archiveDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            new PathAndContents(
                outputDirectory.resolve("Halo Infinite/Legendary Campaign/1st mission.cfr.mp4"),
                testVideoEncoded),
            // archive
            new PathAndContents(
                archiveDirectory.resolve(
                    "Halo Infinite/Legendary Campaign/1st mission.archived.mp4"),
                testVideo));
  }

  @Test
  @Order(20)
  void severalVideos_nestedDirectoryStructure() throws IOException {
    // Given
    // videos to encode
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("recording1.mp4"), testVideo);
    createVideoAt(inputDirectory.resolve("recording2.mp4"), testVideo);
    createVideoAt(inputDirectory.resolve("Nested/recording3.mp4"), testVideo);
    createVideoAt(inputDirectory.resolve("Nested1/Nested2/recording4.mp4"), testVideo);

    // When
    runApp(inputDirectory, inputDirectory, inputDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            // encodings
            new PathAndContents(inputDirectory.resolve("recording1.cfr.mp4"), testVideoEncoded),
            new PathAndContents(inputDirectory.resolve("recording2.cfr.mp4"), testVideoEncoded),
            new PathAndContents(
                inputDirectory.resolve("Nested/recording3.cfr.mp4"), testVideoEncoded),
            new PathAndContents(
                inputDirectory.resolve("Nested1/Nested2/recording4.cfr.mp4"), testVideoEncoded),
            // archives
            new PathAndContents(inputDirectory.resolve("recording1.archived.mp4"), testVideo),
            new PathAndContents(inputDirectory.resolve("recording2.archived.mp4"), testVideo),
            new PathAndContents(
                inputDirectory.resolve("Nested/recording3.archived.mp4"), testVideo),
            new PathAndContents(
                inputDirectory.resolve("Nested1/Nested2/recording4.archived.mp4"), testVideo));
  }

  @Test
  @Order(21)
  void severalVideos_nestedDirectoryStructure_encodeToDifferentDirectory() throws IOException {
    // Given
    // videos to encode
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("recording1.mp4"), testVideo);
    createVideoAt(inputDirectory.resolve("recording2.mp4"), testVideo);
    createVideoAt(inputDirectory.resolve("Nested/recording3.mp4"), testVideo);
    createVideoAt(inputDirectory.resolve("Nested1/Nested2/recording4.mp4"), testVideo);

    Path outputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay Encoded"));

    // When
    runApp(inputDirectory, outputDirectory, inputDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            // encodings
            new PathAndContents(outputDirectory.resolve("recording1.cfr.mp4"), testVideoEncoded),
            new PathAndContents(outputDirectory.resolve("recording2.cfr.mp4"), testVideoEncoded),
            new PathAndContents(
                outputDirectory.resolve("Nested/recording3.cfr.mp4"), testVideoEncoded),
            new PathAndContents(
                outputDirectory.resolve("Nested1/Nested2/recording4.cfr.mp4"), testVideoEncoded),
            // archives
            new PathAndContents(inputDirectory.resolve("recording1.archived.mp4"), testVideo),
            new PathAndContents(inputDirectory.resolve("recording2.archived.mp4"), testVideo),
            new PathAndContents(
                inputDirectory.resolve("Nested/recording3.archived.mp4"), testVideo),
            new PathAndContents(
                inputDirectory.resolve("Nested1/Nested2/recording4.archived.mp4"), testVideo));
  }

  @Test
  @Order(22)
  void severalVideos_nestedDirectoryStructure_archiveToDifferentDirectory() throws IOException {
    // Given
    // videos to encode
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("recording1.mp4"), testVideo);
    createVideoAt(inputDirectory.resolve("recording2.mp4"), testVideo);
    createVideoAt(inputDirectory.resolve("Nested/recording3.mp4"), testVideo);
    createVideoAt(inputDirectory.resolve("Nested1/Nested2/recording4.mp4"), testVideo);

    Path archiveDirectory = createDirectoryAt(testDirectory.resolve("Gameplay Archive"));

    // When
    runApp(inputDirectory, inputDirectory, archiveDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            // encodings
            new PathAndContents(inputDirectory.resolve("recording1.cfr.mp4"), testVideoEncoded),
            new PathAndContents(inputDirectory.resolve("recording2.cfr.mp4"), testVideoEncoded),
            new PathAndContents(
                inputDirectory.resolve("Nested/recording3.cfr.mp4"), testVideoEncoded),
            new PathAndContents(
                inputDirectory.resolve("Nested1/Nested2/recording4.cfr.mp4"), testVideoEncoded),
            // archives
            new PathAndContents(archiveDirectory.resolve("recording1.archived.mp4"), testVideo),
            new PathAndContents(archiveDirectory.resolve("recording2.archived.mp4"), testVideo),
            new PathAndContents(
                archiveDirectory.resolve("Nested/recording3.archived.mp4"), testVideo),
            new PathAndContents(
                archiveDirectory.resolve("Nested1/Nested2/recording4.archived.mp4"), testVideo));
  }

  @Test
  @Order(23)
  void severalVideos_nestedDirectoryStructure_encodeAndArchiveToDifferentDirectory()
      throws IOException {
    // Given
    // videos to encode
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("recording1.mp4"), testVideo);
    createVideoAt(inputDirectory.resolve("recording2.mp4"), testVideo);
    createVideoAt(inputDirectory.resolve("Nested/recording3.mp4"), testVideo);
    createVideoAt(inputDirectory.resolve("Nested1/Nested2/recording4.mp4"), testVideo);

    Path outputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay Encoded"));

    Path archiveDirectory = createDirectoryAt(testDirectory.resolve("Gameplay Archive"));

    // When
    runApp(inputDirectory, outputDirectory, archiveDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            // encodings
            new PathAndContents(outputDirectory.resolve("recording1.cfr.mp4"), testVideoEncoded),
            new PathAndContents(outputDirectory.resolve("recording2.cfr.mp4"), testVideoEncoded),
            new PathAndContents(
                outputDirectory.resolve("Nested/recording3.cfr.mp4"), testVideoEncoded),
            new PathAndContents(
                outputDirectory.resolve("Nested1/Nested2/recording4.cfr.mp4"), testVideoEncoded),
            // archives
            new PathAndContents(archiveDirectory.resolve("recording1.archived.mp4"), testVideo),
            new PathAndContents(archiveDirectory.resolve("recording2.archived.mp4"), testVideo),
            new PathAndContents(
                archiveDirectory.resolve("Nested/recording3.archived.mp4"), testVideo),
            new PathAndContents(
                archiveDirectory.resolve("Nested1/Nested2/recording4.archived.mp4"), testVideo));
  }

  @Test
  @Order(30)
  void singleVideo_and_deletesIncompleteEncodingsAndArchives() throws IOException {
    // Given
    // video to encode
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("my video.mp4"), testVideo);

    // incomplete encodings
    createVideoAt(inputDirectory.resolve("recording - CFR (incomplete).mp4"), testVideo);
    createVideoAt(inputDirectory.resolve("recording2 - CFR (incomplete).mp4"), testVideo);

    // incomplete archives
    createVideoAt(inputDirectory.resolve("vid - Archived (incomplete).mp4"), testVideo);
    createVideoAt(inputDirectory.resolve("vid2 - Archived (incomplete).mp4"), testVideo);

    // When
    runApp(inputDirectory, inputDirectory, inputDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            new PathAndContents(inputDirectory.resolve("my video.cfr.mp4"), testVideoEncoded),
            // archive
            new PathAndContents(inputDirectory.resolve("my video.archived.mp4"), testVideo));
  }

  @Test
  @Order(31)
  void
      singleVideo_encodeAndArchiveToDifferentDirectory_and_deletesIncompleteEncodingsAndArchivesInAllDirectories()
          throws IOException {
    // Given
    // video to encode
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("my video.mp4"), testVideo);

    Path outputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay Encoded"));

    Path archiveDirectory = createDirectoryAt(testDirectory.resolve("Gameplay Archive"));

    // incomplete encodings
    createVideoAt(inputDirectory.resolve("recording - CFR (incomplete).mp4"), testVideo);
    createVideoAt(outputDirectory.resolve("recording - CFR (incomplete).mp4"), testVideo);
    createVideoAt(archiveDirectory.resolve("recording - CFR (incomplete).mp4"), testVideo);

    // incomplete archives
    createVideoAt(inputDirectory.resolve("vid - Archived (incomplete).mp4"), testVideo);
    createVideoAt(outputDirectory.resolve("vid - Archived (incomplete).mp4"), testVideo);
    createVideoAt(archiveDirectory.resolve("vid - Archived (incomplete).mp4"), testVideo);

    // When
    runApp(inputDirectory, outputDirectory, archiveDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            new PathAndContents(outputDirectory.resolve("my video.cfr.mp4"), testVideoEncoded),
            // archive
            new PathAndContents(archiveDirectory.resolve("my video.archived.mp4"), testVideo));
  }

  @Test
  @Order(40)
  void singleVideo_and_retainsCompleteEncodingsAndArchives() throws IOException {
    // Given
    // video to encode
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("my video.mp4"), testVideo);

    // unrelated encodings
    createVideoAt(inputDirectory.resolve("recording.cfr.mp4"), testVideoEncoded);
    createVideoAt(inputDirectory.resolve("recording2.cfr.mp4"), testVideoEncoded);

    // unrelated archives
    createVideoAt(inputDirectory.resolve("recording.archived.mp4"), testVideo);
    createVideoAt(inputDirectory.resolve("recording2.archived.mp4"), testVideo);

    // When
    runApp(inputDirectory, inputDirectory, inputDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            new PathAndContents(inputDirectory.resolve("my video.cfr.mp4"), testVideoEncoded),
            // archive
            new PathAndContents(inputDirectory.resolve("my video.archived.mp4"), testVideo),
            // unrelated encodings
            new PathAndContents(inputDirectory.resolve("recording.cfr.mp4"), testVideoEncoded),
            new PathAndContents(inputDirectory.resolve("recording2.cfr.mp4"), testVideoEncoded),
            // unrelated archives
            new PathAndContents(inputDirectory.resolve("recording.archived.mp4"), testVideo),
            new PathAndContents(inputDirectory.resolve("recording2.archived.mp4"), testVideo));
  }

  @Test
  @Order(41)
  void
      singleVideo_encodeAndArchiveToDifferentDirectory_and_retainsCompleteEncodingsAndArchivesInAllDirectories()
          throws IOException {
    // Given
    // video to encode
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("my video.mp4"), testVideo);

    Path outputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay Encoded"));

    Path archiveDirectory = createDirectoryAt(testDirectory.resolve("Gameplay Archive"));

    // unrelated encodings
    createVideoAt(inputDirectory.resolve("recording.cfr.mp4"), testVideoEncoded);
    createVideoAt(outputDirectory.resolve("recording.cfr.mp4"), testVideoEncoded);
    createVideoAt(archiveDirectory.resolve("recording.cfr.mp4"), testVideoEncoded);

    // unrelated archives
    createVideoAt(inputDirectory.resolve("recording.archived.mp4"), testVideo);
    createVideoAt(outputDirectory.resolve("recording.archived.mp4"), testVideo);
    createVideoAt(archiveDirectory.resolve("recording.archived.mp4"), testVideo);

    // When
    runApp(inputDirectory, outputDirectory, archiveDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            new PathAndContents(outputDirectory.resolve("my video.cfr.mp4"), testVideoEncoded),
            // archive
            new PathAndContents(archiveDirectory.resolve("my video.archived.mp4"), testVideo),
            // unrelated encodings
            new PathAndContents(inputDirectory.resolve("recording.cfr.mp4"), testVideoEncoded),
            new PathAndContents(outputDirectory.resolve("recording.cfr.mp4"), testVideoEncoded),
            new PathAndContents(archiveDirectory.resolve("recording.cfr.mp4"), testVideoEncoded),
            // unrelated archives
            new PathAndContents(inputDirectory.resolve("recording.archived.mp4"), testVideo),
            new PathAndContents(outputDirectory.resolve("recording.archived.mp4"), testVideo),
            new PathAndContents(archiveDirectory.resolve("recording.archived.mp4"), testVideo));
  }

  @Test
  @Order(50)
  void singleVideo_encodingAlreadyExists() throws IOException {
    // Given
    // video to encode
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("my video.mp4"), testVideo);

    // already encoded
    createVideoAt(inputDirectory.resolve("my video.cfr.mp4"), testVideoEncoded);

    // When
    runApp(inputDirectory, inputDirectory, inputDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            new PathAndContents(inputDirectory.resolve("my video.cfr.mp4"), testVideoEncoded),
            // archive
            new PathAndContents(inputDirectory.resolve("my video.archived.mp4"), testVideo));
  }

  @Test
  @Order(51)
  void singleVideo_archiveAlreadyExists() throws IOException {
    // Given
    // video to encode
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("my video.mp4"), testVideo);

    // already archived
    createVideoAt(inputDirectory.resolve("my video.archived.mp4"), testVideo);

    // When
    runApp(inputDirectory, inputDirectory, inputDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            new PathAndContents(inputDirectory.resolve("my video.cfr.mp4"), testVideoEncoded),
            // archive
            new PathAndContents(inputDirectory.resolve("my video.archived.mp4"), testVideo));
  }

  @Test
  @Order(52)
  void singleVideo_encodingAndArchiveAlreadyExists() throws IOException {
    // Given
    // video to encode
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("my video.mp4"), testVideo);

    // already encoded
    createVideoAt(inputDirectory.resolve("my video.cfr.mp4"), testVideoEncoded);

    // already archived
    createVideoAt(inputDirectory.resolve("my video.archived.mp4"), testVideo);

    // When
    runApp(inputDirectory, inputDirectory, inputDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            new PathAndContents(inputDirectory.resolve("my video.cfr.mp4"), testVideoEncoded),
            // archive
            new PathAndContents(inputDirectory.resolve("my video.archived.mp4"), testVideo));
  }

  @Test
  @Order(60)
  void singleVideo_archiveExistsButContentsDifferSoRetainsOriginal() throws IOException {
    // Given
    // video to encode
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("my video.mp4"), testVideo);

    // already archived but different contents
    createVideoAt(inputDirectory.resolve("my video.archived.mp4"), testVideo2);

    // When
    runApp(inputDirectory, inputDirectory, inputDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            // original
            new PathAndContents(inputDirectory.resolve("my video.mp4"), testVideo),
            // encoding
            new PathAndContents(inputDirectory.resolve("my video.cfr.mp4"), testVideoEncoded),
            // archive
            new PathAndContents(inputDirectory.resolve("my video.archived.mp4"), testVideo2));
  }

  @Test
  @Order(61)
  void singleVideo_encodingAlreadyExists_and_archiveExistsButContentsDifferSoRetainsOriginal()
      throws IOException {
    // Given
    // video to encode
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("my video.mp4"), testVideo);

    // already encoded
    createVideoAt(inputDirectory.resolve("my video.cfr.mp4"), testVideoEncoded);

    // already archived but different contents
    createVideoAt(inputDirectory.resolve("my video.archived.mp4"), testVideo2);

    // When
    runApp(inputDirectory, inputDirectory, inputDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            // original
            new PathAndContents(inputDirectory.resolve("my video.mp4"), testVideo),
            // encoding
            new PathAndContents(inputDirectory.resolve("my video.cfr.mp4"), testVideoEncoded),
            // archive
            new PathAndContents(inputDirectory.resolve("my video.archived.mp4"), testVideo2));
  }

  @Test
  @Order(100)
  void
      severalVideos_nestedDirectoryStructure_and_someEncodingsAndArchivesAlreadyExists_and_deletesIncompleteEncodingsAndArchives_and_retainsCompleteEncodingsAndArchives()
          throws IOException {
    // Given
    // videos to encode
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("recording1.mp4"), testVideo);
    createVideoAt(inputDirectory.resolve("recording2.mp4"), testVideo);
    createVideoAt(inputDirectory.resolve("Nested/recording3.mp4"), testVideo);
    createVideoAt(inputDirectory.resolve("Nested1/Nested2/recording4.mp4"), testVideo);

    // already encoded
    createVideoAt(inputDirectory.resolve("recording2.cfr.mp4"), testVideoEncoded);
    createVideoAt(inputDirectory.resolve("Nested/recording3.cfr.mp4"), testVideoEncoded);

    // already archived
    createVideoAt(inputDirectory.resolve("recording1.archived.mp4"), testVideo);
    createVideoAt(inputDirectory.resolve("Nested/recording3.archived.mp4"), testVideo);

    // incomplete encodings
    createVideoAt(inputDirectory.resolve("recording1 - CFR (incomplete).mp4"), testVideo);
    createVideoAt(
        inputDirectory.resolve("Nested1/Nested2/Nested3/other video - CFR (incomplete).mp4"),
        testVideo);

    // incomplete archives
    createVideoAt(inputDirectory.resolve("recording2 - Archived (incomplete).mp4"), testVideo);
    createVideoAt(
        inputDirectory.resolve("Nested1/Nested2/Nested3/random video - Archived (incomplete).mp4"),
        testVideo);

    // unrelated encodings
    createVideoAt(inputDirectory.resolve("Starcraft II/protoss.cfr.mp4"), testVideoEncoded);
    createVideoAt(
        inputDirectory.resolve("Starcraft II/Campaign/terran1.cfr.mp4"), testVideoEncoded);

    // unrelated archives
    createVideoAt(inputDirectory.resolve("League of Legends/ryze.archived.mp4"), testVideo);
    createVideoAt(
        inputDirectory.resolve("Path of Exile/Old builds/Discharge CoC.archived.mp4"), testVideo);

    // When
    runApp(inputDirectory, inputDirectory, inputDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            // encodings
            new PathAndContents(inputDirectory.resolve("recording1.cfr.mp4"), testVideoEncoded),
            new PathAndContents(inputDirectory.resolve("recording2.cfr.mp4"), testVideoEncoded),
            new PathAndContents(
                inputDirectory.resolve("Nested/recording3.cfr.mp4"), testVideoEncoded),
            new PathAndContents(
                inputDirectory.resolve("Nested1/Nested2/recording4.cfr.mp4"), testVideoEncoded),
            // archives
            new PathAndContents(inputDirectory.resolve("recording1.archived.mp4"), testVideo),
            new PathAndContents(inputDirectory.resolve("recording2.archived.mp4"), testVideo),
            new PathAndContents(
                inputDirectory.resolve("Nested/recording3.archived.mp4"), testVideo),
            new PathAndContents(
                inputDirectory.resolve("Nested1/Nested2/recording4.archived.mp4"), testVideo),
            // unrelated encodings
            new PathAndContents(
                inputDirectory.resolve("Starcraft II/protoss.cfr.mp4"), testVideoEncoded),
            new PathAndContents(
                inputDirectory.resolve("Starcraft II/Campaign/terran1.cfr.mp4"),
                testVideoEncoded),
            // unrelated archives
            new PathAndContents(
                inputDirectory.resolve("League of Legends/ryze.archived.mp4"), testVideo),
            new PathAndContents(
                inputDirectory.resolve("Path of Exile/Old builds/Discharge CoC.archived.mp4"),
                testVideo));
  }

  @Test
  @Order(101)
  void
      severalVideos_nestedDirectoryStructure_encodeAndArchiveToDifferentDirectory_and_someEncodingsAndArchivesAlreadyExists_and_deletesIncompleteEncodingsAndArchivesInAllDirectories_and_retainsCompleteEncodingsAndArchivesInAllDirectories()
          throws IOException {
    // Given
    // videos to encode
    Path inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    createVideoAt(inputDirectory.resolve("recording1.mp4"), testVideo);
    createVideoAt(inputDirectory.resolve("recording2.mp4"), testVideo);
    createVideoAt(inputDirectory.resolve("Nested/recording3.mp4"), testVideo);
    createVideoAt(inputDirectory.resolve("Nested1/Nested2/recording4.mp4"), testVideo);

    Path outputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay Encoded"));

    Path archiveDirectory = createDirectoryAt(testDirectory.resolve("Gameplay Archive"));

    // already encoded
    createVideoAt(outputDirectory.resolve("recording2.cfr.mp4"), testVideoEncoded);
    createVideoAt(outputDirectory.resolve("Nested/recording3.cfr.mp4"), testVideoEncoded);

    // already archived
    createVideoAt(archiveDirectory.resolve("recording1.archived.mp4"), testVideo);
    createVideoAt(archiveDirectory.resolve("Nested/recording3.archived.mp4"), testVideo);

    // incomplete encodings
    createVideoAt(inputDirectory.resolve("recording1 - CFR (incomplete).mp4"), testVideo);
    createVideoAt(outputDirectory.resolve("recording1 - CFR (incomplete).mp4"), testVideo);
    createVideoAt(archiveDirectory.resolve("recording1 - CFR (incomplete).mp4"), testVideo);

    // incomplete archives
    createVideoAt(inputDirectory.resolve("recording2 - Archived (incomplete).mp4"), testVideo);
    createVideoAt(outputDirectory.resolve("recording2 - Archived (incomplete).mp4"), testVideo);
    createVideoAt(archiveDirectory.resolve("recording2 - Archived (incomplete).mp4"), testVideo);

    // unrelated encodings
    createVideoAt(inputDirectory.resolve("recording.cfr.mp4"), testVideoEncoded);
    createVideoAt(outputDirectory.resolve("recording.cfr.mp4"), testVideoEncoded);
    createVideoAt(archiveDirectory.resolve("recording.cfr.mp4"), testVideoEncoded);

    // unrelated archives
    createVideoAt(inputDirectory.resolve("recording.archived.mp4"), testVideo);
    createVideoAt(outputDirectory.resolve("recording.archived.mp4"), testVideo);
    createVideoAt(archiveDirectory.resolve("recording.archived.mp4"), testVideo);

    // When
    runApp(inputDirectory, outputDirectory, archiveDirectory);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            // encodings
            new PathAndContents(outputDirectory.resolve("recording1.cfr.mp4"), testVideoEncoded),
            new PathAndContents(outputDirectory.resolve("recording2.cfr.mp4"), testVideoEncoded),
            new PathAndContents(
                outputDirectory.resolve("Nested/recording3.cfr.mp4"), testVideoEncoded),
            new PathAndContents(
                outputDirectory.resolve("Nested1/Nested2/recording4.cfr.mp4"), testVideoEncoded),
            // archives
            new PathAndContents(archiveDirectory.resolve("recording1.archived.mp4"), testVideo),
            new PathAndContents(archiveDirectory.resolve("recording2.archived.mp4"), testVideo),
            new PathAndContents(
                archiveDirectory.resolve("Nested/recording3.archived.mp4"), testVideo),
            new PathAndContents(
                archiveDirectory.resolve("Nested1/Nested2/recording4.archived.mp4"), testVideo),
            // unrelated encodings
            new PathAndContents(inputDirectory.resolve("recording.cfr.mp4"), testVideoEncoded),
            new PathAndContents(outputDirectory.resolve("recording.cfr.mp4"), testVideoEncoded),
            new PathAndContents(archiveDirectory.resolve("recording.cfr.mp4"), testVideoEncoded),
            // unrelated archives
            new PathAndContents(inputDirectory.resolve("recording.archived.mp4"), testVideo),
            new PathAndContents(outputDirectory.resolve("recording.archived.mp4"), testVideo),
            new PathAndContents(archiveDirectory.resolve("recording.archived.mp4"), testVideo));
  }

  @CanIgnoreReturnValue
  private Path createDirectoryAt(Path path) throws IOException {
    // not returning result of Files.createDirectories - it's absolute rather than relative, which
    // breaks the tests
    Files.createDirectories(path);
    return path;
  }

  @CanIgnoreReturnValue
  private Path createVideoAt(Path path, Path videoToCopy) throws IOException {
    Files.createDirectories(checkNotNull(path.getParent()));
    Files.copy(videoToCopy, path);
    return path;
  }

  private void runApp(Path inputDirectory, Path outputDirectory, Path archiveDirectory) {
    App.main(inputDirectory.toString(), outputDirectory.toString(), archiveDirectory.toString());
  }

  private IterableSubject.UsingCorrespondence<Path, PathAndContents> assertThatTestDirectory()
      throws IOException {
    return assertThat(Files.walk(testDirectory).filter(Files::isRegularFile).toList())
        .comparingElementsUsing(PathAndContents.EQUIVALENCE);
  }

  private record PathAndContents(Path path, Path contents) {
    static final Correspondence<Path, PathAndContents> EQUIVALENCE =
        Correspondence.from(PathAndContents::recordsEquivalent, "is equivalent to")
            .formattingDiffsUsing(PathAndContents::formatRecordDiff);

    static final double PERCENT_MISMATCH_TOLERANCE = 0.01;

    static boolean recordsEquivalent(Path actual, PathAndContents expected) {
      return actual.equals(expected.path)
          && percentMismatch(actual, expected.contents) < PERCENT_MISMATCH_TOLERANCE;
    }

    static String formatRecordDiff(Path actual, PathAndContents expected) {
      if (!actual.equals(expected.path)) {
        return "paths not equal";
      }
      double percentMismatch = percentMismatch(actual, expected.contents);
      if (percentMismatch >= PERCENT_MISMATCH_TOLERANCE) {
        return "contents not similar, percentMismatch=%s%%".formatted(percentMismatch * 100);
      }
      throw new AssertionError("Unreachable");
    }

    // HandBrake is not deterministic (encoding doesn't always produce the exact same output) so
    // need a method to test file contents are similar.
    static double percentMismatch(Path path1, Path path2) {
      try {
        byte[] bytes1 = Files.readAllBytes(path1);
        byte[] bytes2 = Files.readAllBytes(path2);
        // pad arrays to same length
        byte[] paddedBytes1 = Arrays.copyOf(bytes1, Math.max(bytes1.length, bytes2.length));
        byte[] paddedBytes2 = Arrays.copyOf(bytes2, Math.max(bytes1.length, bytes2.length));

        long mismatchCount =
            IntStream.range(0, paddedBytes1.length)
                .filter(i -> paddedBytes1[i] != paddedBytes2[i])
                .count();

        return (double) mismatchCount / paddedBytes1.length;
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
  }
}
