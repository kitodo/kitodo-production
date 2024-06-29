/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.kitodo.data.elasticsearch.index.type;

import java.util.HashMap;
import java.util.Map;

import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.elasticsearch.index.type.enums.DocketTypeField;

/**
 * Implementation of Docket Type.
 */
public class DocketType extends BaseType<Docket> {

    @Override
    Map<String, Object> getJsonObject(Docket docket) {
        Map<String, Object> jsonObject = new HashMap<>();
        jsonObject.put(DocketTypeField.TITLE.getKey(), preventNull(docket.getTitle()));
        jsonObject.put(DocketTypeField.FILE.getKey(), preventNull(docket.getFile()));
        jsonObject.put(DocketTypeField.ACTIVE.getKey(), docket.isActive());
        jsonObject.put(DocketTypeField.CLIENT_ID.getKey(), getId(docket.getClient()));
        jsonObject.put(DocketTypeField.CLIENT_NAME.getKey(), getTitle(docket.getClient()));
        return jsonObject;
    }
}
