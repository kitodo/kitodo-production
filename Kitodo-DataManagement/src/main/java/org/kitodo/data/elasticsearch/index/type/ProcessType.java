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
    JsonObject getJsonObject(Process process) {
        String processBaseUri = process.getProcessBaseUri() != null ? process.getProcessBaseUri().getRawPath() : "";
        int projectId = process.getProject() != null ? process.getProject().getId() : 0;
        String projectTitle = process.getProject() != null ? process.getProject().getTitle() : "";
        boolean projectActive = process.getProject() != null && process.getProject().isActive();
        int projectClientId = process.getProject() != null ? getClientId(process.getProject().getClient()) : 0;
        int templateId = process.getTemplate() != null ? process.getTemplate().getId() : 0;
        String templateTitle = process.getTemplate() != null ? process.getTemplate().getTitle() : "";
        int ruleset = process.getRuleset() != null ? process.getRuleset().getId() : 0;
        int docket = process.getDocket() != null ? process.getDocket().getId() : 0;

        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        jsonObjectBuilder.add(ProcessTypeField.TITLE.getKey(), preventNull(process.getTitle()));
        jsonObjectBuilder.add(ProcessTypeField.CREATION_DATE.getKey(), getFormattedDate(process.getCreationDate()));
        jsonObjectBuilder.add(ProcessTypeField.WIKI_FIELD.getKey(), preventNull(process.getWikiField()));
        jsonObjectBuilder.add(ProcessTypeField.SORT_HELPER_ARTICLES.getKey(), process.getSortHelperArticles());
        jsonObjectBuilder.add(ProcessTypeField.SORT_HELPER_DOCSTRUCTS.getKey(), process.getSortHelperDocstructs());
        jsonObjectBuilder.add(ProcessTypeField.SORT_HELPER_STATUS.getKey(), preventNull(process.getSortHelperStatus()));
        jsonObjectBuilder.add(ProcessTypeField.SORT_HELPER_IMAGES.getKey(), process.getSortHelperImages());
        jsonObjectBuilder.add(ProcessTypeField.SORT_HELPER_METADATA.getKey(), process.getSortHelperMetadata());
        jsonObjectBuilder.add(ProcessTypeField.PROCESS_BASE_URI.getKey(), processBaseUri);
        jsonObjectBuilder.add(ProcessTypeField.TEMPLATE_ID.getKey(), templateId);
        jsonObjectBuilder.add(ProcessTypeField.TEMPLATE_TITLE.getKey(), templateTitle);
        jsonObjectBuilder.add(ProcessTypeField.PROJECT_ID.getKey(), projectId);
        jsonObjectBuilder.add(ProcessTypeField.PROJECT_TITLE.getKey(), projectTitle);
        jsonObjectBuilder.add(ProcessTypeField.PROJECT_ACTIVE.getKey(), projectActive);
        jsonObjectBuilder.add(ProcessTypeField.PROJECT_CLIENT_ID.getKey(), projectClientId);
        jsonObjectBuilder.add(ProcessTypeField.RULESET.getKey(), ruleset);
        jsonObjectBuilder.add(ProcessTypeField.DOCKET.getKey(), docket);
        jsonObjectBuilder.add(ProcessTypeField.BATCHES.getKey(), addObjectRelation(process.getBatches(), true));
        jsonObjectBuilder.add(ProcessTypeField.TASKS.getKey(), addObjectRelation(process.getTasks(), true));
        jsonObjectBuilder.add(ProcessTypeField.PROPERTIES.getKey(), addObjectRelation(process.getProperties()));
        jsonObjectBuilder.add(ProcessTypeField.TEMPLATES.getKey(), addObjectRelation(process.getTemplates()));
        jsonObjectBuilder.add(ProcessTypeField.WORKPIECES.getKey(), addObjectRelation(process.getWorkpieces()));
        return jsonObjectBuilder.build();
    }

    private int getClientId(Client client) {
        return Objects.nonNull(client) ? client.getId() : 0;
    }
}
