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

import org.kitodo.data.database.beans.Template;
import org.kitodo.data.elasticsearch.index.type.enums.ProjectTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.TemplateTypeField;

public class TemplateType extends BaseType<Template> {

    @Override
    public Map<String, Object> getJsonObject(Template template) {
        String diagramFileName = template.getWorkflow() != null ? template.getWorkflow().getFileName() : "";

        Map<String, Object> jsonObject = new HashMap<>();
        jsonObject.put(TemplateTypeField.TITLE.getKey(), template.getTitle());
        jsonObject.put(TemplateTypeField.CREATION_DATE.getKey(), getFormattedDate(template.getCreationDate()));
        jsonObject.put(TemplateTypeField.ACTIVE.getKey(), template.isActive());
        jsonObject.put(TemplateTypeField.SORT_HELPER_STATUS.getKey(),
            preventNull(template.getSortHelperStatus()));
        jsonObject.put(TemplateTypeField.WORKFLOW_TITLE.getKey(), getTitle(template.getWorkflow()));
        jsonObject.put(TemplateTypeField.WORKFLOW_FILE_NAME.getKey(), diagramFileName);
        jsonObject.put(TemplateTypeField.RULESET.getKey(), getId(template.getRuleset()));
        jsonObject.put(TemplateTypeField.DOCKET.getKey(), getId(template.getDocket()));
        jsonObject.put(TemplateTypeField.PROJECTS.getKey(), addObjectRelation(template.getProjects(), true));
        if (template.getProjects().isEmpty()) {
            jsonObject.put(TemplateTypeField.PROJECTS + "." + ProjectTypeField.CLIENT_ID, 0);
        }
        jsonObject.put(TemplateTypeField.TASKS.getKey(), addObjectRelation(template.getTasks(), true));

        return jsonObject;
    }
}
