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

package org.kitodo.production.forms.createprocess;

import com.sun.jersey.api.NotFoundException;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.Metadata;
import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.enums.TaskEditType;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.exceptions.InvalidMetadataValueException;
import org.kitodo.exceptions.ProcessGenerationException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.forms.BaseForm;
import org.kitodo.production.forms.dataeditor.DataEditorForm;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.interfaces.RulesetSetupInterface;
import org.kitodo.production.metadata.MetadataEditor;
import org.kitodo.production.process.ProcessGenerator;
import org.kitodo.production.process.ProcessValidator;
import org.kitodo.production.services.ServiceManager;

@Named("CreateProcessForm")
@ViewScoped
public class CreateProcessForm extends BaseForm implements RulesetSetupInterface {

    private static final Logger logger = LogManager.getLogger(CreateProcessForm.class);

    private final ImportTab importTab = new ImportTab(this);
    private final ProcessDataTab processDataTab = new ProcessDataTab(this);
    private final ProcessMetadataTab processMetadataTab = new ProcessMetadataTab(this);
    private final SearchTab searchTab = new SearchTab(this);
    private final TitleRecordLinkTab titleRecordLinkTab = new TitleRecordLinkTab(this);

    private RulesetManagementInterface rulesetManagementInterface;
    private List<Locale.LanguageRange> priorityList;
    private String acquisitionStage = "create";
    private Project project;
    private Workpiece workpiece = new Workpiece();
    private Template template;
    private LinkedList<Process> processes = new LinkedList<>(Collections.singletonList(new Process()));
    private final String processListPath = MessageFormat.format(REDIRECT_PATH, "processes");

    /**
     * Returns the ruleset management to access the ruleset.
     *
     * @return the ruleset
     */
    @Override
    public RulesetManagementInterface getRuleset() {
        return rulesetManagementInterface;
    }

    /**
     * Update ruleset and docType.
     * @param rulesetFileName as String
     */
    public void updateRulesetAndDocType(String rulesetFileName) {
        setRulesetManagementInterface(rulesetFileName);
        processDataTab.setAllDocTypes(getAllRulesetDivisions());
    }

    private void setRulesetManagementInterface(String rulesetFileName) {
        try {
            this.rulesetManagementInterface = openRulesetFile(rulesetFileName);
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
        }
    }

