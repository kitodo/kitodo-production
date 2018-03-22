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
        jsonObjectBuilder.add("title", preventNull(property.getTitle()));
        jsonObjectBuilder.add("value", preventNull(property.getValue()));
        jsonObjectBuilder.add("creationDate", getFormattedDate(property.getCreationDate()));
        jsonObjectBuilder.add("processes", addObjectRelation(property.getProcesses()));
        jsonObjectBuilder.add("templates", addObjectRelation(property.getTemplates()));
        jsonObjectBuilder.add("workpieces", addObjectRelation(property.getWorkpieces()));
        jsonObjectBuilder.add("type", type);
        return jsonObjectBuilder.build();
    }
}
