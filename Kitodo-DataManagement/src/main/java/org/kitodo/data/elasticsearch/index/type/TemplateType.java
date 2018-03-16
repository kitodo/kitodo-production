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

public class TemplateType extends BaseType<Template> {

    @Override
    public JsonObject getJsonObject(Template template) {

        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();

        jsonObjectBuilder.add("title", template.getTitle());
        jsonObjectBuilder.add("outputName", preventNull(template.getOutputName()));
        jsonObjectBuilder.add("creationDate", getFormattedDate(template.getCreationDate()));
        jsonObjectBuilder.add("wikiField", preventNull(template.getWikiField()));
        jsonObjectBuilder.add("sortHelperStatus", preventNull(template.getSortHelperStatus()));
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
