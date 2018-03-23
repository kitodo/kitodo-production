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
        Integer ruleset = process.getRuleset() != null ? process.getRuleset().getId() : 0;
        Integer docket = process.getDocket() != null ? process.getDocket().getId() : 0;

        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        jsonObjectBuilder.add("title", preventNull(process.getTitle()));
        jsonObjectBuilder.add("outputName", preventNull(process.getOutputName()));
        jsonObjectBuilder.add("creationDate", getFormattedDate(process.getCreationDate()));
        jsonObjectBuilder.add("wikiField", preventNull(process.getWikiField()));
        jsonObjectBuilder.add("sortHelperArticles", process.getSortHelperArticles());
        jsonObjectBuilder.add("sortHelperDocstructs", process.getSortHelperDocstructs());
        jsonObjectBuilder.add("sortHelperStatus", preventNull(process.getSortHelperStatus()));
        jsonObjectBuilder.add("sortHelperImages", process.getSortHelperImages());
        jsonObjectBuilder.add("sortHelperMetadata", process.getSortHelperMetadata());
        jsonObjectBuilder.add("processBaseUri", processBaseUri);
        jsonObjectBuilder.add("template", process.isTemplate());
        jsonObjectBuilder.add("project.id", projectId);
        jsonObjectBuilder.add("project.title", projectTitle);
        jsonObjectBuilder.add("project.active", projectActive);
        jsonObjectBuilder.add("ruleset", ruleset);
        jsonObjectBuilder.add("docket", docket);
        jsonObjectBuilder.add("batches", addObjectRelation(process.getBatches(), true));
        jsonObjectBuilder.add("tasks", addObjectRelation(process.getTasks(), true));
        jsonObjectBuilder.add("properties", addObjectRelation(process.getProperties()));
        jsonObjectBuilder.add("templates", addObjectRelation(process.getTemplates()));
        jsonObjectBuilder.add("workpieces", addObjectRelation(process.getWorkpieces()));
        return jsonObjectBuilder.build();
    }
}
