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

package org.kitodo.production.forms.process;

import java.text.MessageFormat;
import java.util.Objects;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.forms.ValidatableForm;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ProcessService;
import org.kitodo.utils.Stopwatch;


@Named("ProcessEditView")
@ViewScoped
public class ProcessEditView extends ValidatableForm {
    
    public static final String VIEW_PATH = MessageFormat.format(REDIRECT_PATH, "processEdit");

    private Process process;

    private static final Logger logger = LogManager.getLogger(ProcessEditView.class);
    private final transient ProcessService processService = ServiceManager.getProcessService();

    private String processEditReferer = DEFAULT_LINK;

    @Inject
    private ProcessEditViewDetailsTab detailsTab;

    @Inject
    private ProcessEditViewTasksTab tasksTab;

    @Inject
    private ProcessEditViewTemplatesTab templatesTab;

    @Inject
    private ProcessEditViewWorkpiecesTab workpiecesTab;

    @Inject
    private ProcessEditViewPropertiesTab propertiesTab;

    /**
     * Initialize UserEditView.
     */
    @PostConstruct
    public void init() {
        this.process = new Process();
    }

    /**
     * Return the process currently being edited.
     * 
     * @return the process currently being edited
     */
    public Process getProcess() {
        return this.process;
    }

    /**
     * Get process edit page referring view.
     *
     * @return process edit page referring view
     */
    public String getProcessEditReferer() {
        return this.processEditReferer;
    }

    /**
     * Method being used as viewAction for user edit form.
     *
     * @param id
     *            ID of the user to load
     */
    public void load(int id) {
        // reset when user is loaded
        try {
            if (!Objects.equals(id, 0)) {
                this.process = processService.getById(id);
            }
            setSaveDisabled(true);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.PROCESS.getTranslationSingular(), id }, logger, e);
        }

        detailsTab.load(this.process);
        tasksTab.load(this.process);
        templatesTab.load(this.process);
        workpiecesTab.load(this.process);
        propertiesTab.load(this.process);
    }

    /**
     * Save the currently edited process.
     *
     * @return page or empty String
     */
    public String save() {
        if (!detailsTab.save() || !tasksTab.save() || !templatesTab.save() || !workpiecesTab.save() || !propertiesTab.save()) {
            return this.stayOnCurrentPage;
        }

        try {
            processService.save(this.process);     
        } catch (DAOException | RuntimeException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.PROCESS.getTranslationSingular() }, logger, e);
            return this.stayOnCurrentPage;
        }

        return ProcessListView.getViewPath() + "&" + getReferrerListOptions();
    }

    /**
     * Set referring view which will be returned when the user clicks "save" or
     * "cancel" on the process edit page.
     *
     * @param referer
     *            the referring view
     */
    public void setProcessEditReferer(String referer) {
        Stopwatch stopwatch = new Stopwatch(this, "setProcessEditReferer", "referer", referer);
        if (!referer.isEmpty()) {
            if ("processes".equals(referer)) {
                this.processEditReferer = ProcessListView.getViewPath() + "&" + getReferrerListOptions() ;
            } else if (!referer.contains("taskEdit") || this.processEditReferer.isEmpty()) {
                this.processEditReferer = DEFAULT_LINK;
            }
        }
        stopwatch.stop();
    }
}
