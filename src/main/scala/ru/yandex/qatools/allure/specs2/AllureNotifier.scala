package ru.yandex.qatools.allure.specs2

import java.util.UUID

import org.specs2.execute.Details
import org.specs2.reporter.Notifier
import ru.yandex.qatools.allure.Allure
import ru.yandex.qatools.allure.events._
import scala.collection.mutable

class AllureNotifier extends Notifier {

  private var st = new mutable.Stack[String]

  private var lc = Allure.LIFECYCLE

  private val specTitleToUUIDMap = scala.collection.mutable.HashMap[String, String]()

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

  override def contextStart(text: String, location: String): Unit = {
    val specTitle = stack.head
    testCaseStarted(specTitle, text)
  }

  override def specEnd(title: String, location: String): Unit = {
    val specTitle = stack.pop()
    testSuiteFinished(getSpecUuid(specTitle))
  }

  override def specStart(title: String, location: String): Unit = {
    stack.push(title)
    testSuiteStarted(getSpecUuid(title), title)
  }

  def getSpecUuid(specTitle: String): String = specTitleToUUIDMap.get(specTitle) match {
    case Some(uuid) => uuid
    case None =>
      val uuid = UUID.randomUUID().toString
      specTitleToUUIDMap += specTitle -> uuid
      uuid
  }

  def lifecycle = lc
  
  def setLifecycle(lifecycle: Allure) {
    lc = lifecycle
  }

  def stack = st

  def setStack(stack: mutable.Stack[String]) {
    st = stack
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