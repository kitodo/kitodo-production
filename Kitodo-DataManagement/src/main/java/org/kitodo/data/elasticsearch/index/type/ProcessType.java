/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.kitodo.data.elasticsearch.index.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.kitodo.data.database.beans.Comment;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.data.elasticsearch.index.converter.ProcessConverter;
import org.kitodo.data.elasticsearch.index.type.enums.ProcessTypeField;

/**
 * Implementation of Process Type.
 */
public class ProcessType extends BaseType<Process> {

    private static final String TITLE_FIELD_KEY = "title";
    private static final String VALUE_FIELD_KEY = "value";

    @Override
    Map<String, Object> getJsonObject(Process process) {
        String processBaseUri = process.getProcessBaseUri() != null ? process.getProcessBaseUri().getRawPath() : "";
        boolean projectActive = process.getProject() != null && process.getProject().isActive();
        int projectClientId = process.getProject() != null ? getId(process.getProject().getClient()) : 0;
        int processParentId = Objects.nonNull(process.getParent()) ? process.getParent().getId() : 0;

        Map<String, Object> jsonObject = new HashMap<>();
        jsonObject.put(ProcessTypeField.ID.getKey(), preventNull(process.getId()));
        jsonObject.put(ProcessTypeField.TITLE.getKey(), preventNull(process.getTitle()));
        jsonObject.put(ProcessTypeField.CREATION_DATE.getKey(), getFormattedDate(process.getCreationDate()));
        jsonObject.put(ProcessTypeField.WIKI_FIELD.getKey(), preventNull(process.getWikiField()));
        jsonObject.put(ProcessTypeField.SORT_HELPER_ARTICLES.getKey(), process.getSortHelperArticles());
        jsonObject.put(ProcessTypeField.SORT_HELPER_DOCSTRUCTS.getKey(), process.getSortHelperDocstructs());
        jsonObject.put(ProcessTypeField.SORT_HELPER_STATUS.getKey(), preventNull(process.getSortHelperStatus()));
        jsonObject.put(ProcessTypeField.SORT_HELPER_IMAGES.getKey(), process.getSortHelperImages());
        jsonObject.put(ProcessTypeField.SORT_HELPER_METADATA.getKey(), process.getSortHelperMetadata());
        jsonObject.put(ProcessTypeField.PROCESS_BASE_URI.getKey(), processBaseUri);
        jsonObject.put(ProcessTypeField.TEMPLATE_ID.getKey(), getId(process.getTemplate()));
        jsonObject.put(ProcessTypeField.TEMPLATE_TITLE.getKey(), getTitle(process.getTemplate()));
        jsonObject.put(ProcessTypeField.PROJECT_ID.getKey(), getId(process.getProject()));
        jsonObject.put(ProcessTypeField.PROJECT_TITLE.getKey(), getTitle(process.getProject()));
        jsonObject.put(ProcessTypeField.PROJECT_ACTIVE.getKey(), projectActive);
        jsonObject.put(ProcessTypeField.PROJECT_CLIENT_ID.getKey(), projectClientId);
        jsonObject.put(ProcessTypeField.RULESET.getKey(), getId(process.getRuleset()));
        jsonObject.put(ProcessTypeField.DOCKET.getKey(), getId(process.getDocket()));
        jsonObject.put(ProcessTypeField.BATCHES.getKey(), addObjectRelation(process.getBatches(), true));
        jsonObject.put(ProcessTypeField.COMMENTS.getKey(), addObjectRelation(process.getComments()));
        jsonObject.put(ProcessTypeField.COMMENTS_MESSAGE.getKey(), getProcessComments(process));
        jsonObject.put(ProcessTypeField.HAS_CHILDREN.getKey(), process.getChildren().size() > 0);
        jsonObject.put(ProcessTypeField.PARENT_ID.getKey(), processParentId);
        jsonObject.put(ProcessTypeField.TASKS.getKey(), addObjectRelation(process.getTasks(), true));
        jsonObject.put(ProcessTypeField.METADATA.getKey(), process.getMetadata());
        jsonObject.put(ProcessTypeField.NUMBER_OF_METADATA.getKey(), process.getNumberOfMetadata());
        jsonObject.put(ProcessTypeField.NUMBER_OF_IMAGES.getKey(), process.getNumberOfImages());
        jsonObject.put(ProcessTypeField.NUMBER_OF_STRUCTURES.getKey(), process.getNumberOfStructures());
        jsonObject.put(ProcessTypeField.PROPERTIES.getKey(), getProperties(process));
        jsonObject.put(ProcessTypeField.BASE_TYPE.getKey(), process.getBaseType());
        jsonObject.put(ProcessTypeField.IN_CHOICE_LIST_SHOWN.getKey(), process.getInChoiceListShown());
        jsonObject.put(ProcessTypeField.LAST_EDITING_USER.getKey(), ProcessConverter.getLastEditingUser(process));
        jsonObject.put(
            ProcessTypeField.CORRECTION_COMMENT_STATUS.getKey(), 
            ProcessConverter.getCorrectionCommentStatus(process).getValue()
        );
        convertLastProcessingTask(jsonObject, process);
        convertProgressStatus(jsonObject, process);

        return jsonObject;
    }

