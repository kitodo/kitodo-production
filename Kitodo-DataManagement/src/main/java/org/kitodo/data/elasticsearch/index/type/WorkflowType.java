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

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.kitodo.data.database.beans.Workflow;
import org.kitodo.data.elasticsearch.index.type.enums.WorkflowTypeField;

public class WorkflowType extends BaseType<Workflow> {

    @Override
    JsonObject getJsonObject(Workflow workflow) {
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        jsonObjectBuilder.add(WorkflowTypeField.TITLE.getKey(), preventNull(workflow.getTitle()));
        jsonObjectBuilder.add(WorkflowTypeField.FILE_NAME.getKey(), preventNull(workflow.getFileName()));
        jsonObjectBuilder.add(WorkflowTypeField.READY.getKey(), workflow.isReady());
        jsonObjectBuilder.add(WorkflowTypeField.ACTIVE.getKey(), workflow.isActive());
        jsonObjectBuilder.add(WorkflowTypeField.TASKS.getKey(), addObjectRelation(workflow.getTasks(), true));
        return jsonObjectBuilder.build();
    }
}
