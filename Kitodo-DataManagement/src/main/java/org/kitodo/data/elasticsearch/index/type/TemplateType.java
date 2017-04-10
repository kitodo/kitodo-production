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

import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.beans.TemplateProperty;

/**
 * Implementation of Template Type.
 */
public class TemplateType extends BaseType<Template> {

    @SuppressWarnings("unchecked")
    @Override
    public HttpEntity createDocument(Template template) {

        JSONObject processObject = new JSONObject();
        Integer process = template.getProcess() != null ? template.getProcess().getId() : null;
        processObject.put("process", process);

        JSONArray properties = new JSONArray();
        List<TemplateProperty> templateProperties = template.getProperties();
        for (TemplateProperty property : templateProperties) {
            JSONObject propertyObject = new JSONObject();
            propertyObject.put("title", property.getTitle());
            propertyObject.put("value", property.getValue());
            properties.add(propertyObject);
        }
        processObject.put("properties", properties);

        return new NStringEntity(processObject.toJSONString(), ContentType.APPLICATION_JSON);
    }
}
