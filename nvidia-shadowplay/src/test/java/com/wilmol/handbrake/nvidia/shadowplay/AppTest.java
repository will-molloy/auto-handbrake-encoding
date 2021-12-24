package com.wilmol.handbrake.nvidia.shadowplay;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.io.Resources;
import com.wilmol.handbrake.core.Cli;
import com.wilmol.handbrake.core.HandBrake;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Component test.
 *
 * @author <a href=https://wilmol.com>Will Molloy</a>
 */
@SuppressFBWarnings("UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
@ExtendWith(MockitoExtension.class)
class AppTest {

  private Path testDirectory;
  private Path testVideo;

  private App app;

  @BeforeEach
  void setUp() throws Exception {
    testDirectory = Path.of("AppTest");
    testVideo = Path.of(Resources.getResource("test-video.mp4").toURI());

    Cli cli = new Cli(ProcessBuilder::new);
    HandBrake handBrake = new HandBrake(cli);
    app = new App(handBrake, cli);
  }

  @AfterEach
  void tearDown() throws IOException {
    FileUtils.deleteDirectory(testDirectory.toFile());
  }

  @Test
  void notDeletingOriginalVideos_encodesVideoFilesAndKeepsOriginals() throws Exception {
    // setup data
    Files.createDirectories(testDirectory.resolve(Path.of("KeepOriginals/NestedFolder")));
    Files.copy(testVideo, testDirectory.resolve(Path.of("KeepOriginals/video1.mp4")));
    Files.copy(testVideo, testDirectory.resolve(Path.of("KeepOriginals/video2.mp4")));
    Files.copy(testVideo, testDirectory.resolve(Path.of("KeepOriginals/NestedFolder/video3.mp4")));

    Path videosPath = Path.of("AppTest/KeepOriginals");
    app.run(videosPath, false, false);

    // originals still exist
    assertThat(Files.exists(Path.of("AppTest/KeepOriginals/video1.mp4"))).isTrue();
    assertThat(Files.exists(Path.of("AppTest/KeepOriginals/video2.mp4"))).isTrue();
    assertThat(Files.exists(Path.of("AppTest/KeepOriginals/NestedFolder/video3.mp4"))).isTrue();

    // encoded videos exist
    assertThat(Files.exists(Path.of("AppTest/KeepOriginals/video1 - CFR 60 FPS.mp4"))).isTrue();
    assertThat(Files.exists(Path.of("AppTest/KeepOriginals/video2 - CFR 60 FPS.mp4"))).isTrue();
    assertThat(Files.exists(Path.of("AppTest/KeepOriginals/NestedFolder/video3 - CFR 60 FPS.mp4")))
        .isTrue();
  }

  @Test
  void deletingOriginalVideos_encodesVideoFilesAndDeletesOriginals() throws Exception {
    // setup data
    Files.createDirectories(testDirectory.resolve(Path.of("DeleteOriginals/NestedFolder")));
    Files.copy(testVideo, testDirectory.resolve(Path.of("DeleteOriginals/video1.mp4")));
    Files.copy(testVideo, testDirectory.resolve(Path.of("DeleteOriginals/video2.mp4")));
    Files.copy(testVideo, testDirectory.resolve(Path.of("DeleteOriginals/NestedFolder/video3.mp4")));

    Path videosPath = Path.of("AppTest/DeleteOriginals");
    app.run(videosPath, true, false);

    // originals deleted
    assertThat(Files.exists(Path.of("AppTest/DeleteOriginals/video1.mp4"))).isFalse();
    assertThat(Files.exists(Path.of("AppTest/DeleteOriginals/video2.mp4"))).isFalse();
    assertThat(Files.exists(Path.of("AppTest/DeleteOriginals/NestedFolder/video3.mp4"))).isFalse();

    // encoded videos exist
    assertThat(Files.exists(Path.of("AppTest/DeleteOriginals/video1 - CFR 60 FPS.mp4"))).isTrue();
    assertThat(Files.exists(Path.of("AppTest/DeleteOriginals/video2 - CFR 60 FPS.mp4"))).isTrue();
    assertThat(Files.exists(Path.of("AppTest/DeleteOriginals/NestedFolder/video3 - CFR 60 FPS.mp4")))
            .isTrue();
  }
}
