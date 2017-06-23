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
import org.kitodo.data.database.beans.Property;

/**
 * Implementation of Property Type.
 */
public class PropertyType extends BaseType<Property> {

    @SuppressWarnings("unchecked")
    @Override
    public HttpEntity createDocument(Property property) {

        JSONObject propertyObject = new JSONObject();
        propertyObject.put("title", property.getTitle());
        propertyObject.put("value", property.getValue());
        propertyObject.put("processes", addObjectRelation(property.getProcesses()));
        propertyObject.put("users", addObjectRelation(property.getUsers()));
        propertyObject.put("templates", addObjectRelation(property.getTemplates()));
        propertyObject.put("workpieces", addObjectRelation(property.getWorkpieces()));

        return new NStringEntity(propertyObject.toJSONString(), ContentType.APPLICATION_JSON);
    }
}
