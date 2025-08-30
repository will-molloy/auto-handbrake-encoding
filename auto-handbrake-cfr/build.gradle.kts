import org.ajoberstar.grgit.Grgit
import org.unbrokendome.gradle.plugins.testsets.dsl.testSets

plugins {
  alias(libs.plugins.testsets)
  alias(libs.plugins.jib)
  alias(libs.plugins.grgit)
}

testSets {
  create("integrationTest")
}

jib {
  from {
    image = "docker://handbrake-java-base"
  }
  to {
    image = "ghcr.io/will-molloy/auto-handbrake-cfr"
  }
  container {
    mainClass = "com.willmolloy.handbrake.cfr.Main"
    args = listOf("/input", "/output", "/archive")
    creationTime.set(gitCommitTime())
  }
}

fun gitCommitTime(): String {
  Grgit.open(mapOf("currentDir" to rootProject.rootDir)).use {
    return it.head().dateTime.toInstant().toString()
  }
}

dependencies {
  implementation(project(":auto-handbrake-core"))
}
