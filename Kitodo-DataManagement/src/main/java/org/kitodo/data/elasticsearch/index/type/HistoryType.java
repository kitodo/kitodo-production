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
import org.kitodo.data.database.beans.History;

/**
 * Implementation of History Type.
 */
public class HistoryType extends BaseType<History> {

    @SuppressWarnings("unchecked")
    @Override
    public HttpEntity createDocument(History history) {

        JSONObject historyObject = new JSONObject();
        historyObject.put("numericValue", history.getNumericValue());
        historyObject.put("stringValue", history.getStringValue());
        historyObject.put("type", history.getHistoryType().getValue());
        String date = history.getDate() != null ? formatDate(history.getDate()) : null;
        historyObject.put("date", date);
        Integer process = history.getProcess() != null ? history.getProcess().getId() : null;
        historyObject.put("process", process);

        return new NStringEntity(historyObject.toJSONString(), ContentType.APPLICATION_JSON);
    }
}
