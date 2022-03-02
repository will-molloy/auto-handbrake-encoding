package com.wilmol.handbrake.nvidia.shadowplay;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Represents an unencoded, unarchived video (.mp4 file).
 *
 * @see Factory
 * @author <a href=https://wilmol.com>Will Molloy</a>
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

  public boolean hasBeenEncoded() {
    return Files.exists(encodedPath);
  }

  @Override
  public String toString() {
    return originalPath.toString();
  }

  public Path originalPath() {
    return originalPath;
  }

  public Path encodedPath() {
    return encodedPath;
  }

  public Path tempEncodedPath() {
    return tempEncodedPath;
  }

  public Path archivedPath() {
    return archivedPath;
  }

  public Path tempArchivedPath() {
    return tempArchivedPath;
  }

  /** Factory for constructing {@link UnencodedVideo}. */
  static class Factory {

    private static final String MP4_SUFFIX = ".mp4";
    private static final String ENCODED_SUFFIX = " - CFR.mp4";
    private static final String TEMP_ENCODED_SUFFIX = " - CFR (incomplete).mp4";
    private static final String ARCHIVED_SUFFIX = " - Archived.mp4";
    private static final String TEMP_ARCHIVED_SUFFIX = " - Archived (incomplete).mp4";

    public static boolean isMp4(Path path) {
      return fileName(path).endsWith(MP4_SUFFIX);
    }

    public static boolean isEncodedMp4(Path path) {
      return fileName(path).endsWith(ENCODED_SUFFIX);
    }

    public static boolean isTempEncodedMp4(Path path) {
      return fileName(path).endsWith(TEMP_ENCODED_SUFFIX);
    }

    public static boolean isArchivedMp4(Path path) {
      return fileName(path).endsWith(ARCHIVED_SUFFIX);
    }

    public static boolean isTempArchivedMp4(Path path) {
      return fileName(path).endsWith(TEMP_ARCHIVED_SUFFIX);
    }

    private static String fileName(Path path) {
      return checkNotNull(path.getFileName()).toString();
    }

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

    public UnencodedVideo newUnencodedVideo(Path videoPath) {
      checkArgument(isMp4(videoPath), "videoPath (%s) does not represent an .mp4 file", videoPath);

      checkArgument(
          !isEncodedMp4(videoPath), "videoPath (%s) represents an encoded .mp4 file", videoPath);
      checkArgument(
          !isTempEncodedMp4(videoPath),
          "videoPath (%s) represents an incomplete encoded .mp4 file",
          videoPath);

      checkArgument(
          !isArchivedMp4(videoPath), "videoPath (%s) represents an archived .mp4 file", videoPath);
      checkArgument(
          !isTempArchivedMp4(videoPath),
          "videoPath (%s) represents an incomplete archived .mp4 file",
          videoPath);

      checkArgument(
          videoPath.startsWith(inputDirectory),
          "videoPath (%s) is not a child of inputDirectory (%s)",
          videoPath,
          inputDirectory);

      return new UnencodedVideo(
          videoPath,
          encodedPath(videoPath),
          tempEncodedPath(videoPath),
          archivedPath(videoPath),
          tempArchivedPath(videoPath));
    }

    private Path encodedPath(Path videoPath) {
      return newPath(videoPath, ENCODED_SUFFIX, outputDirectory);
    }

    private Path tempEncodedPath(Path videoPath) {
      return newPath(videoPath, TEMP_ENCODED_SUFFIX, outputDirectory);
    }

    private Path archivedPath(Path videoPath) {
      return newPath(videoPath, ARCHIVED_SUFFIX, archiveDirectory);
    }

    private Path tempArchivedPath(Path videoPath) {
      return newPath(videoPath, TEMP_ARCHIVED_SUFFIX, archiveDirectory);
    }

    private Path newPath(Path videoPath, String newSuffix, Path newDirectory) {
      String newFileName = fileName(videoPath).replace(MP4_SUFFIX, newSuffix);
      return newDirectory.resolve(inputDirectory.relativize(videoPath)).resolveSibling(newFileName);
    }
  }
}
