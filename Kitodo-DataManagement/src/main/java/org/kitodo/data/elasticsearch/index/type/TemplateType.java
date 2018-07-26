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

import org.kitodo.data.database.beans.Template;
import org.kitodo.data.elasticsearch.index.type.enums.TemplateTypeField;

public class TemplateType extends BaseType<Template> {

    @Override
    public JsonObject getJsonObject(Template template) {

        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();

        jsonObjectBuilder.add(TemplateTypeField.TITLE.getName(), template.getTitle());
        jsonObjectBuilder.add(TemplateTypeField.OUTPUT_NAME.getName(), preventNull(template.getOutputName()));
        jsonObjectBuilder.add(TemplateTypeField.CREATION_DATE.getName(), getFormattedDate(template.getCreationDate()));
        jsonObjectBuilder.add(TemplateTypeField.WIKI_FIELD.getName(), preventNull(template.getWikiField()));
        jsonObjectBuilder.add(TemplateTypeField.SORT_HELPER_STATUS.getName(), preventNull(template.getSortHelperStatus()));
        String workflowTitle = template.getWorkflow() != null ? template.getWorkflow().getTitle() : "";
        jsonObjectBuilder.add(TemplateTypeField.WORKFLOW_TITLE.getName(), workflowTitle);
        String diagramFileName = template.getWorkflow() != null ? template.getWorkflow().getFileName() : "";
        jsonObjectBuilder.add(TemplateTypeField.WORKFLOW_FILE_NAME.getName(), diagramFileName);
        Integer projectId = template.getProject() != null ? template.getProject().getId() : 0;
        jsonObjectBuilder.add("project.id", projectId);
        String projectTitle = template.getProject() != null ? template.getProject().getTitle() : "";
        jsonObjectBuilder.add("project.title", projectTitle);
        boolean projectActive = template.getProject() != null && template.getProject().isActive();
        jsonObjectBuilder.add("project.active", projectActive);
        Integer ruleset = template.getRuleset() != null ? template.getRuleset().getId() : 0;
        jsonObjectBuilder.add("ruleset", ruleset);
        Integer docket = template.getDocket() != null ? template.getDocket().getId() : 0;
        jsonObjectBuilder.add("docket", docket);
        jsonObjectBuilder.add("tasks", addObjectRelation(template.getTasks(), true));

        return jsonObjectBuilder.build();
    }
}
