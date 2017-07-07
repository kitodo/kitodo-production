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
        Integer project = process.getProject() != null ? process.getProject().getId() : null;
        processObject.put("project", project);
        Integer ruleset = process.getRuleset() != null ? process.getRuleset().getId() : null;
        processObject.put("ruleset", ruleset);
        Integer docket = process.getDocket() != null ? process.getDocket().getId() : null;
        processObject.put("docket", docket);
        processObject.put("batches", addObjectRelation(process.getBatches()));
        processObject.put("tasks", addObjectRelation(process.getTasks()));
        processObject.put("properties", addObjectRelation(process.getProperties()));

        return new NStringEntity(processObject.toJSONString(), ContentType.APPLICATION_JSON);
    }
}
