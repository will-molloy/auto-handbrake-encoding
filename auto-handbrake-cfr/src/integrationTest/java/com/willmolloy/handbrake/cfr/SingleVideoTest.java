package com.willmolloy.handbrake.cfr;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Single video scenarios (simplest case).
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SingleVideoTest extends BaseIntegrationTest {

  @Test
  void singleVideo() throws Exception {
    // Given
    // video to encode
    createVideoAt(inputDirectory.resolve("my video.mp4"), unencodedVideo);

    // When
    boolean result = runApp(inputDirectory, inputDirectory, inputDirectory);

    // Then
    assertThat(result).isTrue();
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            pathAndContents(inputDirectory.resolve("my video.cfr.mp4"), encodedVideo),
            // archive
            pathAndContents(inputDirectory.resolve("my video.mp4"), unencodedVideo));
  }

  @Test
  void singleVideo_encodeToDifferentDirectory() throws Exception {
    // Given
    // video to encode
    createVideoAt(inputDirectory.resolve("recording.mp4"), unencodedVideo);

    // When
    boolean result = runApp(inputDirectory, outputDirectory, inputDirectory);

    // Then
    assertThat(result).isTrue();
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            pathAndContents(outputDirectory.resolve("recording.cfr.mp4"), encodedVideo),
            // archive
            pathAndContents(inputDirectory.resolve("recording.mp4"), unencodedVideo));
  }

  @Test
  void singleVideo_archiveToDifferentDirectory() throws Exception {
    // Given
    // video to encode
    createVideoAt(inputDirectory.resolve("vid1.mp4"), unencodedVideo);

    // When
    boolean result = runApp(inputDirectory, inputDirectory, archiveDirectory);

    // Then
    assertThat(result).isTrue();
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            pathAndContents(inputDirectory.resolve("vid1.cfr.mp4"), encodedVideo),
            // archive
            pathAndContents(archiveDirectory.resolve("vid1.mp4"), unencodedVideo));
  }

  @Test
  void singleVideo_encodeAndArchiveToDifferentDirectory() throws Exception {
    // Given
    // video to encode
    createVideoAt(inputDirectory.resolve("recording1.mp4"), unencodedVideo);

    // When
    boolean result = runApp(inputDirectory, outputDirectory, archiveDirectory);

    // Then
    assertThat(result).isTrue();
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            pathAndContents(outputDirectory.resolve("recording1.cfr.mp4"), encodedVideo),
            // archive
            pathAndContents(archiveDirectory.resolve("recording1.mp4"), unencodedVideo));
  }
}
