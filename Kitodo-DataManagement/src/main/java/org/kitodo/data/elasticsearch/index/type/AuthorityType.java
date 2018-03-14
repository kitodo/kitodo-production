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
import org.kitodo.data.database.beans.Authority;

public class AuthorityType extends BaseType<Authority> {

    @Override
    public HttpEntity createDocument(Authority authority) {

        JsonObject authorityObject = Json.createObjectBuilder()
                .add("title", preventNull(authority.getTitle()))
                .add("title", preventNull(authority.getTitle()))
                .add("userGroups", addObjectRelation(authority.getUserGroups(), true)).build();

        return new NStringEntity(authorityObject.toString(), ContentType.APPLICATION_JSON);
    }
}
