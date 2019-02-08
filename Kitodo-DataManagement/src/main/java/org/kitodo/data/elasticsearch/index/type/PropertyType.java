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

import java.util.HashMap;
import java.util.Map;

import org.kitodo.data.database.beans.Property;
import org.kitodo.data.elasticsearch.index.type.enums.PropertyTypeField;

/**
 * Implementation of Property Type.
 */
public class PropertyType extends BaseType<Property> {

    @Override
    Map<String, Object> getJsonObject(Property property) {
        String type = "";
        if (!property.getProcesses().isEmpty()) {
            type = "process";
        } else if (!property.getTemplates().isEmpty()) {
            type = "template";
        } else if (!property.getWorkpieces().isEmpty()) {
            type = "workpiece";
        }

        Map<String, Object> jsonObject = new HashMap<>();
        jsonObject.put(PropertyTypeField.TITLE.getKey(), preventNull(property.getTitle()));
        jsonObject.put(PropertyTypeField.VALUE.getKey(), preventNull(property.getValue()));
        jsonObject.put(PropertyTypeField.CREATION_DATE.getKey(), getFormattedDate(property.getCreationDate()));
        jsonObject.put(PropertyTypeField.PROCESSES.getKey(), addObjectRelation(property.getProcesses()));
        jsonObject.put(PropertyTypeField.TEMPLATES.getKey(), addObjectRelation(property.getTemplates()));
        jsonObject.put(PropertyTypeField.WORKPIECES.getKey(), addObjectRelation(property.getWorkpieces()));
        jsonObject.put(PropertyTypeField.TYPE.getKey(), type);
        return jsonObject;
    }
}
