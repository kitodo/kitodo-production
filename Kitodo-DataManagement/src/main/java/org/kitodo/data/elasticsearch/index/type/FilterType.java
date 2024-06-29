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

import org.kitodo.data.database.beans.Filter;
import org.kitodo.data.elasticsearch.index.type.enums.FilterTypeField;

/**
 * Type class for Filter bean.
 */
public class FilterType extends BaseType<Filter> {

    @Override
    Map<String, Object> getJsonObject(Filter filter) {
        Map<String, Object> jsonObject = new HashMap<>();
        jsonObject.put(FilterTypeField.VALUE.getKey(), preventNull(filter.getValue()));
        jsonObject.put(FilterTypeField.USER.getKey(), getId(filter.getUser()));
        return jsonObject;
    }
}