    private List<SelectItem> getAllRulesetDivisions() {
        List<SelectItem> allDocTypes = rulesetManagementInterface
                .getStructuralElements(priorityList).entrySet()
                .stream().map(entry -> new SelectItem(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        if (allDocTypes.isEmpty()) {
            Helper.setErrorMessage("errorLoadingDocTypes");
        }
        return allDocTypes;
    }

    /**
     * Returns the current acquisition stage to adapt the displaying of fields
     * accordingly.
     *
     * @return the current acquisition stage
     */
    @Override
    public String getAcquisitionStage() {
        return acquisitionStage;
    }

    /**
     * Returns the language preference list of the editing user to display
     * labels and options in the user-preferred language.
     *
     * @return the language preference list
     */
    @Override
    public List<Locale.LanguageRange> getPriorityList() {
        return priorityList;
    }

    /**
     * Get importTab.
     *
     * @return value of importTab
     */
    public ImportTab getImportTab() {
        return importTab;
    }

    /**
     * Get processDataTab.
     *
     * @return value of processDataTab
     */
    public ProcessDataTab getProcessDataTab() {
        return processDataTab;
    }

    /**
     * Get processMetadataTab.
     *
     * @return value of processMetadataTab
     */
    public ProcessMetadataTab getProcessMetadataTab() {
        return processMetadataTab;
    }

    /**
     * Get searchTab.
     *
     * @return value of searchTab
     */
    public SearchTab getSearchTab() {
        return searchTab;
    }

    /**
     * Get titleRecordLinkTab.
     *
     * @return value of titleRecordLinkTab
     */
    public TitleRecordLinkTab getTitleRecordLinkTab() {
        return titleRecordLinkTab;
    }

    /**
     * Get newProcesses.
     *
     * @return value of newProcesses
     */
    public List<Process> getProcesses() {
        return processes;
    }

    /**
     * Set newProcesses.
     *
     * @param processes as java.util.List of Process
     */
    public void setProcesses(LinkedList<Process> processes) {
        this.processes = processes;
    }

    /**
     * Get the main Process that want to be created.
     *
     * @return value of first element in newProcesses
     */
    public Process getMainProcess() {
        if (processes.isEmpty()) {
            throw new NotFoundException("Process list is empty!");
        }
        return processes.get(0);
    }

    /**
     * Get template.
     *
     * @return value of template
     */
    public Template getTemplate() {
        return template;
    }

    /**
     * Set template.
     *
     * @param template as org.kitodo.data.database.beans.Template
     */
    public void setTemplate(Template template) {
        this.template = template;
    }

    /**
     * Get project.
     *
     * @return value of project
     */
    public Project getProject() {
        return project;
    }

    /**
     * Get workpiece.
     *
     * @return value of workpiece
     */
    public Workpiece getWorkpiece() {
        return workpiece;
    }

    /**
     * Set project.
     *
     * @param project as org.kitodo.data.database.beans.Project
     */
    public void setProject(Project project) {
        this.project = project;
    }

    /**
     * Create the process and save the metadata.
     */
    public String createNewProcess() {
        if (Objects.nonNull(titleRecordLinkTab.getTitleRecordProcess())) {
            if (Objects.isNull(titleRecordLinkTab.getSelectedInsertionPosition())
                    || titleRecordLinkTab.getSelectedInsertionPosition().isEmpty()) {
                FacesContext.getCurrentInstance().validationFailed();
                Helper.setErrorMessage("prozesskopieForm.createNewProcess.noInsertionPositionSelected");
                return stayOnCurrentPage;
            } else {
                User titleRecordOpenUser = DataEditorForm
                        .getUserOpened(titleRecordLinkTab.getTitleRecordProcess().getId());
                if (Objects.nonNull(titleRecordOpenUser)) {
                    FacesContext.getCurrentInstance().validationFailed();
                    Helper.setErrorMessage("prozesskopieForm.createNewProcess.titleRecordOpen",
                            titleRecordOpenUser.getFullName());
                    return stayOnCurrentPage;
                }
            }
        }
        if (createProcess()) {
            Process mainProcess = getMainProcess();
            if (Objects.nonNull(titleRecordLinkTab.getTitleRecordProcess())) {
                ServiceManager.getProcessService().refresh(mainProcess);
                try {
                    MetadataEditor.addLink(titleRecordLinkTab.getTitleRecordProcess(),
                            titleRecordLinkTab.getSelectedInsertionPosition(), mainProcess.getId());

                } catch (IOException exception) {
                    Helper.setErrorMessage("errorSaving", titleRecordLinkTab.getTitleRecordProcess().getTitle(), logger,
                            exception);
                }
            }
            return processListPath;
        }
        return this.stayOnCurrentPage;
    }

    /**
     * Prepare template and project for which new process will be created.
     *
     * @param templateId
     *            id of template to query from database
     * @param projectId
     *            id of project to query from database
     * @param referringView
     *            JSF page the user came from
     *
     * @return path to page with form
     */
    public String prepare(int templateId, int projectId, String referringView) {
        if (prepareProcess(templateId, projectId)) {
            return stayOnCurrentPage;
        }
        return MessageFormat.format(REDIRECT_PATH, referringView);
    }

    /**
     * Prepare new process which will be created.
     *
     * @param templateId
     *            id of template to query from database
     * @param projectId
     *            id of project to query from database
     *
     * @return true if process was prepared, otherwise false
     */
    private boolean prepareProcess(int templateId, int projectId) {
        ProcessGenerator processGenerator = new ProcessGenerator();
        try {
            boolean generated = processGenerator.generateProcess(templateId, projectId);
            if (generated) {
                processes = new LinkedList<>(Collections.singletonList(processGenerator.getGeneratedProcess()));
                project = processGenerator.getProject();
                template = processGenerator.getTemplate();
                updateRulesetAndDocType(getMainProcess().getRuleset().getFile());
                processDataTab.prepare();
                return true;
            }
        } catch (ProcessGenerationException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        return false;
    }

    /**
     * Create process.
     *
     * @return true if process was created, otherwise false
     */
    public boolean createProcess() {
        Process mainProcess = getMainProcess();
        if (!ProcessValidator.isContentValid(mainProcess.getTitle(),
                processMetadataTab.getProcessDetailsElements(),
                true)) {
            return false;
        }
        addProperties();
        updateTasks(mainProcess);
        try {
            mainProcess.setSortHelperImages(processDataTab.getGuessedImages());
            ServiceManager.getProcessService().save(mainProcess);
        } catch (DataException e) {
            Helper.setErrorMessage("errorCreating", new Object[] {ObjectType.PROCESS.getTranslationSingular() }, logger,
                    e);
            return false;
        }
        if (!createProcessLocation()) {
            return false;
        }

        if (Objects.nonNull(workpiece)) {
            workpiece.getRootElement().setType(processDataTab.getDocType());
            processMetadataTab.preserve();
            try (OutputStream out = ServiceManager.getFileService()
                    .write(ServiceManager.getProcessService().getMetadataFileUri(getMainProcess()))) {
                ServiceManager.getMetsService().save(workpiece, out);
            } catch (IOException e) {
                Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            }
        }

        if (Objects.nonNull(titleRecordLinkTab.getTitleRecordProcess())) {
            getMainProcess().setParent(titleRecordLinkTab.getTitleRecordProcess());
            titleRecordLinkTab.getTitleRecordProcess().getChildren().add(getMainProcess());
        }

        try {
            ServiceManager.getProcessService().save(mainProcess);
        } catch (DataException e) {
            Helper.setErrorMessage("errorCreating", new Object[] {ObjectType.PROCESS.getTranslationSingular() }, logger,
                    e);
            return false;
        }
        return true;
    }

    private void addProperties() {
        Process mainProcess = getMainProcess();
        addMetadataProperties(processMetadataTab.getProcessDetailsElements(), mainProcess);
        ProcessGenerator.addPropertyForWorkpiece(mainProcess, "DocType", processDataTab.getDocType());
        ProcessGenerator.addPropertyForWorkpiece(mainProcess, "TifHeaderImagedescription",
                processDataTab.getTiffHeaderImageDescription());
        ProcessGenerator.addPropertyForWorkpiece(mainProcess, "TifHeaderDocumentname",
                processDataTab.getTiffHeaderDocumentName());
        if (Objects.nonNull(template)) {
            ProcessGenerator.addPropertyForProcess(mainProcess, "Template", template.getTitle());
            ProcessGenerator.addPropertyForProcess(mainProcess, "TemplateID", String.valueOf(template.getId()));
        }
    }

    private void addMetadataProperties(List<ProcessDetail> processDetailList, Process process) {
        try {
            for (ProcessDetail processDetail : processDetailList) {
                if (!processDetail.getMetadata().isEmpty() && processDetail.getMetadata().toArray()[0] instanceof Metadata) {
                    String metadataValue = ProcessMetadataTab.getProcessDetailValue(processDetail);
                    Metadata metadata = (Metadata) processDetail.getMetadata().toArray()[0];
                    switch (metadata.getDomain()) {
                        case DMD_SEC:
                            ProcessGenerator.addPropertyForWorkpiece(process, processDetail.getLabel(), metadataValue);
                            break;
                        case SOURCE_MD:
                            ProcessGenerator.addPropertyForTemplate(process, processDetail.getLabel(), metadataValue);
                            break;
                        case TECH_MD:
                            ProcessGenerator.addPropertyForProcess(process, processDetail.getLabel(), metadataValue);
                            break;
                        default:
                            logger.info("Don't save metadata '" + processDetail.getMetadataID() + "' with domain '"
                                    + metadata.getDomain() + "' to property.");
                            break;
                    }
                }
            }
        } catch (InvalidMetadataValueException e) {
            logger.error(e.getLocalizedMessage());
        }
    }

    private void updateTasks(Process process) {
        for (Task task : process.getTasks()) {
            task.setProcessingTime(process.getCreationDate());
            task.setEditType(TaskEditType.AUTOMATIC);
            if (task.getProcessingStatus() == TaskStatus.DONE) {
                task.setProcessingBegin(process.getCreationDate());
                Date date = new Date();
                task.setProcessingTime(date);
                task.setProcessingEnd(date);
            }
        }
    }

    private boolean createProcessLocation() {
        Process mainProcess = getMainProcess();
        try {
            URI processBaseUri = ServiceManager.getFileService().createProcessLocation(mainProcess);
            mainProcess.setProcessBaseUri(processBaseUri);
            return true;
        } catch (IOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            try {
                ServiceManager.getProcessService().remove(mainProcess);
            } catch (DataException ex) {
                Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            }
            return false;
        }
    }

    private RulesetManagementInterface openRulesetFile(String fileName) throws IOException {
        final long begin = System.nanoTime();
        String metadataLanguage = ServiceManager.getUserService().getCurrentUser().getMetadataLanguage();
        priorityList = Locale.LanguageRange.parse(metadataLanguage.isEmpty() ? "en" : metadataLanguage);
        RulesetManagementInterface ruleset = ServiceManager.getRulesetManagementService().getRulesetManagement();
        ruleset.load(new File(Paths.get(ConfigCore.getParameter(ParameterCore.DIR_RULESETS), fileName).toString()));
        if (logger.isTraceEnabled()) {
            logger.trace("Reading ruleset took {} ms", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - begin));
        }
        return ruleset;
    }

    /**
     * Initialize the list of created processes.
     */
    public void initializeProcesses() {
        try {
            ProcessGenerator processGenerator = new ProcessGenerator();
            processGenerator.generateProcess(template.getId(), project.getId());
            this.processes = new LinkedList<>(Collections.singletonList(processGenerator.getGeneratedProcess()));
        } catch (ProcessGenerationException e) {
            logger.error(e.getLocalizedMessage());
        }
        this.processMetadataTab.initializeProcessDetails(workpiece.getRootElement());
    }
}
