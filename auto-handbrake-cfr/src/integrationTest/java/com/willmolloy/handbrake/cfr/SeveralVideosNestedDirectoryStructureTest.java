package com.willmolloy.handbrake.cfr;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Encoding several videos.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
class SeveralVideosNestedDirectoryStructureTest extends BaseIntegrationTest {

  @Test
  void severalVideos_nestedDirectoryStructure() throws Exception {
    // Given
    // videos to encode
    createVideoAt(inputDirectory.resolve("recording1.mp4"), unencodedVideo);
    createVideoAt(inputDirectory.resolve("recording2.mp4"), unencodedVideo);
    createVideoAt(inputDirectory.resolve("Nested/recording3.mp4"), unencodedVideo);
    createVideoAt(inputDirectory.resolve("Nested1/Nested2/recording4.mp4"), unencodedVideo);

    // When
    boolean result = runApp(inputDirectory, inputDirectory, inputDirectory);

    // Then
    assertThat(result).isTrue();
    assertThatTestDirectory()
        .containsExactly(
            // encodings
            pathAndContents(inputDirectory.resolve("recording1.cfr.mp4"), encodedVideo),
            pathAndContents(inputDirectory.resolve("recording2.cfr.mp4"), encodedVideo),
            pathAndContents(inputDirectory.resolve("Nested/recording3.cfr.mp4"), encodedVideo),
            pathAndContents(
                inputDirectory.resolve("Nested1/Nested2/recording4.cfr.mp4"), encodedVideo),
            // archives
            pathAndContents(inputDirectory.resolve("recording1.mp4"), unencodedVideo),
            pathAndContents(inputDirectory.resolve("recording2.mp4"), unencodedVideo),
            pathAndContents(inputDirectory.resolve("Nested/recording3.mp4"), unencodedVideo),
            pathAndContents(
                inputDirectory.resolve("Nested1/Nested2/recording4.mp4"), unencodedVideo));
  }

  @Test
  void severalVideos_nestedDirectoryStructure_encodeToDifferentDirectory() throws Exception {
    // Given
    // videos to encode
    createVideoAt(inputDirectory.resolve("recording1.mp4"), unencodedVideo);
    createVideoAt(inputDirectory.resolve("recording2.mp4"), unencodedVideo);
    createVideoAt(inputDirectory.resolve("Nested/recording3.mp4"), unencodedVideo);
    createVideoAt(inputDirectory.resolve("Nested1/Nested2/recording4.mp4"), unencodedVideo);

    // When
    boolean result = runApp(inputDirectory, outputDirectory, inputDirectory);

    // Then
    assertThat(result).isTrue();
    assertThatTestDirectory()
        .containsExactly(
            // encodings
            pathAndContents(outputDirectory.resolve("recording1.cfr.mp4"), encodedVideo),
            pathAndContents(outputDirectory.resolve("recording2.cfr.mp4"), encodedVideo),
            pathAndContents(outputDirectory.resolve("Nested/recording3.cfr.mp4"), encodedVideo),
            pathAndContents(
                outputDirectory.resolve("Nested1/Nested2/recording4.cfr.mp4"), encodedVideo),
            // archives
            pathAndContents(inputDirectory.resolve("recording1.mp4"), unencodedVideo),
            pathAndContents(inputDirectory.resolve("recording2.mp4"), unencodedVideo),
            pathAndContents(inputDirectory.resolve("Nested/recording3.mp4"), unencodedVideo),
            pathAndContents(
                inputDirectory.resolve("Nested1/Nested2/recording4.mp4"), unencodedVideo));
  }

  @Test
  void severalVideos_nestedDirectoryStructure_archiveToDifferentDirectory() throws Exception {
    // Given
    // videos to encode
    createVideoAt(inputDirectory.resolve("recording1.mp4"), unencodedVideo);
    createVideoAt(inputDirectory.resolve("recording2.mp4"), unencodedVideo);
    createVideoAt(inputDirectory.resolve("Nested/recording3.mp4"), unencodedVideo);
    createVideoAt(inputDirectory.resolve("Nested1/Nested2/recording4.mp4"), unencodedVideo);

    // When
    boolean result = runApp(inputDirectory, inputDirectory, archiveDirectory);

    // Then
    assertThat(result).isTrue();
    assertThatTestDirectory()
        .containsExactly(
            // encodings
            pathAndContents(inputDirectory.resolve("recording1.cfr.mp4"), encodedVideo),
            pathAndContents(inputDirectory.resolve("recording2.cfr.mp4"), encodedVideo),
            pathAndContents(inputDirectory.resolve("Nested/recording3.cfr.mp4"), encodedVideo),
            pathAndContents(
                inputDirectory.resolve("Nested1/Nested2/recording4.cfr.mp4"), encodedVideo),
            // archives
            pathAndContents(archiveDirectory.resolve("recording1.mp4"), unencodedVideo),
            pathAndContents(archiveDirectory.resolve("recording2.mp4"), unencodedVideo),
            pathAndContents(archiveDirectory.resolve("Nested/recording3.mp4"), unencodedVideo),
            pathAndContents(
                archiveDirectory.resolve("Nested1/Nested2/recording4.mp4"), unencodedVideo));
  }

  @Test
  void severalVideos_nestedDirectoryStructure_encodeAndArchiveToDifferentDirectory()
      throws Exception {
    // Given
    // videos to encode
    createVideoAt(inputDirectory.resolve("recording1.mp4"), unencodedVideo);
    createVideoAt(inputDirectory.resolve("recording2.mp4"), unencodedVideo);
    createVideoAt(inputDirectory.resolve("Nested/recording3.mp4"), unencodedVideo);
    createVideoAt(inputDirectory.resolve("Nested1/Nested2/recording4.mp4"), unencodedVideo);

    // When
    boolean result = runApp(inputDirectory, outputDirectory, archiveDirectory);

    // Then
    assertThat(result).isTrue();
    assertThatTestDirectory()
        .containsExactly(
            // encodings
            pathAndContents(outputDirectory.resolve("recording1.cfr.mp4"), encodedVideo),
            pathAndContents(outputDirectory.resolve("recording2.cfr.mp4"), encodedVideo),
            pathAndContents(outputDirectory.resolve("Nested/recording3.cfr.mp4"), encodedVideo),
            pathAndContents(
                outputDirectory.resolve("Nested1/Nested2/recording4.cfr.mp4"), encodedVideo),
            // archives
            pathAndContents(archiveDirectory.resolve("recording1.mp4"), unencodedVideo),
            pathAndContents(archiveDirectory.resolve("recording2.mp4"), unencodedVideo),
            pathAndContents(archiveDirectory.resolve("Nested/recording3.mp4"), unencodedVideo),
            pathAndContents(
                archiveDirectory.resolve("Nested1/Nested2/recording4.mp4"), unencodedVideo));
  }
}
