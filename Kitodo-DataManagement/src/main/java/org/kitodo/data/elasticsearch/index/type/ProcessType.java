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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.elasticsearch.index.type.enums.ProcessTypeField;

/**
 * Implementation of Process Type.
 */
public class ProcessType extends BaseType<Process> {

    @Override
    Map<String, Object> getJsonObject(Process process) {
        String processBaseUri = process.getProcessBaseUri() != null ? process.getProcessBaseUri().getRawPath() : "";
        int projectId = process.getProject() != null ? process.getProject().getId() : 0;
        String projectTitle = process.getProject() != null ? process.getProject().getTitle() : "";
        boolean projectActive = process.getProject() != null && process.getProject().isActive();
        int projectClientId = process.getProject() != null ? getClientId(process.getProject().getClient()) : 0;
        int templateId = process.getTemplate() != null ? process.getTemplate().getId() : 0;
        String templateTitle = process.getTemplate() != null ? process.getTemplate().getTitle() : "";
        int ruleset = process.getRuleset() != null ? process.getRuleset().getId() : 0;
        int docket = process.getDocket() != null ? process.getDocket().getId() : 0;

        Map<String, Object> jsonObject = new HashMap<>();
        jsonObject.put(ProcessTypeField.TITLE.getKey(), preventNull(process.getTitle()));
        jsonObject.put(ProcessTypeField.CREATION_DATE.getKey(), getFormattedDate(process.getCreationDate()));
        jsonObject.put(ProcessTypeField.WIKI_FIELD.getKey(), preventNull(process.getWikiField()));
        jsonObject.put(ProcessTypeField.SORT_HELPER_ARTICLES.getKey(), process.getSortHelperArticles());
        jsonObject.put(ProcessTypeField.SORT_HELPER_DOCSTRUCTS.getKey(), process.getSortHelperDocstructs());
        jsonObject.put(ProcessTypeField.SORT_HELPER_STATUS.getKey(), preventNull(process.getSortHelperStatus()));
        jsonObject.put(ProcessTypeField.SORT_HELPER_IMAGES.getKey(), process.getSortHelperImages());
        jsonObject.put(ProcessTypeField.SORT_HELPER_METADATA.getKey(), process.getSortHelperMetadata());
        jsonObject.put(ProcessTypeField.PROCESS_BASE_URI.getKey(), processBaseUri);
        jsonObject.put(ProcessTypeField.TEMPLATE_ID.getKey(), templateId);
        jsonObject.put(ProcessTypeField.TEMPLATE_TITLE.getKey(), templateTitle);
        jsonObject.put(ProcessTypeField.PROJECT_ID.getKey(), projectId);
        jsonObject.put(ProcessTypeField.PROJECT_TITLE.getKey(), projectTitle);
        jsonObject.put(ProcessTypeField.PROJECT_ACTIVE.getKey(), projectActive);
        jsonObject.put(ProcessTypeField.PROJECT_CLIENT_ID.getKey(), projectClientId);
        jsonObject.put(ProcessTypeField.RULESET.getKey(), ruleset);
        jsonObject.put(ProcessTypeField.DOCKET.getKey(), docket);
        jsonObject.put(ProcessTypeField.BATCHES.getKey(), addObjectRelation(process.getBatches(), true));
        jsonObject.put(ProcessTypeField.TASKS.getKey(), addObjectRelation(process.getTasks(), true));
        jsonObject.put(ProcessTypeField.PROPERTIES.getKey(), addObjectRelation(process.getProperties()));
        jsonObject.put(ProcessTypeField.TEMPLATES.getKey(), addObjectRelation(process.getTemplates()));
        jsonObject.put(ProcessTypeField.WORKPIECES.getKey(), addObjectRelation(process.getWorkpieces()));
        return jsonObject;
    }

    private int getClientId(Client client) {
        return Objects.nonNull(client) ? client.getId() : 0;
    }
}
