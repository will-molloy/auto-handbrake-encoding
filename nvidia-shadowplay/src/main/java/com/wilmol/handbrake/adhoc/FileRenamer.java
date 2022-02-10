package com.wilmol.handbrake.adhoc;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Useful for renaming files, e.g. removing the suffix to quickly unarchive files (or if the
 * suffixes in {@link com.wilmol.handbrake.nvidia.shadowplay.UnencodedVideo} change).
 *
 * @author <a href=https://wilmol.com>Will Molloy</a>
 */
public class FileRenamer {

  private static final Logger log = LogManager.getLogger();

  /**
   * Changes file suffixes.
   *
   * @param directoryPath directory to walk
   * @param oldSuffix old suffix
   * @param newSuffix new suffix
   */
  public void changeSuffixes(Path directoryPath, String oldSuffix, String newSuffix)
      throws IOException {
    log.info(
        "Running(directoryPath={}, oldSuffix={}, newSuffix={})",
        directoryPath,
        oldSuffix,
        newSuffix);

    List<Path> paths =
        Files.walk(directoryPath)
            .filter(Files::isRegularFile)
            .filter(path -> path.toString().endsWith(oldSuffix))
            .toList();

    log.info("Detected {} files to rename", paths.size());

    for (int i = 0; i < paths.size(); i++) {
      Path path = paths.get(i);
      String fileName = checkNotNull(path.getFileName()).toString();
      String newFileName = fileName.replace(oldSuffix, newSuffix);
      Path newPath = path.resolveSibling(newFileName);

      log.info("({}/{}) Renaming {} -> {}", i + 1, paths.size(), path, newPath);
      Files.move(path, newPath);
    }
  }

  //  public static void main(String[] args) {
  //    try {
  //      Path directory = Path.of("D:\\Videos\\Gameplay");
  //      FileRenamer fileRenamer = new FileRenamer();
  //
  //      // unarchive
  //      //      fileRenamer.changeSuffixes(directory, " - Archived.mp4", ".mp4");
  //
  //      // other
  //      //      fileRenamer.changeSuffixes(directory, " - CFR 60 FPS.mp4", " - CFR.mp4");
  //    } catch (Exception e) {
  //      log.fatal("Fatal error", e);
  //    }
  //  }
}
