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

import org.json.simple.JSONObject;

import org.kitodo.data.database.beans.History;

/**
 * Implementation of History Type.
 */
public class HistoryType extends BaseType<History> {

    @SuppressWarnings("unchecked")
    @Override
    public HttpEntity createDocument(History history) {

        LinkedHashMap<String, String> orderedHistoryMap = new LinkedHashMap<>();
        orderedHistoryMap.put("numericValue", history.getNumericValue().toString());
        orderedHistoryMap.put("stringValue", history.getStringValue());
        orderedHistoryMap.put("type", history.getHistoryType().toString());
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = history.getDate() != null ? dateFormat.format(history.getDate()) : "null";
        orderedHistoryMap.put("date", date);
        orderedHistoryMap.put("process", history.getProcess().getId().toString());

        JSONObject historyObject = new JSONObject(orderedHistoryMap);

        return new NStringEntity(historyObject.toJSONString(), ContentType.APPLICATION_JSON);
    }

    @Override
    public HashMap<Integer, HttpEntity> createDocuments(List<History> histories) {
        HashMap<Integer, HttpEntity> documents = new HashMap<>();
        for (History history : histories) {
            documents.put(history.getId(), createDocument(history));
        }
        return documents;
    }
}
