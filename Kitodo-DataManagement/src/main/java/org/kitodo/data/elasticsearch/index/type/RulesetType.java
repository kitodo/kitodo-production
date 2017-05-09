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
import org.kitodo.data.database.beans.Ruleset;

/**
 * Implementation of Ruleset Type.
 */
public class RulesetType extends BaseType<Ruleset> {

    @SuppressWarnings("unchecked")
    @Override
    public HttpEntity createDocument(Ruleset ruleset) {

        JSONObject rulesetObject = new JSONObject();
        rulesetObject.put("title", ruleset.getTitle());
        rulesetObject.put("file", ruleset.getFile());
        rulesetObject.put("fileContent", "");

        return new NStringEntity(rulesetObject.toJSONString(), ContentType.APPLICATION_JSON);
    }
}
