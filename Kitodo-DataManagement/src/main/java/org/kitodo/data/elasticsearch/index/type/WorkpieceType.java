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
import org.kitodo.data.database.beans.Workpiece;

/**
 * Implementation of Workpiece Type.
 */
public class WorkpieceType extends BaseType<Workpiece> {

    @SuppressWarnings("unchecked")
    @Override
    public HttpEntity createDocument(Workpiece workpiece) {

        JSONObject workpieceObject = new JSONObject();
        Integer process = workpiece.getProcess() != null ? workpiece.getProcess().getId() : null;
        workpieceObject.put("process", process);
        workpieceObject.put("properties", addPropertyRelation(workpiece.getProperties()));

        return new NStringEntity(workpieceObject.toJSONString(), ContentType.APPLICATION_JSON);
    }
}
