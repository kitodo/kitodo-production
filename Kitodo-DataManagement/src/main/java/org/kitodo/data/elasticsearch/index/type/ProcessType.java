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

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.kitodo.data.database.beans.Process;
import org.kitodo.data.elasticsearch.index.type.enums.ProcessTypeField;

/**
 * Implementation of Process Type.
 */
public class ProcessType extends BaseType<Process> {

    @Override
    JsonObject getJsonObject(Process process) {
        String processBaseUri = process.getProcessBaseUri() != null ? process.getProcessBaseUri().getRawPath() : "";
        Integer projectId = process.getProject() != null ? process.getProject().getId() : 0;
        String projectTitle = process.getProject() != null ? process.getProject().getTitle() : "";
        boolean projectActive = process.getProject() != null && process.getProject().isActive();
        Integer templateId = process.getTemplate() != null ? process.getTemplate().getId() : 0;
        String templateTitle = process.getTemplate() != null ? process.getTemplate().getTitle() : "";
        Integer ruleset = process.getRuleset() != null ? process.getRuleset().getId() : 0;
        Integer docket = process.getDocket() != null ? process.getDocket().getId() : 0;

        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        jsonObjectBuilder.add(ProcessTypeField.TITLE.getName(), preventNull(process.getTitle()));
        jsonObjectBuilder.add(ProcessTypeField.OUTPUT_NAME.getName(), preventNull(process.getOutputName()));
        jsonObjectBuilder.add(ProcessTypeField.CREATION_DATE.getName(), getFormattedDate(process.getCreationDate()));
        jsonObjectBuilder.add(ProcessTypeField.WIKI_FIELD.getName(), preventNull(process.getWikiField()));
        jsonObjectBuilder.add(ProcessTypeField.SORT_HELPER_ARTICLES.getName(), process.getSortHelperArticles());
        jsonObjectBuilder.add(ProcessTypeField.SORT_HELPER_DOCSTRUCTS.getName(), process.getSortHelperDocstructs());
        jsonObjectBuilder.add(ProcessTypeField.SORT_HELPER_STATUS.getName(), preventNull(process.getSortHelperStatus()));
        jsonObjectBuilder.add(ProcessTypeField.SORT_HELPER_IMAGES.getName(), process.getSortHelperImages());
        jsonObjectBuilder.add(ProcessTypeField.SORT_HELPER_METADATA.getName(), process.getSortHelperMetadata());
        jsonObjectBuilder.add(ProcessTypeField.PROCESS_BASE_URI.getName(), processBaseUri);
        jsonObjectBuilder.add(ProcessTypeField.TEMPLATE_ID.getName(), templateId);
        jsonObjectBuilder.add(ProcessTypeField.TEMPLATE_TITLE.getName(), templateTitle);
        jsonObjectBuilder.add(ProcessTypeField.PROJECT_ID.getName(), projectId);
        jsonObjectBuilder.add(ProcessTypeField.PROJECT_TITLE.getName(), projectTitle);
        jsonObjectBuilder.add(ProcessTypeField.PROJECT_ACTIVE.getName(), projectActive);
        jsonObjectBuilder.add(ProcessTypeField.RULESET.getName(), ruleset);
        jsonObjectBuilder.add(ProcessTypeField.DOCKET.getName(), docket);
        jsonObjectBuilder.add(ProcessTypeField.BATCHES.getName(), addObjectRelation(process.getBatches(), true));
        jsonObjectBuilder.add(ProcessTypeField.TASKS.getName(), addObjectRelation(process.getTasks(), true));
        jsonObjectBuilder.add(ProcessTypeField.PROPERTIES.getName(), addObjectRelation(process.getProperties()));
        jsonObjectBuilder.add(ProcessTypeField.TEMPLATES.getName(), addObjectRelation(process.getTemplates()));
        jsonObjectBuilder.add(ProcessTypeField.WORKPIECES.getName(), addObjectRelation(process.getWorkpieces()));
        return jsonObjectBuilder.build();
    }
}
