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

package org.kitodo.forms;

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.BaseBean;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.enums.ObjectType;
import org.kitodo.helper.Helper;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.data.base.SearchDatabaseService;

public class TemplateBaseForm extends BaseForm {

    private static final long serialVersionUID = 6566567843176821176L;
    private static final Logger logger = LogManager.getLogger(TemplateBaseForm.class);
    private boolean showInactiveProjects = false;

    /**
     * Check if inactive projects should be shown.
     *
     * @return true or false
     */
    public boolean isShowInactiveProjects() {
        return this.showInactiveProjects;
    }

    /**
     * Set if inactive projects should be shown.
     *
     * @param showInactiveProjects
     *            true or false
     */
    public void setShowInactiveProjects(boolean showInactiveProjects) {
        this.showInactiveProjects = showInactiveProjects;
    }

    void saveTask(Task task, BaseBean baseBean, String message, SearchDatabaseService searchDatabaseService) {
        try {
            ServiceManager.getTaskService().save(task);
            ServiceManager.getTaskService().evict(task);
            reload(baseBean, message, searchDatabaseService);
        } catch (DataException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.TASK.getTranslationSingular() }, logger, e);
        }
    }

    @SuppressWarnings("unchecked")
    void reload(BaseBean baseBean, String message, SearchDatabaseService searchDatabaseService) {
        if (Objects.nonNull(baseBean) && Objects.nonNull(baseBean.getId())) {
            try {
                searchDatabaseService.refresh(baseBean);
            } catch (RuntimeException e) {
                Helper.setErrorMessage(ERROR_RELOADING, new Object[] {message }, logger, e);
            }
        }
    }
}
