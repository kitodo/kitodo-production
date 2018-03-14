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
import org.kitodo.data.database.beans.User;

/**
 * Implementation of User Type.
 */
public class UserType extends BaseType<User> {

    @Override
    public HttpEntity createDocument(User user) {

        JsonObject userObject = Json.createObjectBuilder()
                .add("name", preventNull(user.getName()))
                .add("surname", preventNull(user.getSurname()))
                .add("login", preventNull(user.getLogin()))
                .add("ldapLogin", preventNull(user.getLdapLogin()))
                .add("active", user.isActive())
                .add("location", preventNull(user.getLocation()))
                .add("metadataLanguage", preventNull(user.getMetadataLanguage()))
                .add("userGroups", addObjectRelation(user.getUserGroups(), true))
                .add("filters", addObjectRelation(user.getFilters(), true))
                .add("projects", addObjectRelation(user.getProjects(), true))
                .add("processingTasks", addObjectRelation(user.getProcessingTasks()))
                .add("tasks", addObjectRelation(user.getTasks()))
                .build();

        return new NStringEntity(userObject.toString(), ContentType.APPLICATION_JSON);
    }
}
