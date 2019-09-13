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

package org.kitodo.production.migration;

import java.util.Comparator;
import java.util.Objects;

import org.kitodo.data.database.beans.Task;

public class TaskComparator implements Comparator<Task> {

    @Override
    public int compare(Task firstTask, Task secondTask) {
        if (Objects.isNull(firstTask) || Objects.isNull(secondTask)) {
            return 1;
        }
        if (Objects.isNull(firstTask.getTitle()) ? Objects.nonNull(secondTask.getTitle())
                : !firstTask.getTitle().equals(secondTask.getTitle())) {
            return 1;
        }
        if (Objects.isNull(firstTask.getOrdering()) ? Objects.nonNull(secondTask.getOrdering())
                : !firstTask.getOrdering().equals(secondTask.getOrdering())) {
            return 1;
        }
        if (firstTask.isTypeAutomatic() != secondTask.isTypeAutomatic()) {
            return 1;
        }
        if (firstTask.isTypeMetadata() != secondTask.isTypeMetadata()) {
            return 1;
        }
        if (firstTask.isTypeImagesRead() != secondTask.isTypeImagesRead()) {
            return 1;
        }
        if (firstTask.isTypeImagesWrite() != secondTask.isTypeImagesWrite()) {
            return 1;
        }
        if (firstTask.isTypeExportDMS() != secondTask.isTypeExportDMS()) {
            return 1;
        }
        if (firstTask.isTypeAcceptClose() != secondTask.isTypeAcceptClose()) {
            return 1;
        }
        if (firstTask.isTypeCloseVerify() != secondTask.isTypeCloseVerify()) {
            return 1;
        }
        if (Objects.isNull(firstTask.getScriptPath()) ? Objects.nonNull(secondTask.getScriptPath())
                : !firstTask.getScriptPath().equals(secondTask.getScriptPath())) {
            return 1;
        }
        if (firstTask.isBatchStep() != secondTask.isBatchStep()) {
            return 1;
        }
        return 0;
    }
}
