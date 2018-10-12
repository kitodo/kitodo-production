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

import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.data.elasticsearch.index.type.enums.UserGroupTypeField;

/**
 * Implementation of UserGroup Type.
 */
public class UserGroupType extends BaseType<UserGroup> {

    @Override
    JsonObject getJsonObject(UserGroup userGroup) {
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        jsonObjectBuilder.add(UserGroupTypeField.TITLE.getKey(), preventNull(userGroup.getTitle()));
        jsonObjectBuilder.add(UserGroupTypeField.AUTHORITIES.getKey(), addObjectRelation(userGroup.getAuthorities(), true));
        jsonObjectBuilder.add(UserGroupTypeField.USERS.getKey(), addObjectRelation(userGroup.getUsers(), true));
        jsonObjectBuilder.add(UserGroupTypeField.CLIENTS.getKey(), addObjectRelation(userGroup.getClients(), true));
        return jsonObjectBuilder.build();
    }
}
