package com.willmolloy.handbrake.cfr.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * File utility methods. (Extension to {@link Files}.)
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
public final class Files2 {

  /**
   * Tests if two files have similar contents.
   *
   * <p>Specifically if <1% of bytes mismatch.
   *
   * @param path1 first file
   * @param path2 second file
   * @return {@code true} if the files contents are similar
   * @see Files#mismatch
   */
  // HandBrake is not deterministic (encoding doesn't always produce the exact same output) so need
  // a method to test file contents are similar when comparing encoded files.
  public static boolean contentsSimilar(Path path1, Path path2) {
    try {
      if (Files.isSameFile(path1, path2)) {
        return true;
      }

      double tolerance = 0.01;
      // take size of the largest file, effectively pads the smaller file with 0s
      long size = Math.max(Files.size(path1), Files.size(path2));
      long allowedMismatchBytes = (long) (tolerance * size);
      long mismatchCount = 0;

      // adapted from Files.mismatch
      int bufferSize = 8192;
      byte[] buffer1 = new byte[bufferSize];
      byte[] buffer2 = new byte[bufferSize];

      try (InputStream in1 = Files.newInputStream(path1);
          InputStream in2 = Files.newInputStream(path2)) {

        for (long totalRead = 0; totalRead < size; totalRead += bufferSize) {
          in1.readNBytes(buffer1, 0, bufferSize);
          in2.readNBytes(buffer2, 0, bufferSize);

          for (int i = 0; i < bufferSize; i++) {
            if (buffer1[i] != buffer2[i]) {
              mismatchCount++;
              if (mismatchCount > allowedMismatchBytes) {
                return false;
              }
            }
          }
        }

        return true;
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private Files2() {}
}
