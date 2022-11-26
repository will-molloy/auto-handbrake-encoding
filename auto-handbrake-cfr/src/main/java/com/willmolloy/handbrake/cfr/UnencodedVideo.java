package com.willmolloy.handbrake.cfr;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Represents an unencoded, unarchived video (.mp4 file).
 *
 * @see Factory
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
final class UnencodedVideo {

  private final Path originalPath;
  private final Path encodedPath;
  private final Path tempEncodedPath;
  private final Path archivedPath;
  private final Path tempArchivedPath;

  private UnencodedVideo(
      Path originalPath,
      Path encodedPath,
      Path tempEncodedPath,
      Path archivedPath,
      Path tempArchivedPath) {
    this.originalPath = originalPath;
    this.encodedPath = encodedPath;
    this.tempEncodedPath = tempEncodedPath;
    this.archivedPath = archivedPath;
    this.tempArchivedPath = tempArchivedPath;
  }

  Path originalPath() {
    return originalPath;
  }

  Path encodedPath() {
    return encodedPath;
  }

  Path tempEncodedPath() {
    return tempEncodedPath;
  }

  Path archivedPath() {
    return archivedPath;
  }

  Path tempArchivedPath() {
    return tempArchivedPath;
  }

  @Override
  public String toString() {
    return originalPath.toString();
  }

  // TODO videos don't have to be .mp4
  private static final String MP4_SUFFIX = ".mp4";
  private static final String ENCODED_SUFFIX = ".cfr.mp4";
  private static final String TEMP_ENCODED_SUFFIX = ".cfr.mp4.part";
  private static final String TEMP_ARCHIVED_SUFFIX = ".mp4.part";

  static boolean isMp4(Path path) {
    return fileName(path).endsWith(MP4_SUFFIX);
  }

  static boolean isEncodedMp4(Path path) {
    return fileName(path).endsWith(ENCODED_SUFFIX);
  }

  static boolean isTempEncodedMp4(Path path) {
    return fileName(path).endsWith(TEMP_ENCODED_SUFFIX);
  }

  static boolean isTempArchivedMp4(Path path) {
    return fileName(path).endsWith(TEMP_ARCHIVED_SUFFIX);
  }

  private static String fileName(Path path) {
    return checkNotNull(path.getFileName()).toString();
  }

  /** Factory for constructing {@link UnencodedVideo}. */
  static class Factory {

    private final Path inputDirectory;
    private final Path outputDirectory;
    private final Path archiveDirectory;

    Factory(Path inputDirectory, Path outputDirectory, Path archiveDirectory) {
      checkArgument(
          Files.isDirectory(inputDirectory),
          "inputDirectory (%s) is not a directory",
          inputDirectory);
      checkArgument(
          Files.isDirectory(outputDirectory),
          "outputDirectory (%s) is not a directory",
          outputDirectory);
      checkArgument(
          Files.isDirectory(archiveDirectory),
          "archiveDirectory (%s) is not a directory",
          archiveDirectory);

      this.inputDirectory = inputDirectory;
      this.outputDirectory = outputDirectory;
      this.archiveDirectory = archiveDirectory;
    }

    UnencodedVideo newUnencodedVideo(Path videoPath) {
      checkArgument(
          !isTempEncodedMp4(videoPath),
          "videoPath (%s) represents an incomplete encoded .mp4 file",
          videoPath);

      checkArgument(
          !isTempArchivedMp4(videoPath),
          "videoPath (%s) represents an incomplete archived .mp4 file",
          videoPath);

      checkArgument(isMp4(videoPath), "videoPath (%s) does not represent an .mp4 file", videoPath);

      checkArgument(
          !isEncodedMp4(videoPath), "videoPath (%s) represents an encoded .mp4 file", videoPath);

      checkArgument(
          videoPath.startsWith(inputDirectory),
          "videoPath (%s) is not a child of inputDirectory (%s)",
          videoPath,
          inputDirectory);

      return new UnencodedVideo(
          videoPath,
          newDirectory(newSuffix(videoPath, ENCODED_SUFFIX), outputDirectory),
          newDirectory(newSuffix(videoPath, TEMP_ENCODED_SUFFIX), outputDirectory),
          newDirectory(videoPath, archiveDirectory),
          newDirectory(newSuffix(videoPath, TEMP_ARCHIVED_SUFFIX), archiveDirectory));
    }

    private Path newDirectory(Path videoPath, Path newDirectory) {
      return newDirectory
          .resolve(inputDirectory.relativize(videoPath))
          .resolveSibling(fileName(videoPath));
    }

    private Path newSuffix(Path videoPath, String newSuffix) {
      String newFileName = fileName(videoPath).replace(MP4_SUFFIX, newSuffix);
      return videoPath.resolveSibling(newFileName);
    }
  }
}
