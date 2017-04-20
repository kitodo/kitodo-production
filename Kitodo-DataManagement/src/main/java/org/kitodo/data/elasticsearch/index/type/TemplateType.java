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
import org.kitodo.data.database.beans.Template;

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
        processObject.put("properties", addPropertyRelation(template.getProperties()));

        return new NStringEntity(processObject.toJSONString(), ContentType.APPLICATION_JSON);
    }
}
