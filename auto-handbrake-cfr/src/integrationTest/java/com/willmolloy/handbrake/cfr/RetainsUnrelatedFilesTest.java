package com.willmolloy.handbrake.cfr;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Scenarios where unrelated files exist.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
class RetainsUnrelatedFilesTest extends BaseIntegrationTest {

  @Test
  void singleVideo_retainsCompleteEncodingsAndArchives() throws Exception {
    // Given
    // video to encode
    createVideoAt(inputDirectory.resolve("my video.mp4"), unencodedVideo);

    // unrelated encodings
    createVideoAt(inputDirectory.resolve("recording.cfr.mp4"), encodedVideo);
    createVideoAt(inputDirectory.resolve("recording2.cfr.mp4"), encodedVideo);

    // unrelated archives
    createVideoAt(inputDirectory.resolve("recording.mp4"), unencodedVideo);
    createVideoAt(inputDirectory.resolve("recording2.mp4"), unencodedVideo);

    // When
    boolean result = runApp(inputDirectory, inputDirectory, inputDirectory);

    // Then
    assertThat(result).isFalse();
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            pathAndContents(inputDirectory.resolve("my video.cfr.mp4"), encodedVideo),
            // archive
            pathAndContents(inputDirectory.resolve("my video.mp4"), unencodedVideo),
            // unrelated encodings
            pathAndContents(inputDirectory.resolve("recording.cfr.mp4"), encodedVideo),
            pathAndContents(inputDirectory.resolve("recording2.cfr.mp4"), encodedVideo),
            // unrelated archives
            pathAndContents(inputDirectory.resolve("recording.mp4"), unencodedVideo),
            pathAndContents(inputDirectory.resolve("recording2.mp4"), unencodedVideo));
  }

  @Test
  void
      singleVideo_encodeAndArchiveToDifferentDirectory_retainsCompleteEncodingsAndArchivesInAllDirectories()
          throws Exception {
    // Given
    // video to encode
    createVideoAt(inputDirectory.resolve("my video.mp4"), unencodedVideo);

    // unrelated encodings
    createVideoAt(inputDirectory.resolve("recording.cfr.mp4"), encodedVideo);
    createVideoAt(outputDirectory.resolve("recording.cfr.mp4"), encodedVideo);
    createVideoAt(archiveDirectory.resolve("recording.cfr.mp4"), encodedVideo);

    // unrelated archives
    createVideoAt(inputDirectory.resolve("recording.mp4"), unencodedVideo);
    createVideoAt(outputDirectory.resolve("recording.mp4"), unencodedVideo);
    createVideoAt(archiveDirectory.resolve("recording.mp4"), unencodedVideo);

    // When
    boolean result = runApp(inputDirectory, outputDirectory, archiveDirectory);

    // Then
    assertThat(result).isFalse();
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            pathAndContents(outputDirectory.resolve("my video.cfr.mp4"), encodedVideo),
            // archive
            pathAndContents(archiveDirectory.resolve("my video.mp4"), unencodedVideo),
            // unrelated encodings
            pathAndContents(inputDirectory.resolve("recording.cfr.mp4"), encodedVideo),
            pathAndContents(outputDirectory.resolve("recording.cfr.mp4"), encodedVideo),
            pathAndContents(archiveDirectory.resolve("recording.cfr.mp4"), encodedVideo),
            // unrelated archives
            pathAndContents(inputDirectory.resolve("recording.mp4"), unencodedVideo),
            pathAndContents(outputDirectory.resolve("recording.mp4"), unencodedVideo),
            pathAndContents(archiveDirectory.resolve("recording.mp4"), unencodedVideo));
  }
}
