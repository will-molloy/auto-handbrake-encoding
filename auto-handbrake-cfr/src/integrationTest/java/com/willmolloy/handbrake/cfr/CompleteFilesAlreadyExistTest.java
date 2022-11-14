package com.willmolloy.handbrake.cfr;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Scenarios where complete (encoding/archive) files exist.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
class CompleteFilesAlreadyExistTest extends BaseIntegrationTest {

  @Test
  void singleVideo_encodingAlreadyExists() throws Exception {
    // Given
    // video to encode
    createVideoAt(inputDirectory.resolve("my video.mp4"), unencodedVideo);

    // already encoded
    createVideoAt(inputDirectory.resolve("my video.cfr.mp4"), encodedVideo);

    // When
    boolean result = runApp(inputDirectory, inputDirectory, inputDirectory);

    // Then
    assertThat(result).isFalse();
    assertThatTestDirectory()
        .containsExactly(
            // original
            pathAndContents(inputDirectory.resolve("my video.mp4"), unencodedVideo),
            // encoding
            pathAndContents(inputDirectory.resolve("my video.cfr.mp4"), encodedVideo));
  }

  @Test
  void singleVideo_encodeToDifferentDirectory_encodingAlreadyExists() {}

  @Test
  void singleVideo_archiveToDifferentDirectory_archiveAlreadyExists() throws Exception {
    // Given
    // video to encode
    createVideoAt(inputDirectory.resolve("my video.mp4"), unencodedVideo);

    // already archived
    createVideoAt(archiveDirectory.resolve("my video.mp4"), unencodedVideo);

    // When
    boolean result = runApp(inputDirectory, inputDirectory, archiveDirectory);

    // Then
    assertThat(result).isTrue();
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            pathAndContents(inputDirectory.resolve("my video.cfr.mp4"), encodedVideo),
            // archive
            pathAndContents(archiveDirectory.resolve("my video.mp4"), unencodedVideo));
  }

  @Test
  void singleVideo_encodeAndArchiveToDifferentDirectory_archiveAlreadyExists() {}

  @Test
  void singleVideo_archiveToDifferentDirectory_encodingAndArchiveAlreadyExists() throws Exception {
    // Given
    // video to encode
    createVideoAt(inputDirectory.resolve("my video.mp4"), unencodedVideo);

    // already encoded
    createVideoAt(inputDirectory.resolve("my video.cfr.mp4"), encodedVideo);

    // already archived
    createVideoAt(archiveDirectory.resolve("my video.mp4"), unencodedVideo);

    // When
    boolean result = runApp(inputDirectory, inputDirectory, archiveDirectory);

    // Then
    assertThat(result).isFalse();
    assertThatTestDirectory()
        .containsExactly(
            // original
            pathAndContents(inputDirectory.resolve("my video.mp4"), unencodedVideo),
            // encoding
            pathAndContents(inputDirectory.resolve("my video.cfr.mp4"), encodedVideo),
            // archive
            pathAndContents(archiveDirectory.resolve("my video.mp4"), unencodedVideo));
  }

  @Test
  void singleVideo_encodeAndArchiveToDifferentDirectory_encodingAndArchiveAlreadyExists() {}

  @Test
  void singleVideo_archiveToDifferentDirectory_archiveExistsButContentsDiffer() throws Exception {
    // Given
    // video to encode
    createVideoAt(inputDirectory.resolve("my video.mp4"), unencodedVideo);

    // already archived but different contents
    createVideoAt(archiveDirectory.resolve("my video.mp4"), unencodedVideo2);

    // When
    boolean result = runApp(inputDirectory, inputDirectory, archiveDirectory);

    // Then
    assertThat(result).isFalse();
    assertThatTestDirectory()
        .containsExactly(
            // original
            pathAndContents(inputDirectory.resolve("my video.mp4"), unencodedVideo),
            // encoding
            pathAndContents(inputDirectory.resolve("my video.cfr.mp4"), encodedVideo),
            // archive
            pathAndContents(archiveDirectory.resolve("my video.mp4"), unencodedVideo2));
  }

  @Test
  void singleVideo_encodeAndArchiveToDifferentDirectory_archiveExistsButContentsDiffer() {}

  @Test
  void
      singleVideo_archiveToDifferentDirectory_encodingAlreadyExists_archiveExistsButContentsDiffer()
          throws Exception {
    // Given
    // video to encode
    createVideoAt(inputDirectory.resolve("my video.mp4"), unencodedVideo);

    // already encoded
    createVideoAt(inputDirectory.resolve("my video.cfr.mp4"), encodedVideo);

    // already archived but different contents
    createVideoAt(archiveDirectory.resolve("my video.mp4"), unencodedVideo2);

    // When
    boolean result = runApp(inputDirectory, inputDirectory, archiveDirectory);

    // Then
    assertThat(result).isFalse();
    assertThatTestDirectory()
        .containsExactly(
            // original
            pathAndContents(inputDirectory.resolve("my video.mp4"), unencodedVideo),
            // encoding
            pathAndContents(inputDirectory.resolve("my video.cfr.mp4"), encodedVideo),
            // archive
            pathAndContents(archiveDirectory.resolve("my video.mp4"), unencodedVideo2));
  }

  @Test
  void
      singleVideo_encodeAndArchiveToDifferentDirectory_encodingAlreadyExists_archiveExistsButContentsDiffer() {}
}
