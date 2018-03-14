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

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.kitodo.data.database.beans.Process;

/**
 * Implementation of Process Type.
 */
public class ProcessType extends BaseType<Process> {

    @Override
    public HttpEntity createDocument(Process process) {
        String processBaseUri = process.getProcessBaseUri() != null ? process.getProcessBaseUri().getRawPath() : "";
        Integer projectId = process.getProject() != null ? process.getProject().getId() : 0;
        String projectTitle = process.getProject() != null ? process.getProject().getTitle() : "";
        boolean projectActive = process.getProject() != null && process.getProject().isActive();
        Integer ruleset = process.getRuleset() != null ? process.getRuleset().getId() : 0;
        Integer docket = process.getDocket() != null ? process.getDocket().getId() : 0;

        JsonObject processObject = Json.createObjectBuilder()
                .add("title", preventNull(process.getTitle()))
                .add("outputName", preventNull(process.getOutputName()))
                .add("creationDate", getFormattedDate(process.getCreationDate()))
                .add("wikiField", preventNull(process.getWikiField()))
                .add("sortHelperArticles", process.getSortHelperArticles())
                .add("sortHelperDocstructs", process.getSortHelperDocstructs())
                .add("sortHelperStatus", preventNull(process.getSortHelperStatus()))
                .add("sortHelperImages", process.getSortHelperImages())
                .add("sortHelperMetadata", process.getSortHelperMetadata())
                .add("processBaseUri", processBaseUri)
                .add("template", process.isTemplate())
                .add("project.id", projectId)
                .add("project.title", projectTitle)
                .add("project.active", projectActive)
                .add("ruleset", ruleset)
                .add("docket", docket)
                .add("batches", addObjectRelation(process.getBatches(), true))
                .add("tasks", addObjectRelation(process.getTasks(), true))
                .add("properties", addObjectRelation(process.getProperties()))
                .add("templates", addObjectRelation(process.getTemplates()))
                .add("workpieces", addObjectRelation(process.getWorkpieces()))
                .build();

        return new NStringEntity(processObject.toString(), ContentType.APPLICATION_JSON);
    }
}
