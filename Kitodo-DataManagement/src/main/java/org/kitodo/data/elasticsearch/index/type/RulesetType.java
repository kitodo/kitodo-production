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

import java.util.HashMap;
import java.util.Map;

import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.elasticsearch.index.type.enums.RulesetTypeField;

/**
 * Implementation of Ruleset Type.
 */
public class RulesetType extends BaseType<Ruleset> {

    @Override
    Map<String, Object> getJsonObject(Ruleset ruleset) {
        Map<String, Object> jsonObject = new HashMap<>();
        jsonObject.put(RulesetTypeField.TITLE.getKey(), preventNull(ruleset.getTitle()));
        jsonObject.put(RulesetTypeField.FILE.getKey(), preventNull(ruleset.getFile()));
        jsonObject.put(RulesetTypeField.ORDER_METADATA_BY_RULESET.getKey(), ruleset.isOrderMetadataByRuleset());
        jsonObject.put(RulesetTypeField.ACTIVE.getKey(), ruleset.isActive());
        jsonObject.put(RulesetTypeField.CLIENT_ID.getKey(), getId(ruleset.getClient()));
        jsonObject.put(RulesetTypeField.CLIENT_NAME.getKey(), getTitle(ruleset.getClient()));
        return jsonObject;
    }
}
