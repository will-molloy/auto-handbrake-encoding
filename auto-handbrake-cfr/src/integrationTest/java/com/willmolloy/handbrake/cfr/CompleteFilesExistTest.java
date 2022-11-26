package com.willmolloy.handbrake.cfr;

import static com.google.common.truth.Truth.assertThat;

import java.nio.file.Path;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

/**
 * Scenarios where complete (encoding/archive) files exist.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
class CompleteFilesExistTest extends BaseIntegrationTest {

  @ParameterizedTest
  @ArgumentsSource(EncodeAndArchiveToSameDirectory.class)
  @ArgumentsSource(EncodeToDifferentDirectory.class)
  @ArgumentsSource(ArchiveToDifferentDirectory.class)
  @ArgumentsSource(EncodeAndArchiveToDifferentDirectory.class)
  void when_encodingExists_continuesByOverwritingExistingEncoding(
      Path inputDirectory, Path outputDirectory, Path archiveDirectory) throws Exception {
    // Given
    // video to encode
    createVideoAt(inputDirectory.resolve("my video.mp4"), unencodedVideo1);

    // already encoded
    createVideoAt(outputDirectory.resolve("my video.cfr.mp4"), encodedVideo1);

    // When
    boolean result = runApp(inputDirectory, outputDirectory, archiveDirectory);

    // Then
    assertThat(result).isTrue();
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            pathAndContents(outputDirectory.resolve("my video.cfr.mp4"), encodedVideo1),
            // archive
            pathAndContents(archiveDirectory.resolve("my video.mp4"), unencodedVideo1));
  }

  @ParameterizedTest
  @ArgumentsSource(EncodeAndArchiveToSameDirectory.class)
  @ArgumentsSource(EncodeToDifferentDirectory.class)
  @ArgumentsSource(ArchiveToDifferentDirectory.class)
  @ArgumentsSource(EncodeAndArchiveToDifferentDirectory.class)
  void when_encodingExistsButContentsDiffer_abortsEncoding_andSkipsArchiving(
      Path inputDirectory, Path outputDirectory, Path archiveDirectory) throws Exception {
    // Given
    // video to encode
    createVideoAt(inputDirectory.resolve("my video.mp4"), unencodedVideo1);

    // already encoded but different contents
    createVideoAt(outputDirectory.resolve("my video.cfr.mp4"), encodedVideo2);

    // When
    boolean result = runApp(inputDirectory, outputDirectory, archiveDirectory);

    // Then
    assertThat(result).isFalse();
    assertThatTestDirectory()
        .containsExactly(
            // original
            pathAndContents(inputDirectory.resolve("my video.mp4"), unencodedVideo1),
            // temp encoding
            pathAndContents(outputDirectory.resolve("my video.cfr.mp4.part"), encodedVideo1),
            // encoding (not overwritten)
            pathAndContents(outputDirectory.resolve("my video.cfr.mp4"), encodedVideo2));
  }

  @ParameterizedTest
  @ArgumentsSource(ArchiveToDifferentDirectory.class)
  @ArgumentsSource(EncodeAndArchiveToDifferentDirectory.class)
  void when_archiveExists_stillDeletesOriginal(
      Path inputDirectory, Path outputDirectory, Path archiveDirectory) throws Exception {
    // Given
    // video to encode
    createVideoAt(inputDirectory.resolve("my video.mp4"), unencodedVideo1);

    // already archived
    createVideoAt(archiveDirectory.resolve("my video.mp4"), unencodedVideo1);

    // When
    boolean result = runApp(inputDirectory, outputDirectory, archiveDirectory);

    // Then
    assertThat(result).isTrue();
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            pathAndContents(outputDirectory.resolve("my video.cfr.mp4"), encodedVideo1),
            // archive
            pathAndContents(archiveDirectory.resolve("my video.mp4"), unencodedVideo1));
  }

  @ParameterizedTest
  @ArgumentsSource(ArchiveToDifferentDirectory.class)
  @ArgumentsSource(EncodeAndArchiveToDifferentDirectory.class)
  void when_archiveExistsButContentsDiffer_skipsArchiving(
      Path inputDirectory, Path outputDirectory, Path archiveDirectory) throws Exception {
    // Given
    // video to encode
    createVideoAt(inputDirectory.resolve("my video.mp4"), unencodedVideo1);

    // already archived but different contents
    createVideoAt(archiveDirectory.resolve("my video.mp4"), unencodedVideo2);

    // When
    boolean result = runApp(inputDirectory, outputDirectory, archiveDirectory);

    // Then
    assertThat(result).isFalse();
    assertThatTestDirectory()
        .containsExactly(
            // original
            pathAndContents(inputDirectory.resolve("my video.mp4"), unencodedVideo1),
            // encoding
            pathAndContents(outputDirectory.resolve("my video.cfr.mp4"), encodedVideo1),
            // archive (not overwritten)
            pathAndContents(archiveDirectory.resolve("my video.mp4"), unencodedVideo2));
  }

  @ParameterizedTest
  @ArgumentsSource(ArchiveToDifferentDirectory.class)
  @ArgumentsSource(EncodeAndArchiveToDifferentDirectory.class)
  void
      when_encodingExists_and_archiveExists_continuesByOverwritingExistingEncoding_andStillDeletesOriginal(
          Path inputDirectory, Path outputDirectory, Path archiveDirectory) throws Exception {
    // Given
    // video to encode
    createVideoAt(inputDirectory.resolve("my video.mp4"), unencodedVideo1);

    // already encoded
    createVideoAt(outputDirectory.resolve("my video.cfr.mp4"), encodedVideo1);

    // already archived
    createVideoAt(archiveDirectory.resolve("my video.mp4"), unencodedVideo1);

    // When
    boolean result = runApp(inputDirectory, outputDirectory, archiveDirectory);

    // Then
    assertThat(result).isTrue();
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            pathAndContents(outputDirectory.resolve("my video.cfr.mp4"), encodedVideo1),
            // archive
            pathAndContents(archiveDirectory.resolve("my video.mp4"), unencodedVideo1));
  }

  @ParameterizedTest
  @ArgumentsSource(ArchiveToDifferentDirectory.class)
  @ArgumentsSource(EncodeAndArchiveToDifferentDirectory.class)
  void when_encodingExistsButContentsDiffer_and_archiveExists_abortsEncoding_andSkipsArchiving(
      Path inputDirectory, Path outputDirectory, Path archiveDirectory) throws Exception {
    // Given
    // video to encode
    createVideoAt(inputDirectory.resolve("my video.mp4"), unencodedVideo1);

    // already encoded but different contents
    createVideoAt(outputDirectory.resolve("my video.cfr.mp4"), encodedVideo2);

    // already archived
    createVideoAt(archiveDirectory.resolve("my video.mp4"), unencodedVideo1);

    // When
    boolean result = runApp(inputDirectory, outputDirectory, archiveDirectory);

    // Then
    assertThat(result).isFalse();
    assertThatTestDirectory()
        .containsExactly(
            // original
            pathAndContents(inputDirectory.resolve("my video.mp4"), unencodedVideo1),
            // temp encoding
            pathAndContents(outputDirectory.resolve("my video.cfr.mp4.part"), encodedVideo1),
            // encoding (not overwritten)
            pathAndContents(outputDirectory.resolve("my video.cfr.mp4"), encodedVideo2),
            // archive (not overwritten - but it doesn't matter, same contents)
            pathAndContents(archiveDirectory.resolve("my video.mp4"), unencodedVideo1));
  }

  @ParameterizedTest
  @ArgumentsSource(ArchiveToDifferentDirectory.class)
  @ArgumentsSource(EncodeAndArchiveToDifferentDirectory.class)
  void
      when_encodingExists_and_archiveExistsButContentsDiffer_continuesByOverwritingExistingEncoding_andSkipsArchiving(
          Path inputDirectory, Path outputDirectory, Path archiveDirectory) throws Exception {
    // Given
    // video to encode
    createVideoAt(inputDirectory.resolve("my video.mp4"), unencodedVideo1);

    // already encoded
    createVideoAt(outputDirectory.resolve("my video.cfr.mp4"), encodedVideo1);

    // already archived but different contents
    createVideoAt(archiveDirectory.resolve("my video.mp4"), unencodedVideo2);

    // When
    boolean result = runApp(inputDirectory, outputDirectory, archiveDirectory);

    // Then
    assertThat(result).isFalse();
    assertThatTestDirectory()
        .containsExactly(
            // original
            pathAndContents(inputDirectory.resolve("my video.mp4"), unencodedVideo1),
            // encoding
            pathAndContents(outputDirectory.resolve("my video.cfr.mp4"), encodedVideo1),
            // archive (not overwritten)
            pathAndContents(archiveDirectory.resolve("my video.mp4"), unencodedVideo2));
  }

  @ParameterizedTest
  @ArgumentsSource(ArchiveToDifferentDirectory.class)
  @ArgumentsSource(EncodeAndArchiveToDifferentDirectory.class)
  void
      when_encodingExistsButContentsDiffer_and_archiveExistsButContentsDiffer_abortsEncoding_andSkipsArchiving(
          Path inputDirectory, Path outputDirectory, Path archiveDirectory) throws Exception {
    // Given
    // video to encode
    createVideoAt(inputDirectory.resolve("my video.mp4"), unencodedVideo1);

    // already encoded but different contents
    createVideoAt(outputDirectory.resolve("my video.cfr.mp4"), encodedVideo2);

    // already archived but different contents
    createVideoAt(archiveDirectory.resolve("my video.mp4"), unencodedVideo2);

    // When
    boolean result = runApp(inputDirectory, outputDirectory, archiveDirectory);

    // Then
    assertThat(result).isFalse();
    assertThatTestDirectory()
        .containsExactly(
            // original
            pathAndContents(inputDirectory.resolve("my video.mp4"), unencodedVideo1),
            // temp encoding
            pathAndContents(outputDirectory.resolve("my video.cfr.mp4.part"), encodedVideo1),
            // encoding (not overwritten)
            pathAndContents(outputDirectory.resolve("my video.cfr.mp4"), encodedVideo2),
            // archive (not overwritten)
            pathAndContents(archiveDirectory.resolve("my video.mp4"), unencodedVideo2));
  }
}
