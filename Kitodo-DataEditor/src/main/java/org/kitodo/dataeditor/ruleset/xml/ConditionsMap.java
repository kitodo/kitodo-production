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

package org.kitodo.dataeditor.ruleset.xml;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ConditionsMap extends HashMap<String, Map<String, Condition>> implements ConditionsMapInterface {

    public ConditionsMap(List<Condition> conditions) {
        for (Condition condition : conditions) {
            super.computeIfAbsent(condition.getKey(), unused -> new HashMap<>()).put(condition.getEquals(), condition);
        }
    }

    @Override
    public Iterable<String> getConditionKeys() {
        return super.keySet();
    }

    @Override
    public Condition getCondition(String key, String value) {
        return super.getOrDefault(key, Collections.emptyMap()).get(value);
    }
}
