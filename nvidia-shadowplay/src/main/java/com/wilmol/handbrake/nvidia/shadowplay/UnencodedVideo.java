package com.wilmol.handbrake.nvidia.shadowplay;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Represents an unencoded video (.mp4 file).
 *
 * @author <a href=https://wilmol.com>Will Molloy</a>
 */
class UnencodedVideo {

  private static final String MP4_SUFFIX = ".mp4";
  private static final String ENCODED_MP4_SUFFIX = " - CFR 60 FPS.mp4";

  private final Path originalPath;

  private final Path encodedPath;

  UnencodedVideo(Path path) {
    checkArgument(isMp4(path), "Path %s does not represent an .mp4 file", path);
    checkArgument(!isEncodedMp4(path), "Path %s already represents an encoded .mp4 file", path);

    originalPath = path;

    String encodedFileName =
        checkNotNull(path.getFileName()).toString().replace(MP4_SUFFIX, ENCODED_MP4_SUFFIX);
    encodedPath = path.resolveSibling(encodedFileName);
  }

  public static boolean isMp4(Path path) {
    return path.toString().endsWith(MP4_SUFFIX);
  }

  public static boolean isEncodedMp4(Path path) {
    return path.toString().endsWith(ENCODED_MP4_SUFFIX);
  }

  public boolean hasBeenEncoded() {
    return Files.exists(encodedPath);
  }

  public Path originalPath() {
    return originalPath;
  }

  public Path encodedPath() {
    return encodedPath;
  }
}
