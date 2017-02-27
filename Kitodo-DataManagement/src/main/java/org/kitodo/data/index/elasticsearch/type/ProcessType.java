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

package org.kitodo.data.index.elasticsearch.type;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.ProcessProperty;

/**
 * Implementation of Process Type.
 */
public class ProcessType extends BaseType<Process> {

    @SuppressWarnings("unchecked")
    @Override
    public HttpEntity createDocument(Process process) {

        LinkedHashMap<String, String> orderedProcessMap = new LinkedHashMap<>();
        orderedProcessMap.put("name", process.getTitle());
        orderedProcessMap.put("outputName", process.getOutputName());
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String creationDate = process.getCreationDate() != null ? dateFormat.format(process.getCreationDate()) : "null";
        orderedProcessMap.put("creationDate", creationDate);
        orderedProcessMap.put("wikiField", process.getWikiField());
        String project = process.getProject() != null ? process.getProject().getId().toString() : "null";
        orderedProcessMap.put("project", project);
        String ruleset = process.getRuleset() != null ? process.getRuleset().getId().toString() : "null";
        orderedProcessMap.put("ruleset", ruleset);
        String ldapGroup = process.getDocket() != null ? process.getDocket().getId().toString() : "null";
        orderedProcessMap.put("ldapGroup", ldapGroup);

        JSONObject processObject = new JSONObject(orderedProcessMap);

        JSONArray properties = new JSONArray();
        List<ProcessProperty> processProperties = process.getProperties();
        for (ProcessProperty property : processProperties) {
            JSONObject propertyObject = new JSONObject();
            propertyObject.put("title", property.getTitle());
            propertyObject.put("value", property.getValue());
            properties.add(propertyObject);
        }
        processObject.put("properties", properties);

        return new NStringEntity(processObject.toJSONString(), ContentType.APPLICATION_JSON);
    }

    @Override
    public HashMap<Integer, HttpEntity> createDocuments(List<Process> processes) {
        HashMap<Integer, HttpEntity> documents = new HashMap<>();
        for (Process process : processes) {
            documents.put(process.getId(), createDocument(process));
        }
        return documents;
    }
}
