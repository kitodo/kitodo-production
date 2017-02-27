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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import org.kitodo.data.database.beans.Workpiece;
import org.kitodo.data.database.beans.WorkpieceProperty;

/**
 * Implementation of Workpiece Type.
 */
public class WorkpieceType extends BaseType<Workpiece> {

    @SuppressWarnings("unchecked")
    @Override
    public HttpEntity createDocument(Workpiece workpiece) {

        LinkedHashMap<String, String> orderedWorkpieceMap = new LinkedHashMap<>();
        String process = workpiece.getProcess() != null ? workpiece.getProcess().getId().toString() : "null";
        orderedWorkpieceMap.put("process", process);

        JSONObject processObject = new JSONObject(orderedWorkpieceMap);

        JSONArray properties = new JSONArray();
        List<WorkpieceProperty> workpieceProperties = workpiece.getProperties();
        for (WorkpieceProperty property : workpieceProperties) {
            JSONObject propertyObject = new JSONObject();
            propertyObject.put("title", property.getTitle());
            propertyObject.put("value", property.getValue());
            properties.add(propertyObject);
        }
        processObject.put("properties", properties);

        return new NStringEntity(processObject.toJSONString(), ContentType.APPLICATION_JSON);
    }

    @Override
    public HashMap<Integer, HttpEntity> createDocuments(List<Workpiece> workpieces) {
        HashMap<Integer, HttpEntity> documents = new HashMap<>();
        for (Workpiece workpiece : workpieces) {
            documents.put(workpiece.getId(), createDocument(workpiece));
        }
        return documents;
    }
}
