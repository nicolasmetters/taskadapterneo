package com.taskadapter.connector.mantis;

import com.taskadapter.mantisapi.beans.AccountData;
import com.taskadapter.mantisapi.beans.IssueData;
import com.taskadapter.mantisapi.beans.RelationshipData;
import com.taskadapter.model.GRelation;
import com.taskadapter.model.GTask;
import com.taskadapter.model.GUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class MantisDataConverter {
    private static final Logger logger = LoggerFactory.getLogger(MantisDataConverter.class);

    private static HashMap<String, Integer> priorityNumbers = new HashMap<String, Integer>() {
        private static final long serialVersionUID = 516389048716909610L;

        {
            // TODO this can be moved to properties section to be defined by
            // user.
            put("none", 100);
            put("low", 100);
            put("normal", 500);
            put("high", 700);
            put("urgent", 800);
            put("immediate", 800);
        }
    };

    public static GUser convertToGUser(AccountData mantisUser) {
        GUser user = new GUser();
        user.setId(mantisUser.getId().intValue());
        //user.setId(new Integer(mantisUser.getId().intValue()));
        user.setLoginName(mantisUser.getName());
        return user;
    }

    public static GTask convertToGenericTask(IssueData issue) {
        GTask task = new GTask();

        task.setId(issue.getId().intValue());
        task.setKey(String.valueOf(issue.getId()));
        // task.setParentId(parentId);

        AccountData mntUser = issue.getHandler();
        if (mntUser != null) {
            GUser ass = new GUser(mntUser.getId().intValue(), mntUser.getName());
            task.setAssignee(ass);
        }

        // task.setType(type);
        task.setSummary(issue.getSummary());
        // task.setEstimatedHours(estimatedHours); only in string values for ex.
        // (< 1 day, 2-3 days, < 1week, < 1 month)
        // task.setDoneRatio(doneRatio);
        // task.setStartDate(startDate);
        // task.setDueDate(dueDate);
        task.setCreatedOn(issue.getDate_submitted().getTime());
        task.setUpdatedOn(issue.getLast_updated().getTime());

        Integer priorityValue = priorityNumbers.get(issue.getPriority().getName());
        task.setPriority(priorityValue);

        task.setDescription(issue.getDescription());
        if (issue.getDue_date() != null) {
            task.setDueDate(issue.getDue_date().getTime());
        }

        processRelations(issue, task);

        return task;
    }

    private static void processRelations(IssueData mntIssue, GTask genericTask) {
        RelationshipData[] relations = mntIssue.getRelationships();
        if (relations != null) {
            for (RelationshipData relation : relations) {
                if (relation.getType().getName().equals("child of")) {
                    GRelation r = new GRelation(String.valueOf(relation.getId()),
                            String.valueOf(relation.getTarget_id()),
                            GRelation.TYPE.precedes);
                    genericTask.getRelations().add(r);
                } else {
                    logger.info("relation type is not supported: " + relation.getType());
                }
            }
        }
    }

}
