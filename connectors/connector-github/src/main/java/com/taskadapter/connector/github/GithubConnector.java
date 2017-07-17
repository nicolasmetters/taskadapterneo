package com.taskadapter.connector.github;

import com.taskadapter.connector.common.DefaultValueSetter;
import com.taskadapter.connector.common.TaskSavingUtils;
import com.taskadapter.connector.definition.Connector;
import com.taskadapter.connector.definition.Mappings;
import com.taskadapter.connector.definition.ProgressMonitor;
import com.taskadapter.connector.definition.TaskSaveResult;
import com.taskadapter.connector.definition.WebServerInfo;
import com.taskadapter.connector.definition.exceptions.ConnectorException;
import com.taskadapter.connector.definition.exceptions.UnsupportedConnectorOperation;
import com.taskadapter.model.GTask;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.service.IssueService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GithubConnector implements Connector<GithubConfig> {

    /**
     * Keep it the same to enable backward compatibility with the existing
     * config files.
     */
    public static final String ID = "Github";

    private GithubConfig config;

    public GithubConnector(GithubConfig config) {
        this.config = config;
    }
    
    @Override
    public GTask loadTaskByKey(String key, Mappings mappings) throws ConnectorException {
        IssueService issueService = new ConnectionFactory(config.getServerInfo()).getIssueService();

        Integer id = Integer.valueOf(key);
        Issue issue;
        try {
            issue = issueService.getIssue(config.getServerInfo()
                    .getUserName(), config.getProjectKey(), id);
        } catch (IOException e) {
            throw GithubUtils.convertException(e);
        }
        return new GithubToGTask().toGtask(issue);
    }
    
    private IssueService getIssueService() {
        ConnectionFactory cf = new ConnectionFactory(config.getServerInfo());
        return cf.getIssueService();
    }

    @Override
    public List<GTask> loadData(Mappings mappings, ProgressMonitor monitorIGNORED) throws ConnectorException {
        Map<String, String> issuesFilter = new HashMap<>();
        issuesFilter.put(IssueService.FILTER_STATE,
                config.getIssueState() == null ? IssueService.STATE_OPEN
                        : config.getIssueState());

        IssueService issueService = getIssueService();
        List<Issue> issues;
        try {
            issues = issueService.getIssues(config.getServerInfo()
                    .getUserName(), config.getProjectKey(), issuesFilter);
        } catch (IOException e) {
            throw GithubUtils.convertException(e);
        }

        return new GithubToGTask().toGTaskList(issues);
    }
    

    @Override
    public TaskSaveResult saveData(List<GTask> tasks, ProgressMonitor monitor, Mappings mappings)
            throws ConnectorException {
        final WebServerInfo serverInfo = config.getServerInfo();
        final ConnectionFactory ghConnector = new ConnectionFactory(serverInfo);

        final GTaskToGithub converter = new GTaskToGithub(
                ghConnector.getUserService(), mappings.getSelectedFields());

        final IssueService issueService = ghConnector.getIssueService();
        final GithubTaskSaver saver = new GithubTaskSaver(issueService,
                serverInfo.getUserName(), config.getProjectKey());
        Map<String, String> defaultValuesForEmptyFields = mappings.getDefaultValuesForEmptyFields();
        return TaskSavingUtils.saveTasks(tasks, converter, saver, monitor, new DefaultValueSetter(defaultValuesForEmptyFields))
                .getResult();
    }
}
