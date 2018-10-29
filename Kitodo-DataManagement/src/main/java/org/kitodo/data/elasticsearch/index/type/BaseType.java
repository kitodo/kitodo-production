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
import java.util.Map;
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
import org.kitodo.data.database.beans.Role;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.elasticsearch.api.TypeInterface;
import org.kitodo.data.elasticsearch.index.type.enums.AuthorityTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.BatchTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.FilterTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.ProcessTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.ProjectTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.RoleTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.TaskTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.UserTypeField;

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
    public Map<Integer, HttpEntity> createDocuments(List<T> baseIndexedBeans) {
        Map<Integer, HttpEntity> documents = new HashMap<>();
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
     * @param addAdditionalProperties
     *            true or false, if true also additional information are included,
     *            type of information depends on the bean which is added as related
     *            object
     * @return JSONArray
     */
    <F extends BaseIndexedBean> JsonArray addObjectRelation(List<F> objects, boolean addAdditionalProperties) {
        JsonArrayBuilder result = Json.createArrayBuilder();
        if (objects != null) {
            for (F property : objects) {
                JsonObjectBuilder jsonObject = Json.createObjectBuilder();
                jsonObject.add(AuthorityTypeField.ID.getKey(), property.getId());
                if (addAdditionalProperties) {
                    getAdditionalProperties(jsonObject, property);
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

    private void getAdditionalProperties(JsonObjectBuilder jsonObject, BaseIndexedBean property) {
        if (property instanceof Batch) {
            Batch batch = (Batch) property;
            jsonObject.add(BatchTypeField.TITLE.getKey(), preventNull(batch.getTitle()));
            String type = batch.getType() != null ? batch.getType().toString() : "";
            jsonObject.add(BatchTypeField.TYPE.getKey(), type);
        } else if (property instanceof BaseTemplateBean) {
            jsonObject.add(ProcessTypeField.TITLE.getKey(),
                    preventNull(((BaseTemplateBean) property).getTitle()));
        } else if (property instanceof Project) {
            Project project = (Project) property;
            jsonObject.add(ProjectTypeField.TITLE.getKey(), preventNull(project.getTitle()));
            jsonObject.add(ProjectTypeField.ACTIVE.getKey(), project.isActive());
            if (Objects.nonNull(project.getClient())) {
                jsonObject.add(ProjectTypeField.CLIENT_ID.getKey(), project.getClient().getId());
            }
        } else if (property instanceof User) {
            User user = (User) property;
            jsonObject.add(UserTypeField.LOGIN.getKey(), preventNull(user.getLogin()));
            jsonObject.add(UserTypeField.NAME.getKey(), preventNull(user.getName()));
            jsonObject.add(UserTypeField.SURNAME.getKey(), preventNull(user.getSurname()));
        } else if (property instanceof Role) {
            jsonObject.add(RoleTypeField.TITLE.getKey(),
                    preventNull(((Role) property).getTitle()));
        } else if (property instanceof Task) {
            jsonObject.add(TaskTypeField.TITLE.getKey(), preventNull(((Task) property).getTitle()));
        } else if (property instanceof Filter) {
            jsonObject.add(FilterTypeField.VALUE.getKey(), preventNull(((Filter) property).getValue()));
        } else if (property instanceof Authority) {
            jsonObject.add(AuthorityTypeField.TITLE.getKey(),
                    preventNull(((Authority) property).getTitle()));
        }
    }

    /**
     * Method used for formatting Date as JsonValue. It will help to change fast a
     * way of Date formatting or expected String format.
     * 
     * @param date
     *            as Date
     * @return formatted date as JsonValue - String or NULL
     */
    JsonValue getFormattedDate(Date date) {
        if (Objects.nonNull(date)) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
