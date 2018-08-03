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
import org.kitodo.data.elasticsearch.index.type.enums.UserTypeField;

/**
 * Implementation of User Type.
 */
public class UserType extends BaseType<User> {

    @Override
    JsonObject getJsonObject(User user) {
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        jsonObjectBuilder.add(UserTypeField.NAME.getKey(), preventNull(user.getName()));
        jsonObjectBuilder.add(UserTypeField.SURNAME.getKey(), preventNull(user.getSurname()));
        jsonObjectBuilder.add(UserTypeField.LOGIN.getKey(), preventNull(user.getLogin()));
        jsonObjectBuilder.add(UserTypeField.LDAP_LOGIN.getKey(), preventNull(user.getLdapLogin()));
        jsonObjectBuilder.add(UserTypeField.ACTIVE.getKey(), user.isActive());
        jsonObjectBuilder.add(UserTypeField.LOCATION.getKey(), preventNull(user.getLocation()));
        jsonObjectBuilder.add(UserTypeField.METADATA_LANGUAGE.getKey(), preventNull(user.getMetadataLanguage()));
        jsonObjectBuilder.add(UserTypeField.USER_GROUPS.getKey(), addObjectRelation(user.getUserGroups(), true));
        jsonObjectBuilder.add(UserTypeField.FILTERS.getKey(), addObjectRelation(user.getFilters(), true));
        jsonObjectBuilder.add(UserTypeField.PROJECTS.getKey(), addObjectRelation(user.getProjects(), true));
        jsonObjectBuilder.add(UserTypeField.CLIENTS.getKey(), addObjectRelation(user.getClients(), true));
        jsonObjectBuilder.add(UserTypeField.PROCESSING_TASKS.getKey(), addObjectRelation(user.getProcessingTasks()));
        jsonObjectBuilder.add(UserTypeField.TASKS.getKey(), addObjectRelation(user.getTasks()));
        return jsonObjectBuilder.build();
    }
}
