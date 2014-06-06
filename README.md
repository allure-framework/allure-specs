# Allure Specs2 Adapter
This adapter allows to retrieve test execution data from Specs2 framework and convert it to the form suitable for Allure report generation.

## Example project
Example project is located at: https://github.com/allure-framework/allure-specs2-example

## Usage
**In order to use this adapter you need to have JDK 1.7+ installed.** To enabled adapter simply add the following dependency to build.sbt:
```scala
libraryDependencies += "ru.yandex.qatools.allure" % "allure-specs2_2.10" % "1.4.0-SNAPSHOT"
```

Then attach **AllureReporter** in build.sbt:
```scala
testOptions in Test ++= Seq(
    Tests.Argument(TestFrameworks.Specs2, "-notifier", "ru.yandex.qatools.allure.specs2.AllureReporter")
)
```
