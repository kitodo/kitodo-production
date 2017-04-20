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

import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.beans.Workpiece;

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
        propertyObject.put("processes", addProcessRelation(property.getProcesses()));
        propertyObject.put("users", addUserRelation(property.getUsers()));

        JSONArray templates = new JSONArray();
        List<Template> propertyTemplates = property.getTemplates();
        if (propertyTemplates != null) {
            for (Template template : propertyTemplates) {
                templates.add(addIdForRelation(template.getId()));
            }
        }
        propertyObject.put("templates", templates);

        JSONArray workpieces = new JSONArray();
        List<Workpiece> propertyWorkpieces = property.getWorkpieces();
        if (propertyWorkpieces != null) {
            for (Workpiece workpiece : propertyWorkpieces) {
                workpieces.add(addIdForRelation(workpiece.getId()));
            }
        }
        propertyObject.put("workpieces", workpieces);

        return new NStringEntity(propertyObject.toJSONString(), ContentType.APPLICATION_JSON);
    }
}
