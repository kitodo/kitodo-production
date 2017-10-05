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

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.json.simple.JSONObject;
import org.kitodo.data.database.beans.User;

/**
 * Implementation of User Type.
 */
public class UserType extends BaseType<User> {

    @SuppressWarnings("unchecked")
    @Override
    public HttpEntity createDocument(User user) {

        JSONObject userObject = new JSONObject();
        userObject.put("name", user.getName());
        userObject.put("surname", user.getSurname());
        userObject.put("login", user.getLogin());
        userObject.put("ldapLogin", user.getLdapLogin());
        userObject.put("active", user.isActive());
        userObject.put("location", user.getLocation());
        userObject.put("metadataLanguage", user.getMetadataLanguage());
        userObject.put("userGroups", addObjectRelation(user.getUserGroups(), true));
        userObject.put("filters", addObjectRelation(user.getFilters(), true));
        userObject.put("projects", addObjectRelation(user.getProjects(), true));
        userObject.put("processingTasks", addObjectRelation(user.getProcessingTasks()));
        userObject.put("tasks", addObjectRelation(user.getTasks()));

        return new NStringEntity(userObject.toJSONString(), ContentType.APPLICATION_JSON);
    }
}
