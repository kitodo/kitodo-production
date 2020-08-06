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

import java.util.Objects;

import org.kitodo.data.database.beans.Task;

public class TaskComparer {

    public boolean isEqual(Task firstTask, Task secondTask) {
        if (Objects.isNull(firstTask) || Objects.isNull(secondTask)) {
            return false;
        }
        if (Objects.isNull(firstTask.getTitle()) ? Objects.nonNull(secondTask.getTitle())
                : !firstTask.getTitle().equals(secondTask.getTitle())) {
            return false;
        }
        if (Objects.isNull(firstTask.getOrdering()) ? Objects.nonNull(secondTask.getOrdering())
                : !firstTask.getOrdering().equals(secondTask.getOrdering())) {
            return false;
        }
        if (firstTask.isTypeAutomatic() != secondTask.isTypeAutomatic()) {
            return false;
        }
        if (firstTask.isTypeMetadata() != secondTask.isTypeMetadata()) {
            return false;
        }
        if (firstTask.isTypeImagesRead() != secondTask.isTypeImagesRead()) {
            return false;
        }
        if (firstTask.isTypeImagesWrite() != secondTask.isTypeImagesWrite()) {
            return false;
        }
        if (firstTask.isTypeExportDMS() != secondTask.isTypeExportDMS()) {
            return false;
        }
        if (firstTask.isTypeAcceptClose() != secondTask.isTypeAcceptClose()) {
            return false;
        }
        if (firstTask.isTypeCloseVerify() != secondTask.isTypeCloseVerify()) {
            return false;
        }
        if (Objects.isNull(firstTask.getScriptPath()) ? Objects.nonNull(secondTask.getScriptPath())
                : !firstTask.getScriptPath().equals(secondTask.getScriptPath())) {
            return false;
        }
        if (firstTask.isBatchStep() != secondTask.isBatchStep()) {
            return false;
        }
        return true;
    }

    /**
     * Returns a hash value for which holds that is the same for two tasks if
     * the above comparator returns 0, otherwise different.
     *
     * @param task
     *            task to return hash value for
     * @return hash value
     */
    public static int hashCode(Task task) {
        if (Objects.isNull(task)) {
            return 0;
        }
        int hashCode = Objects.hash(task.getTitle(), task.getOrdering(), task.isTypeAutomatic(), task.isTypeMetadata(),
            task.isTypeImagesRead(), task.isTypeImagesWrite(), task.isTypeExportDMS(), task.isTypeAcceptClose(),
            task.isTypeCloseVerify(), task.getScriptPath(), task.isBatchStep());
        return hashCode;
    }
}
