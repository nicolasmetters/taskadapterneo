package com.taskadapter.connector.msp;

import com.taskadapter.connector.common.CommonTests;
import com.taskadapter.connector.common.TestSaver;
import com.taskadapter.connector.common.TestUtils;
import com.taskadapter.connector.definition.Mappings;
import com.taskadapter.connector.definition.exceptions.ConnectorException;
import com.taskadapter.model.GTask;
import com.taskadapter.model.GTaskDescriptor.FIELD;
import com.taskadapter.model.GUser;
import net.sf.mpxj.*;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.taskadapter.connector.msp.MSPTestUtils.deleteFile;
import static com.taskadapter.connector.msp.MSPTestUtils.findMSPTaskBySummary;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class FieldMappingTest {
    private MSPConfig config;
    private MSPConnector connector;

    @Before
    public void beforeEachTest() {
        config = new MSPConfig();
        // TODO need to generate a random file here to avoid possible collisions
        // when this test class runs in multiple threads
        config.setInputAbsoluteFilePath("msp_test_data.tmp");
        config.setOutputAbsoluteFilePath("msp_test_data.tmp");

        connector = new MSPConnector(config);
    }

    @Test
    public void testEstimatedTimeNotSaved() throws Exception {
        GTask task = TestUtils.generateTask();
        task.setEstimatedHours((float) 25);
        List<Task> loadedTasks = saveAndLoad(config, FIELD.ESTIMATED_TIME, false, TaskField.DURATION.toString(), task);
        Task loadedTask = findMSPTaskBySummary(loadedTasks, task.getSummary());

        assertNull(loadedTask.getWork());
        assertNull(loadedTask.getDuration());
    }

    @Test
    public void testEstimatedTimeSaved() throws Exception {
        GTask task = TestUtils.generateTask();
        Float hours = 25f;
        task.setEstimatedHours(hours);
        List<Task> loadedTasks = saveAndLoad(config, FIELD.ESTIMATED_TIME, true, TaskField.DURATION.toString(),
                task);
        Task loadedTask = findMSPTaskBySummary(loadedTasks, task.getSummary());
        assertEquals(hours, loadedTask.getDuration().getDuration(), 0);
    }

    @Test
    public void testEstimatedTimeSavedToWork() throws Exception {
        GTask task = TestUtils.generateTask();
        Float hours = 25f;
        task.setEstimatedHours(hours);
        List<Task> loadedTasks = saveAndLoad(config, FIELD.ESTIMATED_TIME, true, TaskField.WORK.toString(), task);
        Task loadedTask = findMSPTaskBySummary(loadedTasks, task.getSummary());

        assertEquals(hours, loadedTask.getWork().getDuration(), 0);
        assertNull(loadedTask.getDuration());
    }

    @Test
    public void testEstimatedTimeSavedToDuration() throws Exception {
        GTask task = TestUtils.generateTask();
        Float hours = 25f;
        task.setEstimatedHours(hours);
        List<Task> loadedTasks = saveAndLoad(config, FIELD.ESTIMATED_TIME, true, TaskField.DURATION.toString(), task);
        Task loadedTask = findMSPTaskBySummary(loadedTasks, task.getSummary());

        assertEquals(hours, loadedTask.getDuration().getDuration(), 0);
        assertNull(loadedTask.getWork());
    }

    @Test
    public void testDueDateMappedToFinish() throws Exception {
        GTask task = TestUtils.generateTask();
        Date dueDate = getDateRoundedToMinutes();
        task.setDueDate(dueDate);

        List<Task> loadedTasks = saveAndLoad(config, FIELD.DUE_DATE, true, TaskField.FINISH.toString(), task);
        Task loadedTask = findMSPTaskBySummary(loadedTasks, task.getSummary());
        assertEquals(dueDate, loadedTask.getFinish());
        assertEquals(null, loadedTask.getDeadline());
    }

    @Test
    public void testDueDateMappedToDeadline() throws Exception {
        GTask task = TestUtils.generateTask();
        Date dueDate = getDateRoundedToMinutes();
        task.setDueDate(dueDate);

        List<Task> loadedTasks = saveAndLoad(config, FIELD.DUE_DATE, true, TaskField.DEADLINE.toString(), task);
        Task loadedTask = findMSPTaskBySummary(loadedTasks, task.getSummary());
        assertEquals(dueDate, loadedTask.getDeadline());
        assertEquals(null, loadedTask.getFinish());
    }

    @Test
    public void testDueDateNotExported() throws Exception {
        GTask task = TestUtils.generateTask();
        Date dueDate = getDateRoundedToMinutes();
        task.setDueDate(dueDate);

        List<Task> loadedTasks = saveAndLoad(config, FIELD.DUE_DATE, false, "some value", task);
        Task loadedTask = findMSPTaskBySummary(loadedTasks, task.getSummary());
        assertEquals(null, loadedTask.getDeadline());
        assertEquals(null, loadedTask.getFinish());
    }

    @Test
    public void testDescriptionNotExported() throws Exception {
        GTask task = TestUtils.generateTask();
        GTask loadedTask = getTestSaver().unselectField(FIELD.DESCRIPTION).saveAndLoad(task);
        assertEquals("", loadedTask.getDescription());
    }

    @Test
    public void testDescriptionExported() throws Exception {
        GTask task = TestUtils.generateTask();
        GTask loadedTask = getTestSaver().selectField(FIELD.DESCRIPTION).saveAndLoad(task);
        assertEquals(task.getDescription(), loadedTask.getDescription());
    }

    @Test
    public void descriptionExportedByDefault() throws Exception {
        GTask task = TestUtils.generateTask();
        GTask loadedTask = TestUtils.saveAndLoadViaSummary(connector, task, DefaultMSPMappings.generate());
        assertEquals(task.getDescription(), loadedTask.getDescription());
    }

    @Test
    public void startDateNotMapped() throws Exception {
        GTask task = TestUtils.generateTask();
        TestUtils.setTaskStartYearAgo(task);
        List<Task> loadedTasks = saveAndLoad(config, FIELD.START_DATE, false, TaskField.DURATION.toString(), task);
        assertNull(loadedTasks.get(0).getStart());
    }

    @Test
    public void startDateExportedNoConstraint() throws Exception {
        GTask task = TestUtils.generateTask();
        Calendar yearAgo = TestUtils.getDateRoundedToDay();
        yearAgo.add(Calendar.YEAR, -1);
        task.setStartDate(yearAgo.getTime());
        Mappings mappings = DefaultMSPMappings.generate();
        mappings.setMapping(FIELD.START_DATE, true, MSPUtils.NO_CONSTRAINT);
        GTask loadedTask = TestUtils.saveAndLoadViaSummary(connector, task, mappings);
        assertEquals(yearAgo.getTime(), loadedTask.getStartDate());
    }

    @Test
    public void startDateMustStartOn() throws Exception {
        GTask task = TestUtils.generateTask();
        Calendar cal = TestUtils.setTaskStartYearAgo(task);
        Mappings mappings = DefaultMSPMappings.generate();
        mappings.setMapping(FIELD.START_DATE, true, ConstraintType.MUST_START_ON.name());
        GTask loadedTask = TestUtils.saveAndLoadViaSummary(connector, task, mappings);
        assertEquals(cal.getTime(), loadedTask.getStartDate());
    }

    @Test
    public void assigneeNotExported() throws Exception {
        GTask task = TestUtils.generateTask();
        GUser assignee = new GUser(123, "some user");
        task.setAssignee(assignee);
        GTask loadedTask = getTestSaver().unselectField(FIELD.ASSIGNEE).saveAndLoad(task);
        assertNull(loadedTask.getAssignee());
    }

    @Test
    public void testAssigneeExported() throws Exception {
        GTask task = TestUtils.generateTask();
        GUser assignee = new GUser(123, "some user");
        task.setAssignee(assignee);
        GTask loadedTask = getTestSaver().selectField(FIELD.ASSIGNEE).saveAndLoad(task);
        assertEquals(assignee.getId(), loadedTask.getAssignee().getId());
    }

    @Test
    public void testAssigneeExportedByDefault() throws Exception {
        GTask task = TestUtils.generateTask();
        GUser assignee = new GUser(123, "some user");
        task.setAssignee(assignee);
        GTask loadedTask = TestUtils.saveAndLoadViaSummary(connector, task, DefaultMSPMappings.generate());
        assertEquals(assignee.getId(), loadedTask.getAssignee().getId());
    }

    private static List<Task> saveAndLoad(MSPConfig config, FIELD field, boolean useMap, String mapTo, GTask... tasks) throws IOException, MPXJException, ConnectorException {
        MSPConfig temporaryClonedconfig = new MSPConfig(config);
        Mappings mappings = DefaultMSPMappings.generate();
        mappings.setMapping(field, useMap, mapTo);

        String fileName = "testdata.tmp";
        temporaryClonedconfig.setInputAbsoluteFilePath(fileName);
        temporaryClonedconfig.setOutputAbsoluteFilePath(fileName);
        MSPConnector connector = new MSPConnector(temporaryClonedconfig);

        connector.saveData(Arrays.asList(tasks), null, mappings);

        MSPFileReader fileReader = new MSPFileReader();
        ProjectFile projectFile = fileReader.readFile(temporaryClonedconfig.getInputAbsoluteFilePath());
        List<Task> loadedTasks = projectFile.getAllTasks();

        deleteFile(fileName);
        return loadedTasks;
    }

    private static Date getDateRoundedToMinutes() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    @Test
    public void notMappedDescriptionIsSetToEmpty() throws Exception {
        GTask task = TestUtils.generateTask();
        GTask loadedTask = getTestSaver().unselectField(FIELD.DESCRIPTION).saveAndLoad(task);
        assertEquals("", loadedTask.getDescription());
    }

    @Test
    public void testLoadTasks() throws Exception {
        new CommonTests().testLoadTasks(connector, DefaultMSPMappings.generate());
    }

    @Test
    public void descriptionSavedByDefault() throws Exception {
        new CommonTests().descriptionSavedByDefault(connector, DefaultMSPMappings.generate());
    }

    @Test
    public void descriptionSavedIfSelected() throws Exception {
        new CommonTests().descriptionSavedIfSelected(connector, DefaultMSPMappings.generate());
    }

    @Test
    public void twoTasksAreCreated() throws Exception {
        new CommonTests().testCreates2Tasks(connector, DefaultMSPMappings.generate());
    }

    private TestSaver getTestSaver() {
        return new TestSaver(connector, DefaultMSPMappings.generate());
    }
}
