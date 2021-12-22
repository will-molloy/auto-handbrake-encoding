# java-gradle-template

[![build](https://github.com/wilmol/java-gradle-template/workflows/build/badge.svg?event=push)](https://github.com/wilmol/java-gradle-template/actions?query=workflow%3Abuild)
[![codecov](https://codecov.io/gh/wilmol/java-gradle-template/branch/main/graph/badge.svg)](https://codecov.io/gh/wilmol/java-gradle-template)

template repository for Java projects using Gradle

## Usage
* Just go to: https://github.com/wilmol/java-gradle-template/generate
  * This will prompt you to create a new repository with all the files setup
* Rename the root project (currently `java-gradle-template`) and group (currently `com.wilmol`) to your liking 
* Create your README
* Delete anything you won't use (sub projects, dependencies etc.)

### Build
```
./gradlew spotlessApply build
```

## Features
* Java 17 LTS
* Gradle 7
  * Multi-project builds
* [GitHub Actions CI](https://help.github.com/actions/language-and-framework-guides/building-and-testing-java-with-gradle) integration
* [Codecov](https://codecov.io/) integration
* [Spotless](https://github.com/diffplug/spotless) integration 
  * With [google-java-format](https://github.com/google/google-java-format)
* [Checkstyle](https://github.com/checkstyle/checkstyle) integration 
  * With [google_checks](https://github.com/checkstyle/checkstyle/blob/master/src/main/resources/google_checks.xml)
* [SpotBugs](https://spotbugs.github.io/) integration

## Promise
* Keep up to date with:
  * Future Java versions
  * Future Gradle versions
  * Other dependency upgrades
