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

package org.kitodo.production.forms;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.BaseBean;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.base.SearchDatabaseService;

class TemplateBaseForm extends ProcessListBaseView {

    private static final Logger logger = LogManager.getLogger(TemplateBaseForm.class);

    void saveTask(Task task, BaseBean baseBean, String message, SearchDatabaseService searchDatabaseService) {
        try {
            ServiceManager.getTaskService().save(task, true);
            ServiceManager.getTaskService().evict(task);
        } catch (DataException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.TASK.getTranslationSingular() }, logger, e);
        }
    }
}