    /**
     * Adds last processing task dates to json object for indexing.
     * 
     * @param jsonObject the json object used for indexing
     * @param process the process being index
     */
    private void convertLastProcessingTask(Map<String, Object> jsonObject, Process process) {
        jsonObject.put(
            ProcessTypeField.PROCESSING_BEGIN_LAST_TASK.getKey(), 
            getFormattedDate(ProcessConverter.getLastProcessingBegin(process))
        );
        jsonObject.put(
            ProcessTypeField.PROCESSING_END_LAST_TASK.getKey(), 
            getFormattedDate(ProcessConverter.getLastProcessingEnd(process))
        );
    }

    /**
     * Adds progress status properties to json object for indexing.
     * 
     * @param jsonObject the json object used for indexing
     * @param process the process being index
     */
    private void convertProgressStatus(Map<String, Object> jsonObject, Process process) {
        // calculate and save process status
        Map<TaskStatus, Double> taskProgress = ProcessConverter.getTaskProgressPercentageOfProcess(process, true);
        jsonObject.put(ProcessTypeField.PROGRESS_CLOSED.getKey(), taskProgress.get(TaskStatus.DONE));
        jsonObject.put(ProcessTypeField.PROGRESS_IN_PROCESSING.getKey(), taskProgress.get(TaskStatus.INWORK));
        jsonObject.put(ProcessTypeField.PROGRESS_OPEN.getKey(), taskProgress.get(TaskStatus.OPEN));
        jsonObject.put(ProcessTypeField.PROGRESS_LOCKED.getKey(), taskProgress.get(TaskStatus.LOCKED));
        jsonObject.put(
            ProcessTypeField.PROGRESS_COMBINED.getKey(), 
            ProcessConverter.getCombinedProgressFromTaskPercentages(taskProgress)
        );
    }

    private List<Map<String, String>> getProperties(Process process) {
        List<Property> properties = process.getProperties();
        List<Map<String, String>> propertiesForIndex = new ArrayList<>();
        for (Property property : properties) {
            HashMap<String, String> propertyMap = new HashMap<>();
            propertyMap.put(TITLE_FIELD_KEY, property.getTitle());
            propertyMap.put(VALUE_FIELD_KEY, property.getValue());
            propertiesForIndex.add(propertyMap);
        }
        properties = process.getTemplates();
        for (Property property : properties) {
            HashMap<String, String> propertyMap = new HashMap<>();
            propertyMap.put(TITLE_FIELD_KEY, property.getTitle());
            propertyMap.put(VALUE_FIELD_KEY, property.getValue());
            propertiesForIndex.add(propertyMap);
        }
        properties = process.getWorkpieces();
        for (Property property : properties) {
            HashMap<String, String> propertyMap = new HashMap<>();
            propertyMap.put(TITLE_FIELD_KEY, property.getTitle());
            propertyMap.put(VALUE_FIELD_KEY, property.getValue());
            propertiesForIndex.add(propertyMap);
        }
        return propertiesForIndex;
    }

    private String getProcessComments(Process process) {
        String commentsMessages = "";
        List<Comment> processComments = process.getComments();
        for (Comment comment : processComments) {
            if (Objects.nonNull(comment) && Objects.nonNull(comment.getMessage())) {
                commentsMessages = commentsMessages.concat(comment.getMessage());
            }
        }
        return commentsMessages;
    }
}
