package com.taskadapter.connector.mantis;

import com.taskadapter.connector.definition.ConnectorConfig;

import java.util.HashMap;
import java.util.Map;

public class MantisConfig extends ConnectorConfig {

    static final Map<String, Integer> DEFAULT_PRIORITIES = new HashMap<>();
    static {
        DEFAULT_PRIORITIES.put("low", 100);
        DEFAULT_PRIORITIES.put("normal", 500);
        DEFAULT_PRIORITIES.put("high", 700);
        DEFAULT_PRIORITIES.put("urgent", 800);
        DEFAULT_PRIORITIES.put("immediate", 1000);
    }
    
    static final String DEFAULT_LABEL = "MantisBT";

    private static final long serialVersionUID = 1L;
    private String projectKey;
    private boolean findUserByName;
    private Long queryId;

    public MantisConfig() {
        super(DEFAULT_PRIORITIES);
        setLabel(DEFAULT_LABEL);
    }

    public String getProjectKey() {
        return projectKey;
    }
    
    public Long getQueryId() {
        return queryId;
    }

    public void setQueryId(Long queryId) {
        this.queryId = queryId;
    }
    
    public String getQueryIdStr() {
        return queryId == null ? "" : queryId.toString();
    }

    public void setQueryIdStr(String id) {
        if (id == null || id.isEmpty())
            this.queryId = null;
        else
            this.queryId = Long.parseLong(id);
    }



    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public boolean isFindUserByName() {
        return findUserByName;
    }

    public void setFindUserByName(boolean findUserByName) {
        this.findUserByName = findUserByName;
    }
}
