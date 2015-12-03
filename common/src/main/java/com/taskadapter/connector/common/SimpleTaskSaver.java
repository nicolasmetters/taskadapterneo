package com.taskadapter.connector.common;

import java.util.List;

import com.taskadapter.connector.common.data.ConnectorConverter;
import com.taskadapter.connector.definition.ProgressMonitor;
import com.taskadapter.connector.definition.TaskSaveResultBuilder;
import com.taskadapter.connector.definition.exceptions.ConnectorException;
import com.taskadapter.model.GTask;

final class SimpleTaskSaver<N> {

    private final TaskSaveResultBuilder result;
    private final ConnectorConverter<GTask, N> converter;
    private final BasicIssueSaveAPI<N> saveAPI;
    private final ProgressMonitor monitor;

    protected SimpleTaskSaver(ConnectorConverter<GTask, N> converter,
            BasicIssueSaveAPI<N> saveAPI, TaskSaveResultBuilder resultBuilder,
            ProgressMonitor progressMonitor) {
        this.result = resultBuilder;
        this.converter = converter;
        this.saveAPI = saveAPI;
        this.monitor = progressMonitor == null ? ProgressMonitorUtils
                .getDummyMonitor() : progressMonitor;
    }

    void saveTasks(String parentIssueKey, List<GTask> tasks, DefaultValueSetter defaultValueSetter) {
        for (GTask task : tasks) {
            String newTaskKey = null;
            try {
                if (parentIssueKey != null) {
                    task.setParentKey(parentIssueKey);
                }
                // TODO REVIEW Name mismatch. Why default value setter is used to clone tasks? Consider a better name.
                // Something like "TaskMapper", which could be an interface with the only method and several implementations.
                GTask taskWithDefaultValues = defaultValueSetter.cloneAndReplaceEmptySelectedFieldsWithDefaultValues(task);
                GTask finalGTaskForConversion = setKeyToRemoteIdIfPresent(taskWithDefaultValues);
                N nativeIssueToCreateOrUpdate = converter.convert(finalGTaskForConversion);
                newTaskKey = submitTask(finalGTaskForConversion, nativeIssueToCreateOrUpdate);
            } catch (ConnectorException e) {
                result.addTaskError(task, e);
            } catch (Throwable t) {
                result.addTaskError(task, t);
                t.printStackTrace();
            }
            monitor.worked(1);

            saveTasks(newTaskKey, task.getChildren(), defaultValueSetter);
        }
    }

    private static GTask setKeyToRemoteIdIfPresent(GTask gTask) {
        final GTask result = new GTask(gTask);
        final String remoteId = gTask.getRemoteId();
        if (remoteId != null) {
            result.setKey(remoteId);
        }
        return result;
    }

    /**
     * @return the newly created task's KEY
     */
    // TODO refactor? we only pass the GTask to check its IDs.
    private String submitTask(GTask task, N nativeTask)
            throws ConnectorException {
        String newTaskKey;
        if (task.getRemoteId() == null) {
            newTaskKey = saveAPI.createTask(nativeTask);
            result.addCreatedTask(task.getId(), newTaskKey);
        } else {
            newTaskKey = task.getRemoteId();
            saveAPI.updateTask(nativeTask);
            result.addUpdatedTask(task.getId(), newTaskKey);
        }
        return newTaskKey;
    }
}
