package com.willmolloy.handbrake.cfr;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Stopwatch;
import java.nio.file.Files;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Responsible for archiving videos.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
class VideoArchiver {

  private static final Logger log = LogManager.getLogger();

  /**
   * Archives the given video.
   *
   * @param video video to archive
   * @return {@code true} if archiving was successful
   */
  public boolean archive(UnencodedVideo video) {
    Stopwatch stopwatch = Stopwatch.createStarted();
    try {
      // simulate lag
      Thread.sleep(1_000);
      if (video.originalPath().equals(video.archivedPath())) {
        log.info("Archived: {}", video.originalPath());
        return true;
      }

      if (Files.exists(video.archivedPath())) {
        log.info("Verifying existing archived file contents");
        if (Files.mismatch(video.originalPath(), video.archivedPath()) != -1) {
          log.error(
              "Archive file ({}) already exists but contents differ. Skipping archive process",
              video.archivedPath());
          return false;
        } else {
          log.warn("Archive file ({}) already exists", video.archivedPath());
          log.info("Deleting: {}", video.originalPath());
          Files.delete(video.originalPath());
          return true;
        }
      }

      Files.createDirectories(checkNotNull(video.archivedPath().getParent()));

      log.info("Moving: {} -> {}", video.originalPath(), video.archivedPath());
      // archive to a temp file first in case something goes wrong
      // (e.g. app crash while it's uploading to NAS)
      Files.move(video.originalPath(), video.tempArchivedPath());
      Files.move(video.tempArchivedPath(), video.archivedPath());

      log.info("Archived: {}", video.archivedPath());
      return true;
    } catch (Exception e) {
      log.error("Error archiving: %s".formatted(video), e);
      return false;
    } finally {
      log.info("Elapsed: {}", stopwatch.elapsed());
    }
  }
}
