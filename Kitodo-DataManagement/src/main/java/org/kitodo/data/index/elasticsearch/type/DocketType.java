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

import java.util.LinkedHashMap;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;

import org.json.simple.JSONObject;

import org.kitodo.data.database.beans.Docket;

/**
 * Implementation of Docket Type.
 */
public class DocketType extends BaseType<Docket> {

    @SuppressWarnings("unchecked")
    @Override
    public HttpEntity createDocument(Docket docket) {

        LinkedHashMap<String, String> orderedDocketMap = new LinkedHashMap<>();
        orderedDocketMap.put("name", docket.getName());
        orderedDocketMap.put("file", docket.getFile());
        JSONObject docketObject = new JSONObject(orderedDocketMap);

        return new NStringEntity(docketObject.toJSONString(), ContentType.APPLICATION_JSON);
    }
}
