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

package org.kitodo.data.database.converter;

import javax.persistence.AttributeConverter;

import org.kitodo.data.database.enums.TaskEditType;

public class TaskEditTypeConverter implements AttributeConverter<TaskEditType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(TaskEditType taskStatus) {
        return taskStatus.getValue();
    }

    @Override
    public TaskEditType convertToEntityAttribute(Integer taskEditTypeValue) {
        return TaskEditType.getTypeFromValue(taskEditTypeValue);
    }
}
