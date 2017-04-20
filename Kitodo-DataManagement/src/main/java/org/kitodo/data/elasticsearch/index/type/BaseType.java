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
import org.kitodo.data.database.beans.Property;
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
     * Method used for formatting Date as String. It will help to change fast a
     * way of Date formatting or expected String format.
     * 
     * @param date
     *            as Date
     * @return formatted date as String
     */
    protected String formatDate(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(date);
    }

    @SuppressWarnings("unchecked")
    protected JSONArray addPropertyRelation(List<Property> properties) {
        JSONArray jsonArrayProperties = new JSONArray();
        for (Property property : properties) {
            jsonArrayProperties.add(addIdForRelation(property.getId()));
        }
        return jsonArrayProperties;
    }

    @SuppressWarnings("unchecked")
    protected JSONObject addIdForRelation(Integer id) {
        JSONObject object = new JSONObject();
        object.put("id", id);
        return object;
    }
}
