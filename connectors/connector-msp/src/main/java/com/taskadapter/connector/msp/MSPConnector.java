package com.taskadapter.connector.msp;

import com.taskadapter.connector.common.AbstractConnector;
import com.taskadapter.connector.definition.*;
import com.taskadapter.connector.definition.exceptions.BadConfigException;
import com.taskadapter.connector.definition.exceptions.ConnectorException;
import com.taskadapter.connector.msp.write.MSXMLFileWriter;
import com.taskadapter.model.GTask;
import com.taskadapter.model.GTaskDescriptor.FIELD;

import net.sf.mpxj.MPXJException;
import net.sf.mpxj.ProjectFile;
import net.sf.mpxj.Task;
import net.sf.mpxj.TaskField;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class MSPConnector extends AbstractConnector<MSPConfig> implements FileBasedConnector {
    /**
     * Keep it the same to enable backward compatibility
     */
    public static final String ID = "Microsoft Project";

    public MSPConnector(MSPConfig config) {
        super(config);
    }

    @Override
    public void updateRemoteIDs(ConnectorConfig config, Map<Integer, String> remoteKeys, ProgressMonitor monitorIGNORED) throws ConnectorException {
        MSPConfig c = (MSPConfig) config;
        String fileName = c.getInputAbsoluteFilePath();
        try {
            ProjectFile projectFile = new MSPFileReader().readFile(fileName);
            List<Task> allTasks = projectFile.getAllTasks();

            for (Task mspTask : allTasks) {
                String createdTaskKey = remoteKeys.get(mspTask.getUniqueID());
                if (createdTaskKey != null) {
                    setFieldIfNotNull(c, FIELD.REMOTE_ID, mspTask, createdTaskKey);
                }
            }

            new MSXMLFileWriter(c).writeProject(projectFile);
        } catch (MPXJException e) {
            throw MSPExceptions.convertException(e);
        } catch (IOException e) {
            throw MSPExceptions.convertException(e);
        }
    }

    private void setFieldIfNotNull(MSPConfig config, FIELD field, Task mspTask, String value) {
        String v = config.getFieldMappings().getMappedTo(field);
        TaskField f = MSPUtils.getTaskFieldByName(v);
        mspTask.set(f, value);
    }

    private Object getField(MSPConfig config, FIELD field, Task mspTask) {
        String v = config.getFieldMappings().getMappedTo(field);
        TaskField f = MSPUtils.getTaskFieldByName(v);
        return mspTask.getCurrentValue(f);
    }

    @Override
    public void updateTasksByRemoteIds(List<GTask> tasksFromExternalSystem) throws ConnectorException {
        String fileName = config.getInputAbsoluteFilePath();
        MSXMLFileWriter writer = new MSXMLFileWriter(config);
        try {
            ProjectFile projectFile = new MSPFileReader().readFile(fileName);
            List<Task> allTasks = projectFile.getAllTasks();
            for (GTask gTask : tasksFromExternalSystem) {
                Task mspTask = findTaskByRemoteId(allTasks, gTask.getKey());
                writer.setTaskFields(projectFile, mspTask, gTask, true);
            }
            writer.writeProject(projectFile);
        } catch (MPXJException e) {
            throw MSPExceptions.convertException(e);
        } catch (IOException e) {
            throw MSPExceptions.convertException(e);
        }
    }

    private Task findTaskByRemoteId(List<Task> mspTasks,
                                    String requiredRemoteId) {
        for (Task gTask : mspTasks) {
            String taskRemoteId = (String) getField(config, FIELD.REMOTE_ID, gTask);
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
    public void validateCanUpdate() throws ValidationException {
        if (config.getInputAbsoluteFilePath().toLowerCase().endsWith(MSPFileReader.MPP_SUFFIX_LOWERCASE)) {
            throw new ValidationException("The Microsoft Project 'Input File Name' you provided ends with \"" +
                    MSPFileReader.MPP_SUFFIX_LOWERCASE + "\"."
                    + "\nTask Adapter can't write MPP files."
                    + "\nPlease replace the MPP file name with XML if you want to use \"Update\" operation.");
        }

    }
    
    @Override
    public GTask loadTaskByKey(String key) {
        throw new RuntimeException("not implemented");
    }
    
    @Override
    public List<GTask> loadData(ProgressMonitor monitorIGNORED) throws ConnectorException {
        ProjectFile projectFile;
        final MSPFileReader fileReader = new MSPFileReader();
        try {
            projectFile = fileReader.readFile(config.getInputAbsoluteFilePath());
        } catch (FileNotFoundException e) {
            throw new BadConfigException("MSP: Can't find file with name \"" + config.getInputAbsoluteFilePath() + "\".");
        } catch (MPXJException e) {
            throw MSPExceptions.convertException(e);
        }

        List<Task> mspTasks = projectFile.getAllTasks();
        mspTasks = skipRootNodeIfPresent(mspTasks);
        return loadTasks(projectFile, config, mspTasks);
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
    
    private List<GTask> loadTasks(ProjectFile project, MSPConfig config, List<Task> mspTasks) throws BadConfigException {
        final MSTaskToGenericTaskConverter converter = new MSTaskToGenericTaskConverter();
        converter.setConfig(config);
        converter.setHeader(project.getProjectHeader());
        // TODO add fieldMappings to the params!
        return converter.convertToGenericTaskList(mspTasks);
    }


    @Override
    public SyncResult<TaskSaveResult, TaskErrors<Throwable>> saveData(List<GTask> tasks, ProgressMonitor monitor) throws ConnectorException {
    	return new MSPTaskSaver(config).saveData(tasks, monitor);
    }
}
