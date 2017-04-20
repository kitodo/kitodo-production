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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.kitodo.data.database.beans.BaseBean;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.data.elasticsearch.api.TypeInterface;

/**
 * Abstract class for Type class.
 */
public abstract class BaseType<T extends BaseBean> implements TypeInterface<T> {

    @Override
    public abstract HttpEntity createDocument(T baseBean);

    @Override
    public HashMap<Integer, HttpEntity> createDocuments(List<T> baseBeans) {
        HashMap<Integer, HttpEntity> documents = new HashMap<>();
        for (T bean : baseBeans) {
            documents.put(bean.getId(), createDocument(bean));
        }
        return documents;
    }

    /**
     * Method for adding relationship between some object and list of process
     * objects.
     * 
     * @param processes
     *            list
     * @return JSONArray
     */
    @SuppressWarnings("unchecked")
    JSONArray addProcessRelation(List<Process> processes) {
        JSONArray jsonArray = new JSONArray();
        if (processes != null) {
            for (Process process : processes) {
                jsonArray.add(addIdForRelation(process.getId()));
            }
        }
        return jsonArray;
    }

    /**
     * Method for adding relationship between some object and list of property
     * objects.
     * 
     * @param properties
     *            list
     * @return JSONArray
     */
    @SuppressWarnings("unchecked")
    JSONArray addPropertyRelation(List<Property> properties) {
        JSONArray jsonArray = new JSONArray();
        if (properties != null) {
            for (Property property : properties) {
                jsonArray.add(addIdForRelation(property.getId()));
            }
        }
        return jsonArray;
    }

    /**
     * Method for adding relationship between some object and list of user
     * objects.
     * 
     * @param users
     *            list
     * @return JSONArray
     */
    @SuppressWarnings("unchecked")
    JSONArray addUserRelation(List<User> users) {
        JSONArray jsonArray = new JSONArray();
        if (users != null) {
            for (User user : users) {
                jsonArray.add(addIdForRelation(user.getId()));
            }
        }
        return jsonArray;
    }

    /**
     * Method for adding relationship between some object and list of user group
     * objects.
     *
     * @param userGroups
     *            list
     * @return JSONArray
     */
    @SuppressWarnings("unchecked")
    JSONArray addUserGroupRelation(List<UserGroup> userGroups) {
        JSONArray jsonArray = new JSONArray();
        if (userGroups != null) {
            for (UserGroup userGroup : userGroups) {
                jsonArray.add(addIdForRelation(userGroup.getId()));
            }
        }
        return jsonArray;
    }

    /**
     * Method for adding id to JSONObject.
     *
     * @param id
     *            of object
     * @return JSONObject
     */
    @SuppressWarnings("unchecked")
    JSONObject addIdForRelation(Integer id) {
        JSONObject object = new JSONObject();
        object.put("id", id);
        return object;
    }

    /**
     * Method used for formatting Date as String. It will help to change fast a
     * way of Date formatting or expected String format.
     *
     * @param date
     *            as Date
     * @return formatted date as String
     */
    String formatDate(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(date);
    }
}
