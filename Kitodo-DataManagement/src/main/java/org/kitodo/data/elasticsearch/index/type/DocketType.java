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

import java.util.Objects;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.elasticsearch.index.type.enums.DocketTypeField;

/**
 * Implementation of Docket Type.
 */
public class DocketType extends BaseType<Docket> {

    @Override
    JsonObject getJsonObject(Docket docket) {
        Integer clientId = Objects.nonNull(docket.getClient()) ? docket.getClient().getId() : 0;
        String clientName = Objects.nonNull(docket.getClient()) ? docket.getClient().getName() : "";

        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        jsonObjectBuilder.add(DocketTypeField.TITLE.getKey(), preventNull(docket.getTitle()));
        jsonObjectBuilder.add(DocketTypeField.FILE.getKey(), preventNull(docket.getFile()));
        jsonObjectBuilder.add(DocketTypeField.ACTIVE.getKey(), docket.isActive());
        jsonObjectBuilder.add(DocketTypeField.CLIENT_ID.getKey(), clientId);
        jsonObjectBuilder.add(DocketTypeField.CLIENT_NAME.getKey(), clientName);
        return jsonObjectBuilder.build();
    }
}
