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
import javax.json.JsonObjectBuilder;

import org.kitodo.data.database.beans.Property;
import org.kitodo.data.elasticsearch.index.type.enums.PropertyTypeField;

/**
 * Implementation of Property Type.
 */
public class PropertyType extends BaseType<Property> {

    @Override
    JsonObject getJsonObject(Property property) {
        String type = "";
        if (!property.getProcesses().isEmpty()) {
            type = "process";
        } else if (!property.getTemplates().isEmpty()) {
            type = "template";
        } else if (!property.getWorkpieces().isEmpty()) {
            type = "workpiece";
        }

        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        jsonObjectBuilder.add(PropertyTypeField.TITLE.getKey(), preventNull(property.getTitle()));
        jsonObjectBuilder.add(PropertyTypeField.VALUE.getKey(), preventNull(property.getValue()));
        jsonObjectBuilder.add(PropertyTypeField.CREATION_DATE.getKey(), getFormattedDate(property.getCreationDate()));
        jsonObjectBuilder.add(PropertyTypeField.PROCESSES.getKey(), addObjectRelation(property.getProcesses()));
        jsonObjectBuilder.add(PropertyTypeField.TEMPLATES.getKey(), addObjectRelation(property.getTemplates()));
        jsonObjectBuilder.add(PropertyTypeField.WORKPIECES.getKey(), addObjectRelation(property.getWorkpieces()));
        jsonObjectBuilder.add(PropertyTypeField.TYPE.getKey(), type);
        return jsonObjectBuilder.build();
    }
}
