package ru.yandex.qatools.allure.specs2

import ru.yandex.qatools.allure.Allure
import ru.yandex.qatools.allure.events._
import java.util.UUID
import scala.Some
import org.specs2.reporter.Notifier
import org.specs2.execute.Details

class AllureNotifier extends Notifier {
  
  private var lc = Allure.LIFECYCLE

  private val specLocationToUUIDMap = scala.collection.mutable.HashMap[String, String]()
  
  override def examplePending(name: String, message: String, duration: Long): Unit = {
    stepCanceled()
    stepFinished()
  }

  override def exampleSkipped(name: String, message: String, duration: Long): Unit = {
    stepCanceled()
    stepFinished()
  }

  override def exampleError(name: String, message: String, location: String, f: Throwable, duration: Long): Unit = {
    stepFailed(f)
    stepFinished()
  }

  override def exampleFailure(name: String, message: String, location: String, f: Throwable, details: Details, duration: Long): Unit = {
    //We could probably somehow use Details here in order to extract expected and actual values
    stepFailed(f)
    stepFinished()
  }

  override def exampleSuccess(name: String, duration: Long): Unit = stepFinished()

  override def exampleStarted(name: String, location: String): Unit = stepStarted(name)

  override def text(text: String, location: String): Unit = {}

  override def contextEnd(text: String, location: String): Unit = testCaseFinished()

  override def contextStart(text: String, location: String): Unit = testCaseStarted(getSpecUuid(location), text)

  override def specEnd(title: String, location: String): Unit = testSuiteFinished(getSpecUuid(location))

  override def specStart(title: String, location: String): Unit = testSuiteStarted(getSpecUuid(location), location)

  def getSpecUuid(specLocation: String): String = specLocationToUUIDMap.get(specLocation) match {
    case Some(uuid) => uuid
    case None =>
      val uuid = UUID.randomUUID().toString
      specLocationToUUIDMap += specLocation -> uuid
      uuid
  }

  def lifecycle = lc

  def setLifecycle(lifecycle: Allure) {
    lc = lifecycle
  }

  private def testSuiteStarted(uuid: String, suiteId: String) {
    lifecycle.fire(new TestSuiteStartedEvent(uuid, suiteId))
  }

  private def testSuiteFinished(uuid: String) {
    lifecycle.fire(new TestSuiteFinishedEvent(uuid))
  }

  private def testCaseStarted(suiteId: String, testName: String) {
    val uuid = getSpecUuid(suiteId)
    lifecycle.fire(new TestCaseStartedEvent(uuid, testName))
  }

  private def testCaseFinished() {
    lifecycle.fire(new TestCaseFinishedEvent())
  }

  private def stepCanceled() {
    lifecycle.fire(new StepCanceledEvent)
  }
  
  private def stepStarted(stepName: String) {
    lifecycle.fire(new StepStartedEvent(stepName))
  }
  
  private def stepFinished() {
    lifecycle.fire(new StepFinishedEvent)
  }
  
  private def stepFailed(throwable: Throwable) {
    lifecycle.fire(new StepFailureEvent().withThrowable(throwable))
  }
  
}