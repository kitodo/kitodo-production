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
import org.kitodo.data.database.beans.Authorization;
import org.kitodo.data.database.beans.BaseIndexedBean;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.beans.Filter;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.data.elasticsearch.api.TypeInterface;

/**
 * Abstract class for Type class.
 */
public abstract class BaseType<T extends BaseIndexedBean> implements TypeInterface<T> {

    @Override
    public abstract HttpEntity createDocument(T baseIndexedBean);

    @Override
    public HashMap<Integer, HttpEntity> createDocuments(List<T> baseIndexedBeans) {
        HashMap<Integer, HttpEntity> documents = new HashMap<>();
        for (T bean : baseIndexedBeans) {
            documents.put(bean.getId(), createDocument(bean));
        }
        return documents;
    }

    /**
     * Method for adding relationship between bean objects.
     * 
     * @param objects
     *            list
     * @param title
     *            true or false, if true also title information is included
     * @return JSONArray
     */
    @SuppressWarnings("unchecked")
    <F extends BaseIndexedBean> JSONArray addObjectRelation(List<F> objects, boolean title) {
        JSONArray result = new JSONArray();
        if (objects != null) {
            for (F property : objects) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", property.getId());
                if (title) {
                    if (property instanceof Batch) {
                        jsonObject.put("title", ((Batch) property).getTitle());
                    } else if (property instanceof Process) {
                        jsonObject.put("title", ((Process) property).getTitle());
                    } else if (property instanceof Project) {
                        jsonObject.put("title", ((Project) property).getTitle());
                    } else if (property instanceof User) {
                        jsonObject.put("login", ((User) property).getLogin());
                        jsonObject.put("name", ((User) property).getName());
                        jsonObject.put("surname", ((User) property).getSurname());
                    } else if (property instanceof UserGroup) {
                        jsonObject.put("title", ((UserGroup) property).getTitle());
                    } else if (property instanceof Task) {
                        jsonObject.put("title", ((Task) property).getTitle());
                    } else if (property instanceof Filter) {
                        jsonObject.put("value", ((Filter) property).getValue());
                    } else if (property instanceof Authorization) {
                        jsonObject.put("title", ((Authorization) property).getTitle());
                    }
                }
                result.add(jsonObject);
            }
        }
        return result;
    }

    /**
     * Method for adding relationship between bean objects.
     * 
     * @param objects
     *            list
     * @return JSONArray
     */
    @SuppressWarnings("unchecked")
    <F extends BaseIndexedBean> JSONArray addObjectRelation(List<F> objects) {
        return addObjectRelation(objects, false);
    }

    /**
     * Method used for formatting Date as String. It will help to change fast a way
     * of Date formatting or expected String format.
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
