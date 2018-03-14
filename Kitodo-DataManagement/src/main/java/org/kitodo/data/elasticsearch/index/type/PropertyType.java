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

import javax.json.Json;
import javax.json.JsonObject;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.kitodo.data.database.beans.Property;

/**
 * Implementation of Property Type.
 */
public class PropertyType extends BaseType<Property> {

    @Override
    public HttpEntity createDocument(Property property) {
        String type = "";
        if (!property.getProcesses().isEmpty()) {
            type = "process";
        } else if (!property.getTemplates().isEmpty()) {
            type = "template";
        } else if (!property.getWorkpieces().isEmpty()) {
            type = "workpiece";
        }

        JsonObject propertyObject = Json.createObjectBuilder()
                .add("title", preventNull(property.getTitle()))
                .add("value", preventNull(property.getValue()))
                .add("creationDate", getFormattedDate(property.getCreationDate()))
                .add("processes", addObjectRelation(property.getProcesses()))
                .add("templates", addObjectRelation(property.getTemplates()))
                .add("workpieces", addObjectRelation(property.getWorkpieces()))
                .add("type", type)
                .build();

        return new NStringEntity(propertyObject.toString(), ContentType.APPLICATION_JSON);
    }
}
