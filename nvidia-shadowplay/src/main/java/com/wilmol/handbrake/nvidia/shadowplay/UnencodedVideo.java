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

  private UnencodedVideo(
      Path originalPath, Path encodedPath, Path tempEncodedPath, Path archivedPath) {
    this.originalPath = originalPath;
    this.encodedPath = encodedPath;
    this.tempEncodedPath = tempEncodedPath;
    this.archivedPath = archivedPath;
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

  /**
   * Factory for constructing {@link UnencodedVideo}.
   *
   * @author <a href=https://wilmol.com>Will Molloy</a>
   */
  static class Factory {

    private static final String MP4_SUFFIX = ".mp4";
    private static final String ENCODED_MP4_SUFFIX = " - CFR.mp4";
    private static final String TEMP_ENCODED_MP4_SUFFIX = " - CFR (incomplete).mp4";
    private static final String ARCHIVED_SUFFIX = " - Archived.mp4";

    public static boolean isMp4(Path path) {
      return fileName(path).endsWith(MP4_SUFFIX);
    }

    public static boolean isEncodedMp4(Path path) {
      return fileName(path).endsWith(ENCODED_MP4_SUFFIX);
    }

    public static boolean isTempEncodedMp4(Path path) {
      return fileName(path).endsWith(TEMP_ENCODED_MP4_SUFFIX);
    }

    public static boolean isArchivedMp4(Path path) {
      return fileName(path).endsWith(ARCHIVED_SUFFIX);
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
          videoPath.startsWith(inputDirectory),
          "videoPath (%s) is not a child of inputDirectory (%s)",
          videoPath,
          inputDirectory);

      return new UnencodedVideo(
          videoPath, encodedPath(videoPath), tempEncodedPath(videoPath), archivedPath(videoPath));
    }

    private Path encodedPath(Path videoPath) {
      String encodedFileName = fileName(videoPath).replace(MP4_SUFFIX, ENCODED_MP4_SUFFIX);
      return outputDirectory
          .resolve(inputDirectory.relativize(videoPath))
          .resolveSibling(encodedFileName);
    }

    private Path tempEncodedPath(Path videoPath) {
      String tempEncodedFileName = fileName(videoPath).replace(MP4_SUFFIX, TEMP_ENCODED_MP4_SUFFIX);
      return outputDirectory
          .resolve(inputDirectory.relativize(videoPath))
          .resolveSibling(tempEncodedFileName);
    }

    private Path archivedPath(Path videoPath) {
      String archivedFileName = fileName(videoPath).replace(MP4_SUFFIX, ARCHIVED_SUFFIX);
      return archiveDirectory
          .resolve(inputDirectory.relativize(videoPath))
          .resolveSibling(archivedFileName);
    }
  }
}
