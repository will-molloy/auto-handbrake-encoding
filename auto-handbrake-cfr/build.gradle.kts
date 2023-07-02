import org.ajoberstar.grgit.Grgit
import org.unbrokendome.gradle.plugins.testsets.dsl.testSets

plugins {
  id("org.unbroken-dome.test-sets") version "4.0.0"
  id("com.google.cloud.tools.jib") version "3.3.2"
  id("org.ajoberstar.grgit") version "5.2.0"
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
    jvmFlags = listOf("--enable-preview")
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