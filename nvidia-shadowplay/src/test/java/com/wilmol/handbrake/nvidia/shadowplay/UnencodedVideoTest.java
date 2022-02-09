package com.wilmol.handbrake.nvidia.shadowplay;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.truth.StringSubject;
import java.nio.file.Path;
import java.util.Locale;
import org.junit.jupiter.api.Test;

/**
 * UnencodedVideoTest.
 *
 * @author <a href=https://wilmol.com>Will Molloy</a>
 */
class UnencodedVideoTest {

  @Test
  void acceptsUnencodedMp4FileAndGeneratesOtherPaths() {
    Path path = Path.of("files/file.mp4");

    UnencodedVideo unencodedVideo = new UnencodedVideo(path);

    assertThat(unencodedVideo.originalPath()).isSameInstanceAs(path);
    assertThat(unencodedVideo.encodedPath()).isEqualTo(Path.of("files/file - CFR 60 FPS.mp4"));
    assertThat(unencodedVideo.tempEncodedPath())
        .isEqualTo(Path.of("files/file - CFR 60 FPS (incomplete).mp4"));
    assertThat(unencodedVideo.archivedPath()).isEqualTo(Path.of("files/file - Archived.mp4"));
  }

  @Test
  void rejectsNonMp4File() {
    Path path = Path.of("files/file.mp3");

    IllegalArgumentException thrown =
        assertThrows(IllegalArgumentException.class, () -> new UnencodedVideo(path));
    StringSubject messageThat = assertThat(thrown).hasMessageThat();
    if (isWindows()) {
      messageThat.isEqualTo("Path does not represent an .mp4 file: files\\file.mp3");
    } else {
      messageThat.isEqualTo("Path does not represent an .mp4 file: files/file.mp3");
    }
  }

  @Test
  void rejectsEncodedMp4File() {
    Path path = Path.of("files/file - CFR 60 FPS.mp4");

    IllegalArgumentException thrown =
        assertThrows(IllegalArgumentException.class, () -> new UnencodedVideo(path));
    StringSubject messageThat = assertThat(thrown).hasMessageThat();
    if (isWindows()) {
      messageThat.isEqualTo("Path represents an encoded .mp4 file: files\\file - CFR 60 FPS.mp4");
    } else {
      messageThat.isEqualTo("Path represents an encoded .mp4 file: files/file - CFR 60 FPS.mp4");
    }
  }

  @Test
  void rejectsTempEncodedMp4File() {
    Path path = Path.of("files/file - CFR 60 FPS (incomplete).mp4");

    IllegalArgumentException thrown =
        assertThrows(IllegalArgumentException.class, () -> new UnencodedVideo(path));
    StringSubject messageThat = assertThat(thrown).hasMessageThat();
    if (isWindows()) {
      messageThat.isEqualTo(
          "Path represents an incomplete encoded .mp4 file: files\\file - CFR 60 FPS (incomplete).mp4");
    } else {
      messageThat.isEqualTo(
          "Path represents an incomplete encoded .mp4 file: files/file - CFR 60 FPS (incomplete).mp4");
    }
  }

  @Test
  void rejectsArchivedMp4File() {
    Path path = Path.of("files/file - Archived.mp4");

    IllegalArgumentException thrown =
        assertThrows(IllegalArgumentException.class, () -> new UnencodedVideo(path));
    StringSubject messageThat = assertThat(thrown).hasMessageThat();
    if (isWindows()) {
      messageThat.isEqualTo("Path represents an archived .mp4 file: files\\file - Archived.mp4");
    } else {
      messageThat.isEqualTo("Path represents an archived .mp4 file: files/file - Archived.mp4");
    }
  }

  private boolean isWindows() {
    return System.getProperty("os.name").toLowerCase(Locale.getDefault()).contains("win");
  }
}
