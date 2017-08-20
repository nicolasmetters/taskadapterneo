package com.taskadapter.connector.redmine

import java.util.{Calendar, Collections}

import com.taskadapter.connector.Priorities
import com.taskadapter.connector.definition.TaskId
import com.taskadapter.model.{GUser, Precedes}
import com.taskadapter.redmineapi.bean._
import org.junit.Assert.{assertEquals, assertNull}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FunSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class RedmineToGTaskTest extends FunSpec with Matchers with BeforeAndAfter with BeforeAndAfterAll {

  val userCache = new RedmineUserCache(Seq())

  private def get(): RedmineToGTask = {
    val config = new RedmineConfig
    new RedmineToGTask(config, userCache)
  }

  it("summaryIsConverted") {
    val redmineIssue = new Issue
    redmineIssue.setSubject("text 1")
    val task = get().convertToGenericTask(redmineIssue)
    assertEquals("text 1", task.getValue(RedmineField.summary.name))
  }

  it("descriptionIsConverted") {
    val redmineIssue = new Issue
    redmineIssue.setDescription("description 1")
    val task = get().convertToGenericTask(redmineIssue)
    assertEquals("description 1", task.getValue(RedmineField.description.name))
  }

  it("idIsConvertedIfSet") {
    val redmineIssue = IssueFactory.create(123)
    val task = get().convertToGenericTask(redmineIssue)
    task.getId shouldBe 123
  }

  it("idIsIgnoredIfNull") {
    val redmineIssue = new Issue
    val task = get().convertToGenericTask(redmineIssue)
    assertNull(task.getId)
  }

  it("idIsSetToKey") {
    val redmineIssue = IssueFactory.create(123)
    val task = get().convertToGenericTask(redmineIssue)
    assertEquals("123", task.getKey)
  }

  it("parentIdIsConvertedIfSet") {
    val redmineIssue = new Issue
    redmineIssue.setParentId(123)
    val task = get().convertToGenericTask(redmineIssue)
    task.getParentIdentity shouldBe TaskId(123, "123")
  }

  it("parentIdIsIgnoredIfNotSet") {
    val redmineIssue = new Issue
    val task = get().convertToGenericTask(redmineIssue)
    assertNull(task.getParentIdentity)
  }

  it("assigneeIsIgnoredIfNotSet") {
    val redmineIssue = new Issue
    val task = get().convertToGenericTask(redmineIssue)
    assertNull(task.getValue(RedmineField.assignee.name))
  }

  it("assignee id is converted") {
    val redmineIssue = new Issue
    redmineIssue.setAssigneeId(123)
    val task = get().convertToGenericTask(redmineIssue)
    task.getValue(RedmineField.assignee).asInstanceOf[GUser].getId shouldBe 123
  }

  it("trackerTypeIsConvertedIfSet") {
    val redmineIssue = new Issue
    val tracker = TrackerFactory.create(123, "something")
    redmineIssue.setTracker(tracker)
    val task = get().convertToGenericTask(redmineIssue)
    assertEquals("something", task.getValue(RedmineField.taskType.name))
  }

  it("trackerTypeIsIgnoredIfNotSet") {
    val redmineIssue = new Issue
    val task = get().convertToGenericTask(redmineIssue)
    assertNull(task.getValue(RedmineField.taskType.name))
  }

  it("statusIsConverted") {
    val redmineIssue = new Issue
    redmineIssue.setStatusName("some status")
    val task = get().convertToGenericTask(redmineIssue)
    assertEquals("some status", task.getValue(RedmineField.taskStatus.name))
  }

  it("estimatedHoursAreConverted") {
    val redmineIssue = new Issue
    redmineIssue.setEstimatedHours(55f)
    val task = get().convertToGenericTask(redmineIssue)
    assertEquals(55f.asInstanceOf[Float], task.getValue(RedmineField.estimatedTime.name))
  }

  it("doneRatioIsConverted") {
    val redmineIssue = new Issue
    redmineIssue.setDoneRatio(75)
    val task = get().convertToGenericTask(redmineIssue)
    assertEquals(75, task.getValue(RedmineField.doneRatio.name))
  }

  it("startDateIsConverted") {
    val redmineIssue = new Issue
    val time = getTime
    redmineIssue.setStartDate(time)
    val task = get().convertToGenericTask(redmineIssue)
    assertEquals(time, task.getValue(RedmineField.startDate.name))
  }

  private def getTime = {
    val calendar = Calendar.getInstance
    calendar.set(2014, Calendar.APRIL, 23, 0, 0, 0)
    calendar.getTime
  }

  it("dueDateIsConverted") {
    val redmineIssue = new Issue
    val time = getTime
    redmineIssue.setDueDate(time)
    val task = get().convertToGenericTask(redmineIssue)
    assertEquals(time, task.getValue(RedmineField.dueDate.name))
  }

  it("createdOnIsConverted") {
    val redmineIssue = new Issue
    val time = getTime
    redmineIssue.setCreatedOn(time)
    val task = get().convertToGenericTask(redmineIssue)
    assertEquals(time, task.getValue(RedmineField.createdOn.name))
  }

  it("updatedOnIsConverted") {
    val redmineIssue = new Issue
    val time = getTime
    redmineIssue.setUpdatedOn(time)
    val task = get().convertToGenericTask(redmineIssue)
    assertEquals(time, task.getValue(RedmineField.updatedOn.name))
  }

  it("priorityIsAssignedDefaultValueIfNotSet") {
    val redmineIssue = new Issue
    val task = get().convertToGenericTask(redmineIssue)
    assertEquals(Priorities.DEFAULT_PRIORITY_VALUE, task.getValue(RedmineField.priority.name))
  }

  it("priorityIsConvertedIfSet") {
    val redmineIssue = new Issue
    redmineIssue.setPriorityText("High")
    val task = get().convertToGenericTask(redmineIssue)
    assertEquals(700, task.getValue(RedmineField.priority.name))
  }

  it("priorityIsAssignedDefaultValueIfUnknownValueSet") {
    val redmineIssue = new Issue
    redmineIssue.setPriorityText("some unknown text")
    val task = get().convertToGenericTask(redmineIssue)
    assertEquals(Priorities.DEFAULT_PRIORITY_VALUE, task.getValue(RedmineField.priority.name))
  }

  it("relations are converted") {
    val redmineIssue = IssueFactory.create(10)
    val relation = IssueRelationFactory.create
    relation.setType(IssueRelation.TYPE.precedes.toString)
    relation.setIssueId(10)
    relation.setIssueToId(20)
    redmineIssue.addRelations(Collections.singletonList(relation))
    val task = get().convertToGenericTask(redmineIssue)
    assertEquals(1, task.getRelations.size)
    val gRelation = task.getRelations.get(0)

    gRelation.taskId.id shouldBe 10
    gRelation.relatedTaskId.id shouldBe 20
    gRelation.`type` shouldBe Precedes
  }

}
