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

import org.kitodo.data.database.beans.User;

/**
 * Implementation of User Type.
 */
public class UserType extends BaseType<User> {

    @Override
    JsonObject getJsonObject(User user) {
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        jsonObjectBuilder.add("name", preventNull(user.getName()));
        jsonObjectBuilder.add("surname", preventNull(user.getSurname()));
        jsonObjectBuilder.add("login", preventNull(user.getLogin()));
        jsonObjectBuilder.add("ldapLogin", preventNull(user.getLdapLogin()));
        jsonObjectBuilder.add("active", user.isActive());
        jsonObjectBuilder.add("location", preventNull(user.getLocation()));
        jsonObjectBuilder.add("metadataLanguage", preventNull(user.getMetadataLanguage()));
        jsonObjectBuilder.add("userGroups", addObjectRelation(user.getUserGroups(), true));
        jsonObjectBuilder.add("filters", addObjectRelation(user.getFilters(), true));
        jsonObjectBuilder.add("projects", addObjectRelation(user.getProjects(), true));
        jsonObjectBuilder.add("processingTasks", addObjectRelation(user.getProcessingTasks()));
        jsonObjectBuilder.add("tasks", addObjectRelation(user.getTasks()));
        return jsonObjectBuilder.build();
    }
}
