package com.taskadapter.connector.jira;

public final class JqlBuilder {
    public static String findIssuesByProject(String projectKey) {
        return "project = " + projectKey;
    }

    public static String findIssuesByProjectAndFilterId(String projectKey, int filterId) {
        return "project = " + projectKey + " AND filter = " + filterId;
    }
}
