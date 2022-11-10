package com.willmolloy.handbrake.cfr;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Stopwatch;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;
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
  public CompletableFuture<Boolean> archiveAsync(UnencodedVideo video) {
    return CompletableFuture.supplyAsync(
        () -> {
          Stopwatch stopwatch = Stopwatch.createStarted();
          try {
            log.debug("Archiving: {} -> {}", video.originalPath(), video.archivedPath());

            if (Files.exists(video.archivedPath())) {
              if (Files.mismatch(video.originalPath(), video.archivedPath()) == -1) {
                log.warn("Archive file ({}) already exists", video.archivedPath());
                if (!video.originalPath().equals(video.archivedPath())) {
                  log.info("Deleting: {}", video.originalPath());
                  Files.delete(video.originalPath());
                }
                return true;
              } else {
                log.error(
                    "Archive file ({}) already exists but contents differ. Aborting",
                    video.archivedPath());
                return false;
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
        });
  }
}
