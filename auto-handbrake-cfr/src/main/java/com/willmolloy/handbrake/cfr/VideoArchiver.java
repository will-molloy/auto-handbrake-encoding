package com.willmolloy.handbrake.cfr;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Stopwatch;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Responsible for archiving videos.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
class VideoArchiver {

  private static final Logger log = LogManager.getLogger();

  private final Executor executor =
      Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("video-archiver-", 1).factory());

  /**
   * Archives the given video.
   *
   * @param video video to archive
   * @return {@code true} if archiving was successful
   */
  public CompletableFuture<Boolean> archiveAsync(UnencodedVideo video) {
    // run archive async as it's IO bound and can be expensive (e.g. moving to another disk or NAS)
    // then while it's archiving it can start encoding the next video
    return CompletableFuture.supplyAsync(() -> doArchive(video), executor);
  }

  private boolean doArchive(UnencodedVideo video) {
    Stopwatch stopwatch = Stopwatch.createStarted();
    try {
      log.debug("Archiving: {} -> {}", video.originalPath(), video.archivedPath());

      if (video.originalPath().equals(video.archivedPath())) {
        log.info("Archived: {}", video.originalPath());
        return true;
      }

      if (Files.exists(video.archivedPath())) {
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
