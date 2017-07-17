package com.taskadapter.connector.msp;

import com.taskadapter.connector.FieldRow;
import com.taskadapter.connector.NewConnector;
import com.taskadapter.connector.definition.DropInConnector;
import com.taskadapter.connector.definition.FileBasedConnector;
import com.taskadapter.connector.definition.Mappings;
import com.taskadapter.connector.definition.ProgressMonitor;
import com.taskadapter.connector.definition.SaveResult;
import com.taskadapter.connector.definition.exceptions.BadConfigException;
import com.taskadapter.connector.definition.exceptions.ConnectorException;
import com.taskadapter.connector.msp.write.RealWriter;
import com.taskadapter.connector.msp.write.ResourceManager;
import com.taskadapter.core.TaskKeeper;
import com.taskadapter.model.GTask;
import net.sf.mpxj.MPXJException;
import net.sf.mpxj.ProjectFile;
import net.sf.mpxj.Task;
import net.sf.mpxj.TaskField;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class MSPConnector implements NewConnector, FileBasedConnector, DropInConnector {
    /**
     * Keep it the same to enable backward compatibility
     */
    public static final String ID = "Microsoft Project";

    private MSPConfig config;

    public MSPConnector(MSPConfig config) {
        this.config = config;
    }

/*
    private void setFieldIfNotNull(Iterable<FieldRow> rows, FIELD field, Task mspTask, String value) {
        String v = mappings.getMappedTo(field);
        TaskField f = MSPUtils.getTaskFieldByName(v);
        mspTask.set(f, value);
    }

    private Object getField( Iterable<FieldRow> rows, FIELD field, Task mspTask) {
        String v = mappings.getMappedTo(field);
        TaskField f = MSPUtils.getTaskFieldByName(v);
        return mspTask.getCurrentValue(f);
    }*/

    @Override
    public void updateTasksByRemoteIds(List<GTask> tasksFromExternalSystem, Iterable<FieldRow> rows) throws ConnectorException {
       /* String fileName = config.getInputAbsoluteFilePath();
        try {
            ProjectFile projectFile = new MSPFileReader().readFile(fileName);
            List<Task> allTasks = projectFile.getAllTasks();
            for (GTask gTask : tasksFromExternalSystem) {
                Task mspTask = findTaskByRemoteId(rows, allTasks, gTask.getKey());

                TaskFieldsSetter setter = new TaskFieldsSetter(rows, mspTask, new ResourceManager(projectFile));
                boolean keepTaskId = true;
                setter.setFields(gTask, keepTaskId);
            }
            RealWriter.writeProject(config.getOutputAbsoluteFilePath(), projectFile);
        } catch (MPXJException e) {
            throw MSPExceptions.convertException(e);
        } catch (IOException e) {
            throw MSPExceptions.convertException(e);
        }*/
    }

/*
    private Task findTaskByRemoteId(Iterable<FieldRow> rows, List<Task> mspTasks, String requiredRemoteId) {
        for (Task gTask : mspTasks) {
            String taskRemoteId = (String) getField(rows, FIELD.REMOTE_ID, gTask);
            if (taskRemoteId == null) {
                // not all tasks will have remote IDs
                continue;
            }
            if (taskRemoteId.equals(requiredRemoteId)) {
                return gTask;
            }
        }
        return null;
    }
*/

    @Override
    public boolean fileExists() {
        File file = new File(config.getOutputAbsoluteFilePath());
        return file.exists() && file.length() > 0; // zero files are autogenerated by config
    }

    @Override
    public String getAbsoluteOutputFileName() {
        return config.getOutputAbsoluteFilePath();
    }

    @Override
    public void validateCanUpdate() throws BadConfigException {
        if (config.getInputAbsoluteFilePath().toLowerCase().endsWith(MSPFileReader.MPP_SUFFIX_LOWERCASE)) {
            throw new BadConfigException("The Microsoft Project 'Input File Name' you provided ends with \"" +
                    MSPFileReader.MPP_SUFFIX_LOWERCASE + "\"."
                    + "\nTask Adapter can't write MPP files."
                    + "\nPlease replace the MPP file name with XML if you want to use \"Update\" operation.");
        }

    }

    @Override
    public GTask loadTaskByKey(String key, Iterable<FieldRow> rows) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public List<GTask> loadData() {
        final String sourceFile = config.getInputAbsoluteFilePath();
        return loadInternal(sourceFile);
    }

    @Override
    public List<GTask> loadDropInData(File file, Mappings mappings,
                                      ProgressMonitor monitor) throws ConnectorException {
        return loadInternal(file.getAbsolutePath());
    }

    private List<GTask> loadInternal(final String sourceFile) {
        ProjectFile projectFile;
        final MSPFileReader fileReader = new MSPFileReader();
        try {
            projectFile = fileReader.readFile(sourceFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(new BadConfigException("MSP: Can't find file with name \"" + sourceFile + "\"."));
        } catch (MPXJException e) {
            throw new RuntimeException(MSPExceptions.convertException(e));
        }

        List<Task> mspTasks = projectFile.getAllTasks();
        mspTasks = skipRootNodeIfPresent(mspTasks);
        return loadTasks(projectFile, mspTasks);
    }

    /**
     * MSP XML file can have a root-level task with outline=0 - this is
     * a grouping task for everything (like "project root"), which should not be included in the tasks
     * list.
     *
     * @return flat list of tasks (not a tree!)
     */
    private List<Task> skipRootNodeIfPresent(List<Task> mspTasks) {
//        if (mspTasks.get(0).getParentTask() == null){
//            return mspTasks.subList(1, mspTasks.size());
//        }
        if ((mspTasks != null) && (!mspTasks.isEmpty())
                && mspTasks.get(0).getOutlineLevel().equals(0)) {
            mspTasks = mspTasks.subList(1, mspTasks.size());
        }
        return mspTasks;
    }

    private List<GTask> loadTasks(ProjectFile project, List<Task> mspTasks) {
        final MSPToGTask converter = new MSPToGTask();
        converter.setHeader(project.getProjectHeader());
        return converter.convertToGenericTaskList(mspTasks);
    }

    @Override
    public SaveResult saveData(TaskKeeper taskKeeper, List<GTask> tasks,
                                   ProgressMonitor monitor, Iterable<FieldRow> rows) {
        try {
            return new MSPTaskSaver(config, rows).saveData(tasks);
        } catch (ConnectorException e) {
            throw new RuntimeException(e);
        }
    }

}
