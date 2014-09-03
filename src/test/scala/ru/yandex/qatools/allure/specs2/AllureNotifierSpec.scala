package ru.yandex.qatools.allure.specs2

import ru.yandex.qatools.allure.Allure
import ru.yandex.qatools.allure.events._
import org.mockito.Matchers.{eq => eql}
import java.util.UUID
import org.specs2.mutable.Specification
import org.specs2.execute.FailureDetails
import org.specs2.mock.Mockito
import org.mockito.Mockito.doReturn
import org.specs2.specification.BeforeAfterExample

import scala.collection.mutable

object AllureNotifierSpec extends Specification with Mockito with BeforeAfterExample {
  
  var allure: Allure = _
  
  var notifier: AllureNotifier = _

  val specUuid = "some-uid"
  val specTitle = "spec-title"
  val specLocation = "spec-location"
  val contextText = "context-text"
  val exampleName = "example-name"
  val exampleMessage = "example-message"
  val exampleException = new Exception(exampleMessage)
  val exampleDetails = FailureDetails("0", "1")
  val exampleDuration = 0
  
  override def before = {
    allure = mock[Allure]
    notifier = spy(new AllureNotifier)
    notifier.setStack(new mutable.Stack[String])
    notifier.setLifecycle(allure)
    doReturn(specUuid).when(notifier).getSpecUuid(any[String])
  }

  override def after = {
    there were noMoreCallsTo(allure)
  }

  "AllureReporter" should {
    
    sequential

    "fire Allure TestSuiteStarted event on spec start" in {
      notifier.specStart(specTitle, specLocation)
      there was one(allure).fire(eql(new TestSuiteStartedEvent(specUuid, specTitle)))
    }

    "fire TestSuiteFinished event on spec end" in {
      notifier.stack.push(specTitle)
      notifier.specEnd(specTitle, specLocation)
      there was one(allure).fire(eql(new TestSuiteFinishedEvent(specUuid)))
    }

    "fire TestCaseStarted event on context start" in {
      notifier.stack.push(specTitle)
      notifier.contextStart(contextText, specLocation)
      there was one(allure).fire(eql(new TestCaseStartedEvent(specUuid, contextText)))
    }
    
    "fire TestCaseFinished event on context end" in {
      notifier.contextEnd(contextText, specLocation)
      there was one(allure).fire(eql(new TestCaseFinishedEvent))
    }
    
    "fire StepStarted event on example start" in {
      notifier.exampleStarted(exampleName, specLocation)
      there was one(allure).fire(eql(new StepStartedEvent(exampleName)))
    }
    
    "fire StepFinished event on example success" in {
      notifier.exampleSuccess(exampleName, exampleDuration)
      there was one(allure).fire(eql(new StepFinishedEvent))
    }
    
    "fire StepFailed and StepFinished events on example failure" in {
      notifier.exampleFailure(exampleName, exampleMessage, specLocation, exampleException, exampleDetails, exampleDuration)
      there was one(allure).fire(eql(new StepFailureEvent().withThrowable(exampleException)))
      there was one(allure).fire(eql(new StepFinishedEvent))
    }
    
    "fire StepFailed and StepFinished events on example error" in {
      notifier.exampleError(exampleName, exampleMessage, specLocation, exampleException, exampleDuration)
      there was one(allure).fire(eql(new StepFailureEvent().withThrowable(exampleException)))
      there was one(allure).fire(eql(new StepFinishedEvent))
    }
    
    "fire StepCanceled and StepFinished events on example skipped" in {
      notifier.exampleSkipped(exampleName, exampleMessage, exampleDuration)
      there was one(allure).fire(eql(new StepCanceledEvent))
      there was one(allure).fire(eql(new StepFinishedEvent))
    }
    
    "fire StepCanceled and StepFinished events on example pending" in {
      notifier.examplePending(exampleName, exampleMessage, exampleDuration)
      there was one(allure).fire(eql(new StepCanceledEvent))
      there was one(allure).fire(eql(new StepFinishedEvent))
    }

    "return uuid on first and subsequent getSuiteUuid calls" in {
      val reporter = new AllureNotifier
      val firstUUID = reporter.getSpecUuid(specTitle)
      firstUUID.length must beGreaterThan(0)
      val secondUUID = reporter.getSpecUuid(specTitle)
      firstUUID mustEqual secondUUID
      UUID.fromString(firstUUID) mustEqual UUID.fromString(secondUUID)
    }

  }
    
}
