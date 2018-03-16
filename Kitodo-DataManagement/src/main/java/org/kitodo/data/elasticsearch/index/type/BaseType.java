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
import java.util.Objects;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.kitodo.data.database.beans.Authority;
import org.kitodo.data.database.beans.BaseIndexedBean;
import org.kitodo.data.database.beans.BaseTemplateBean;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.beans.Filter;
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
    public HttpEntity createDocument(T baseIndexedBean) {
        JsonObject baseIndexedObject = getJsonObject(baseIndexedBean);

        return new NStringEntity(baseIndexedObject.toString(), ContentType.APPLICATION_JSON);
    }

    @Override
    public HashMap<Integer, HttpEntity> createDocuments(List<T> baseIndexedBeans) {
        HashMap<Integer, HttpEntity> documents = new HashMap<>();
        for (T bean : baseIndexedBeans) {
            documents.put(bean.getId(), createDocument(bean));
        }
        return documents;
    }

    abstract JsonObject getJsonObject(T baseIndexedBean);

    /**
     * Method for adding relationship between bean objects.
     * 
     * @param objects
     *            list
     * @param title
     *            true or false, if true also title information is included
     * @return JSONArray
     */
    <F extends BaseIndexedBean> JsonArray addObjectRelation(List<F> objects, boolean title) {
        JsonArrayBuilder result = Json.createArrayBuilder();
        if (objects != null) {
            for (F property : objects) {
                JsonObjectBuilder jsonObject = Json.createObjectBuilder();
                jsonObject.add("id", property.getId());
                if (title) {
                    if (property instanceof Batch) {
                        jsonObject.add("title", preventNull(((Batch) property).getTitle()));
                    } else if (property instanceof BaseTemplateBean) {
                        jsonObject.add("title", preventNull(((BaseTemplateBean) property).getTitle()));
                    } else if (property instanceof Project) {
                        jsonObject.add("title", preventNull(((Project) property).getTitle()));
                    } else if (property instanceof User) {
                        jsonObject.add("login", preventNull(((User) property).getLogin()));
                        jsonObject.add("name", preventNull(((User) property).getName()));
                        jsonObject.add("surname", preventNull(((User) property).getSurname()));
                    } else if (property instanceof UserGroup) {
                        jsonObject.add("title", preventNull(((UserGroup) property).getTitle()));
                    } else if (property instanceof Task) {
                        jsonObject.add("title", preventNull(((Task) property).getTitle()));
                    } else if (property instanceof Filter) {
                        jsonObject.add("value", preventNull(((Filter) property).getValue()));
                    } else if (property instanceof Authority) {
                        jsonObject.add("title", preventNull(((Authority) property).getTitle()));
                    }
                }
                result.add(jsonObject.build());
            }
        }
        return result.build();
    }

    /**
     * Method for adding relationship between bean objects.
     * 
     * @param objects
     *            list
     * @return JSONArray
     */
    @SuppressWarnings("unchecked")
    <F extends BaseIndexedBean> JsonArray addObjectRelation(List<F> objects) {
        return addObjectRelation(objects, false);
    }

    /**
     * Method used for formatting Date as JsonValue. It will help to change fast a way
     * of Date formatting or expected String format.
     * 
     * @param date
     *            as Date
     * @return formatted date as JsonValue - String or NULL
     */
    JsonValue getFormattedDate(Date date) {
        if (Objects.nonNull(date)) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            return Json.createValue(dateFormat.format(date));
        }
        return JsonValue.NULL;
    }

    String preventNull(String value) {
        if (Objects.isNull(value)) {
            value = "";
        }
        return value;
    }
}
