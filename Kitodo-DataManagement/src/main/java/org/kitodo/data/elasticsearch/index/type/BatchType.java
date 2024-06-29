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

import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.elasticsearch.index.type.enums.BatchTypeField;

/**
 * Implementation of Batch Type.
 */
public class BatchType extends BaseType<Batch> {

    @Override
    Map<String, Object> getJsonObject(Batch batch) {
        Map<String, Object> jsonObject = new HashMap<>();
        jsonObject.put(BatchTypeField.TITLE.getKey(), preventNull(batch.getTitle()));
        jsonObject.put(BatchTypeField.PROCESSES.getKey(), addObjectRelation(batch.getProcesses(), true));
        return jsonObject;
    }
}
