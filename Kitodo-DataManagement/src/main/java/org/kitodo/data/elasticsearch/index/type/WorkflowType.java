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

import org.kitodo.data.database.beans.Workflow;
import org.kitodo.data.elasticsearch.index.type.enums.WorkflowTypeField;

public class WorkflowType extends BaseType<Workflow> {

    @Override
    Map<String, Object> getJsonObject(Workflow workflow) {
        Map<String, Object> jsonObject = new HashMap<>();
        jsonObject.put(WorkflowTypeField.TITLE.getKey(), preventNull(workflow.getTitle()));
        jsonObject.put(WorkflowTypeField.STATUS.getKey(), workflow.getStatus());
        jsonObject.put(WorkflowTypeField.CLIENT_ID.getKey(), getId(workflow.getClient()));
        jsonObject.put(WorkflowTypeField.CLIENT_NAME.getKey(), getTitle(workflow.getClient()));
        return jsonObject;
    }
}
