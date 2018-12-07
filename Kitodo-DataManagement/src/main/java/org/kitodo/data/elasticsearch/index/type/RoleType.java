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

import java.util.Objects;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.kitodo.data.database.beans.Role;
import org.kitodo.data.elasticsearch.index.type.enums.RoleTypeField;

/**
 * Implementation of Role Type.
 */
public class RoleType extends BaseType<Role> {

    @Override
    JsonObject getJsonObject(Role role) {
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        jsonObjectBuilder.add(RoleTypeField.TITLE.getKey(), preventNull(role.getTitle()));
        jsonObjectBuilder.add(RoleTypeField.AUTHORITIES.getKey(), addObjectRelation(role.getAuthorities(), true));
        jsonObjectBuilder.add(RoleTypeField.USERS.getKey(), addObjectRelation(role.getUsers(), true));
        if (Objects.nonNull(role.getClient())) {
            jsonObjectBuilder.add(RoleTypeField.CLIENT_ID.getKey(), role.getClient().getId());
            jsonObjectBuilder.add(RoleTypeField.CLIENT_NAME.getKey(), preventNull(role.getClient().getName()));
        }
        return jsonObjectBuilder.build();
    }
}
