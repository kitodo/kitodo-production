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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.kitodo.data.database.beans.BaseBean;
import org.kitodo.data.database.beans.BaseIndexedBean;
import org.kitodo.data.database.beans.BaseTemplateBean;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.Comment;
import org.kitodo.data.database.beans.Filter;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Role;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.Workflow;
import org.kitodo.data.elasticsearch.api.TypeInterface;
import org.kitodo.data.elasticsearch.index.type.enums.BatchTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.CommentTypeField;
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
    public Map<String, Object> createDocument(T baseIndexedBean) {
        return getJsonObject(baseIndexedBean);
    }

    @Override
    public Map<Integer, Map<String, Object>> createDocuments(List<T> baseIndexedBeans) {
        Map<Integer, Map<String, Object>> documents = new HashMap<>();
        for (T bean : baseIndexedBeans) {
            documents.put(bean.getId(), createDocument(bean));
        }
        return documents;
    }

    abstract Map<String, Object> getJsonObject(T baseIndexedBean);

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
    <F extends BaseBean> List addObjectRelation(List<F> objects, boolean addAdditionalProperties) {
        List<Map<String, Object>> jsonObjects = new ArrayList<>();
        for (F property : objects) {
            Map<String, Object> jsonObject = new HashMap<>();
            jsonObject.put(BatchTypeField.ID.getKey(), property.getId());
            if (addAdditionalProperties) {
                getAdditionalProperties(jsonObject, property);
            }
            jsonObjects.add(jsonObject);
        }
        return jsonObjects;
    }

    /**
     * Method for adding relationship between bean objects.
     *
     * @param objects
     *            list
     * @return JSONArray
     */
    <F extends BaseBean> List addObjectRelation(List<F> objects) {
        return addObjectRelation(objects, false);
    }

    private void getAdditionalProperties(Map<String, Object> jsonObject, BaseBean property) {
        if (property instanceof Batch) {
            Batch batch = (Batch) property;
            jsonObject.put(BatchTypeField.TITLE.getKey(), preventNull(batch.getTitle()));
        } else if (property instanceof BaseTemplateBean) {
            jsonObject.put(ProcessTypeField.TITLE.getKey(), preventNull(((BaseTemplateBean) property).getTitle()));
        } else if (property instanceof Comment) {
            jsonObject.put(CommentTypeField.MESSAGE.getKey(), preventNull(((Comment) property).getMessage()));
        } else if (property instanceof Project) {
            Project project = (Project) property;
            jsonObject.put(ProjectTypeField.TITLE.getKey(), preventNull(project.getTitle()));
            jsonObject.put(ProjectTypeField.ACTIVE.getKey(), project.isActive());
            if (Objects.nonNull(project.getClient())) {
                jsonObject.put(ProjectTypeField.CLIENT_ID.getKey(), project.getClient().getId());
            }
        } else if (property instanceof User) {
            User user = (User) property;
            jsonObject.put(UserTypeField.LOGIN.getKey(), preventNull(user.getLogin()));
            jsonObject.put(UserTypeField.NAME.getKey(), preventNull(user.getName()));
            jsonObject.put(UserTypeField.SURNAME.getKey(), preventNull(user.getSurname()));
        } else if (property instanceof Role) {
            jsonObject.put(RoleTypeField.TITLE.getKey(), preventNull(((Role) property).getTitle()));
        } else if (property instanceof Task) {
            jsonObject.put(TaskTypeField.TITLE.getKey(), preventNull(((Task) property).getTitle()));
        } else if (property instanceof Filter) {
            jsonObject.put(FilterTypeField.VALUE.getKey(), preventNull(((Filter) property).getValue()));
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
    String getFormattedDate(Date date) {
        if (Objects.nonNull(date)) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return dateFormat.format(date);
        }
        return "";
    }

    int preventNull(Integer value) {
        if (Objects.isNull(value)) {
            return 0;
        }
        return value;
    }

    String preventNull(String value) {
        if (Objects.isNull(value)) {
            return "";
        }
        return value;
    }

    int getId(BaseBean baseBean) {
        if (Objects.nonNull(baseBean)) {
            return baseBean.getId();
        }
        return 0;
    }

    String getTitle(BaseBean baseBean) {
        if (baseBean instanceof BaseTemplateBean) {
            return preventNull(((BaseTemplateBean) baseBean).getTitle());
        } else if (baseBean instanceof Project) {
            return preventNull(((Project) baseBean).getTitle());
        } else if (baseBean instanceof Workflow) {
            return preventNull(((Workflow) baseBean).getTitle());
        } else if (baseBean instanceof Client) {
            return preventNull(((Client) baseBean).getName());
        } else if (baseBean instanceof Ruleset) {
            return preventNull(((Ruleset) baseBean).getTitle());
        }
        return "";
    }
}
