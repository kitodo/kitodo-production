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

import org.kitodo.data.database.beans.Authority;
import org.kitodo.data.elasticsearch.index.type.enums.AuthorityTypeField;

public class AuthorityType extends BaseType<Authority> {

    @Override
    JsonObject getJsonObject(Authority authority) {
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        jsonObjectBuilder.add(AuthorityTypeField.TITLE.getKey(), preventNull(authority.getTitle()));
        jsonObjectBuilder.add(AuthorityTypeField.ROLES.getKey(), addObjectRelation(authority.getRoles(), true));
        return jsonObjectBuilder.build();
    }
}
