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

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.json.simple.JSONObject;
import org.kitodo.data.database.beans.Process;

/**
 * Implementation of Process Type.
 */
public class ProcessType extends BaseType<Process> {

    @SuppressWarnings("unchecked")
    @Override
    public HttpEntity createDocument(Process process) {

        JSONObject processObject = new JSONObject();
        processObject.put("title", process.getTitle());
        processObject.put("outputName", process.getOutputName());
        String creationDate = process.getCreationDate() != null ? formatDate(process.getCreationDate()) : null;
        processObject.put("creationDate", creationDate);
        processObject.put("wikiField", process.getWikiField());
        processObject.put("sortHelperStatus", process.getSortHelperStatus());
        processObject.put("sortHelperImages", process.getSortHelperImages());
        processObject.put("processBaseUri", process.getProcessBaseUri());
        processObject.put("template", process.isTemplate());
        Integer projectId = process.getProject() != null ? process.getProject().getId() : null;
        processObject.put("project.id", projectId);
        String projectTitle = process.getProject() != null ? process.getProject().getTitle() : null;
        processObject.put("project.title", projectTitle);
        boolean projectActive = process.getProject() != null && process.getProject().isActive();
        processObject.put("project.active", projectActive);
        Integer ruleset = process.getRuleset() != null ? process.getRuleset().getId() : null;
        processObject.put("ruleset", ruleset);
        Integer docket = process.getDocket() != null ? process.getDocket().getId() : null;
        processObject.put("docket", docket);
        processObject.put("batches", addObjectRelation(process.getBatches(), true));
        processObject.put("tasks", addObjectRelation(process.getTasks(), true));
        processObject.put("properties", addObjectRelation(process.getProperties()));
        processObject.put("templates", addObjectRelation(process.getTemplates()));
        processObject.put("workpieces", addObjectRelation(process.getWorkpieces()));

        return new NStringEntity(processObject.toJSONString(), ContentType.APPLICATION_JSON);
    }
}
