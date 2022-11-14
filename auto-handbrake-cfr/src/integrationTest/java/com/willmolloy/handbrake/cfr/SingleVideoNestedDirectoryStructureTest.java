package com.willmolloy.handbrake.cfr;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Nested directory structure scenarios.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
class SingleVideoNestedDirectoryStructureTest extends BaseIntegrationTest {

  @Test
  void singleVideo_nestedDirectoryStructure() throws Exception {
    // Given
    // video to encode
    createVideoAt(inputDirectory.resolve("League of Legends/ranked_game1.mp4"), unencodedVideo);

    // When
    boolean result = runApp(inputDirectory, inputDirectory, inputDirectory);

    // Then
    assertThat(result).isTrue();
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            pathAndContents(
                inputDirectory.resolve("League of Legends/ranked_game1.cfr.mp4"), encodedVideo),
            // archive
            pathAndContents(
                inputDirectory.resolve("League of Legends/ranked_game1.mp4"), unencodedVideo));
  }

  @Test
  void singleVideo_nestedDirectoryStructure_encodeToDifferentDirectory() throws Exception {
    // Given
    // video to encode
    createVideoAt(inputDirectory.resolve("StarCraft II/protoss.mp4"), unencodedVideo);

    // When
    boolean result = runApp(inputDirectory, outputDirectory, inputDirectory);

    // Then
    assertThat(result).isTrue();
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            pathAndContents(outputDirectory.resolve("StarCraft II/protoss.cfr.mp4"), encodedVideo),
            // archive
            pathAndContents(inputDirectory.resolve("StarCraft II/protoss.mp4"), unencodedVideo));
  }

  @Test
  void singleVideo_nestedDirectoryStructure_archiveToDifferentDirectory() throws Exception {
    // Given
    // video to encode
    createVideoAt(inputDirectory.resolve("Path of Exile/vaal spark templar.mp4"), unencodedVideo);

    // When
    boolean result = runApp(inputDirectory, inputDirectory, archiveDirectory);

    // Then
    assertThat(result).isTrue();
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            pathAndContents(
                inputDirectory.resolve("Path of Exile/vaal spark templar.cfr.mp4"), encodedVideo),
            // archive
            pathAndContents(
                archiveDirectory.resolve("Path of Exile/vaal spark templar.mp4"), unencodedVideo));
  }

  @Test
  void singleVideo_nestedDirectoryStructure_encodeAndArchiveToDifferentDirectory()
      throws Exception {
    // Given
    // video to encode
    createVideoAt(
        inputDirectory.resolve("Halo Infinite/Legendary Campaign/1st mission.mp4"), unencodedVideo);

    // When
    boolean result = runApp(inputDirectory, outputDirectory, archiveDirectory);

    // Then
    assertThat(result).isTrue();
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            pathAndContents(
                outputDirectory.resolve("Halo Infinite/Legendary Campaign/1st mission.cfr.mp4"),
                encodedVideo),
            // archive
            pathAndContents(
                archiveDirectory.resolve("Halo Infinite/Legendary Campaign/1st mission.mp4"),
                unencodedVideo));
  }
}
