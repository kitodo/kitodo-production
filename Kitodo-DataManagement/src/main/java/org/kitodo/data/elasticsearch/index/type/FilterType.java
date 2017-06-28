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
import org.kitodo.data.database.beans.Filter;

/**
 * Type class for Filter bean.
 */
public class FilterType extends BaseType<Filter> {

    @SuppressWarnings("unchecked")
    @Override
    public HttpEntity createDocument(Filter filter) {

        JSONObject propertyObject = new JSONObject();
        propertyObject.put("value", filter.getValue());
        Integer user = filter.getUser() != null ? filter.getUser().getId() : null;
        propertyObject.put("user", user);

        return new NStringEntity(propertyObject.toJSONString(), ContentType.APPLICATION_JSON);
    }
}
