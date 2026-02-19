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

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.database.beans.ImportConfiguration;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.forms.BaseTabEditView;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.process.ProcessValidator;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ProcessService;
import org.kitodo.utils.Stopwatch;


@Named("ProcessEditViewDetailsTab")
@ViewScoped
public class ProcessEditViewDetailsTab extends BaseTabEditView<Process> {

    private static final Logger logger = LogManager.getLogger(ProcessEditViewDetailsTab.class);

    private Process process;
    private String newProcessTitle;

    private List<Project> availableProjects;
    private List<ImportConfiguration> availableImportConfigurations;
    private List<Ruleset> availableRulesets;
    private List<Docket> availableDockets;
    private List<Pair<?, ?>> availableOcrdWorkflows;
    private Pair<?, ?> ocrdWorkflow;
    private Pair<?, ?> orcdWorkflowOfTemplate;

    private final transient ProcessService processService = ServiceManager.getProcessService();

    /**
     * Return the process currently being edited.
     * 
     * @return the process currently being edited
     */
    public Process getProcess() {
        return this.process;
    }

    /**
     * Return the new title for a process that was entered by the user.
     * 
     * @return the new process title
     */
    public String getNewProcessTitle() {
        return this.newProcessTitle;
    }

    /**
     * Sets a new process title.
     * 
     * @param newProcessTitle
     *            new process title to set
     */
    public void setNewProcessTitle(String newProcessTitle) {
        this.newProcessTitle = newProcessTitle;
    }

    /**
     * Get the list of all projects.
     *
     * @return list of projects
     */
    public List<Project> getProjects() {
        return availableProjects;
    }

    /**
     * Get the list of all import configurations.
     *
     * @return list of all import configurations.
     */
    public List<ImportConfiguration> getImportConfigurations() {
        return availableImportConfigurations;
    }

    /**
     * Get the list of all rulesets for select list.
     *
     * @return list of rulesets
     */
    public List<Ruleset> getRulesets() {
        return availableRulesets;
    }

    /**
     * Get the list of all dockets for select list.
     *
     * @return list of dockets
     */
    public List<Docket> getDockets() {
        return availableDockets;
    }

    /**
     * Get the OCR-D workflow assigned to this process.
     *
     * @return Immutable key value pair
     */
    public Pair<?, ?> getOcrdWorkflow() {
        return ocrdWorkflow;
    }

    /**
     * Get list of all OCR-D workflows for select list.
     *
     * @return list of OCR-D workflows
     */
    public List<Pair<?, ?>> getOcrdWorkflows() {
        return availableOcrdWorkflows;
    }

    /**
     * Get the OCR-D workflow assigned to the process template.
     *
     * @return Immutable key value pair
     */
    public Pair<?, ?> getOcrdWorkflowOfTemplate() {
        return orcdWorkflowOfTemplate;
    }

    /**
     * Load required data for the details tab for a specific process that is currently being edited.
     * 
     * @param process the process currently being edited
     */
    public void load(Process process) {
        final Stopwatch stopwatch = new Stopwatch(this, "ProcessEditViewDetailsTab.load");

        this.process = process;
        this.newProcessTitle = process.getTitle();
        this.availableProjects = ServiceManager.getProjectService().getAllForSelectedClient();
        try {
            this.availableImportConfigurations = ServiceManager.getImportConfigurationService().getAll();
        } catch (DAOException e) {
            Helper.setErrorMessage(e);
            this.availableImportConfigurations = Collections.emptyList();
        }
        this.availableRulesets = ServiceManager.getRulesetService().getAllForSelectedClient();
        this.availableDockets = ServiceManager.getDocketService().getAllForSelectedClient();
        this.ocrdWorkflow = ServiceManager.getOcrdWorkflowService().getOcrdWorkflow(process.getOcrdWorkflowId());
        this.availableOcrdWorkflows = ServiceManager.getOcrdWorkflowService().getOcrdWorkflows();
        this.orcdWorkflowOfTemplate = ServiceManager.getOcrdWorkflowService().getOcrdWorkflow(process.getTemplate()
                .getOcrdWorkflowId());

        stopwatch.stop();
    }

    /**
     * Save details of a process and report on success to process edit view.
     *
     * @return true if saving can proceed, false if there is a problem
     */
    public boolean save() {
        if (Objects.isNull(newProcessTitle) || newProcessTitle.isEmpty()) {
            Helper.setErrorMessage(ERROR_INCOMPLETE_DATA, "processTitleEmpty");
            return false;
        }

        if (!process.getTitle().equals(newProcessTitle) && !renameAfterProcessTitleChanged()) {
            return false;
        }

        return true;
    }

    /**
     * Set the OCR-D workflow.
     *
     * @param ocrdWorkflow
     *         The immutable key value pair
     */
    public void setOcrdWorkflow(Pair<?, ?> ocrdWorkflow) {
        Stopwatch stopwatch = new Stopwatch(this, "setOcrdWorkflow");
        String ocrdWorkflowId = StringUtils.EMPTY;
        if (Objects.nonNull(ocrdWorkflow)) {
            ocrdWorkflowId = ocrdWorkflow.getKey().toString();
        }
        process.setOcrdWorkflowId(ocrdWorkflowId);
        stopwatch.stop();
    }

    private boolean renameAfterProcessTitleChanged() {
        String validateRegEx = ConfigCore.getParameterOrDefaultValue(ParameterCore.VALIDATE_PROCESS_TITLE_REGEX);
        if (!ProcessValidator.isProcessTitleCorrect(newProcessTitle)) {
            Helper.setErrorMessage("processTitleInvalid", new Object[] {validateRegEx });
            return false;
        } else {
            try {
                processService.renameProcess(this.process, this.newProcessTitle);
            } catch (IOException | RuntimeException e) {
                Helper.setErrorMessage("errorRenaming", new Object[] {Helper.getTranslation("directory") }, logger, e);
            }

            // remove Tiffwriter file
            ServiceManager.getKitodoScriptService().deleteTiffHeaderFile(List.of(process));
        }
        return true;
    }
    
}
