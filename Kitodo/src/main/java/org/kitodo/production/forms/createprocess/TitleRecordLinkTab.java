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

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Locale.LanguageRange;
import java.util.Objects;
import java.util.Optional;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataeditor.rulesetmanagement.FunctionalMetadata;
import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.metadata.MetadataEditor;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ImportService;
import org.kitodo.production.services.data.ProcessService;
import org.kitodo.production.services.dataformat.MetsService;
import org.omnifaces.util.Ajax;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

/**
 * Backing bean for the title record link tab.
 */
public class TitleRecordLinkTab {
    private static final Logger logger = LogManager.getLogger(TitleRecordLinkTab.class);

    static final String INSERTION_TREE = "editForm:processFromTemplateTabView:insertionTree";
    static final String PARENT_PROCESS_SELECTION = "editForm:processFromTemplateTabView:chooseParentGroup";
    private static final MetsService metsService = ServiceManager.getMetsService();
    private static final ProcessService processService = ServiceManager.getProcessService();

    /**
     * Maximum number of search hits to show.
     */
    private static final int MAXIMUM_NUMBER_OF_HITS = 10;

    private final CreateProcessForm createProcessForm;

    /**
     * The user-selected parent process.
     */
    private String chosenParentProcess = null;

    /**
     * Specifies whether an indication of further hits is visible.
     */
    private boolean indicationOfMoreHitsVisible = false;

    /**
     * Elements from which a parent process can be selected.
     */
    private List<SelectItem> possibleParentProcesses = Collections.emptyList();

    /**
     * Tree with the logical structure of the workpiece and elements indicating the
     * possible insertion positions.
     */
    private TreeNode logicalStructure = new DefaultTreeNode();

    /**
     * The user’s search for parent processes.
     */
    private String searchQuery = "";

    /**
     * The list of possible insertion positions.
     */
    private List<SelectItem> selectableInsertionPositions = Collections.emptyList();

    /**
     * The user-selected insertion position.
     */
    private String selectedInsertionPosition = null;

    /**
     * Process selected as parent.
     */
    private Process titleRecordProcess = null;

    /**
     * Creates a new data object underlying the title record link tab.
     *
     * @param createProcessForm
     *            CreateProcessForm containing the object
     */
    public TitleRecordLinkTab(CreateProcessForm createProcessForm) {
        this.createProcessForm = createProcessForm;
    }

    /**
     * Selects a parent process and builds the tree with the logical structure of the
     * selected process and the possible insertion positions.
     */
    public void chooseParentProcess() {
        if (StringUtils.isBlank(chosenParentProcess)) {
            logicalStructure = new DefaultTreeNode();
            titleRecordProcess = null;
        } else {
            try {
                titleRecordProcess = ServiceManager.getProcessService().getById(Integer.valueOf(chosenParentProcess));
                createInsertionPositionSelectionTree();
            } catch (DAOException | IOException e) {
                Helper.setErrorMessage("errorLoadingOne",
                        new Object[] {possibleParentProcesses.parallelStream()
                                .filter(selectItem -> selectItem.getValue().equals(chosenParentProcess)).findAny()
                                .orElse(new SelectItem(null, null)).getLabel(), chosenParentProcess },
                        logger, e);
            }
        }
    }

    /**
     * Sets up the variables for the tree for the insertion position selection.
     *
     * @throws IOException
     *             if the METS file cannot be read
     */
    public void createInsertionPositionSelectionTree() throws DAOException, IOException {
        if (Objects.isNull(titleRecordProcess)) {
            return;
        }
        URI uri = ServiceManager.getProcessService().getMetadataFileUri(titleRecordProcess);
        Workpiece workpiece = metsService.loadWorkpiece(uri);

        RulesetManagementInterface ruleset = ServiceManager.getRulesetService()
                .openRuleset(titleRecordProcess.getRuleset());
        String metadataLanguage = ServiceManager.getUserService().getCurrentUser().getMetadataLanguage();
        List<LanguageRange> priorityList = LanguageRange.parse(metadataLanguage.isEmpty() ? "en" : metadataLanguage);

        selectableInsertionPositions = new LinkedList<>();
        logicalStructure = new DefaultTreeNode();
        createInsertionPositionSelectionTreeRecursive("", workpiece.getLogicalStructure(), logicalStructure, ruleset,
            priorityList);
        logicalStructure.setExpanded(true);

        if (selectableInsertionPositions.isEmpty()) {
            selectedInsertionPosition = null;
            Helper.setErrorMessage("createProcessForm.titleRecordLinkTab.noInsertionPosition");
        } else {
            selectedInsertionPosition = (String) ((LinkedList<SelectItem>) selectableInsertionPositions).getLast()
                    .getValue();
        }
    }

    /**
     * Recursively builds the tree for the insertion position selection.
     *
     * @param positionPrefix
     *            A string with comma-delimited specification of the levels that
     *            have already been traversed. Initially empty.
     * @param currentLogicalDivision
     *            Logical division for whom the tree is being created. Initially
     *            the logical structure of the workpiece.
     * @param parentNode
     *            Parent node of the tree structure to add to. This is
     *            initialized with a {@link DefaultTreeNode} which is not
     *            displayed
     * @param ruleset
     *            the current ruleset
     * @param priorityList
     *            the user’s metadata language priority list
     */
    private void createInsertionPositionSelectionTreeRecursive(String positionPrefix,
            LogicalDivision currentLogicalDivision, TreeNode parentNode,
            RulesetManagementInterface ruleset, List<LanguageRange> priorityList) throws IOException, DAOException {

        String type;
        List<String> tooltip = Collections.emptyList();
        if (Objects.isNull(currentLogicalDivision.getLink())) {
            type = currentLogicalDivision.getType();
        } else {
            ProcessService processService = ServiceManager.getProcessService();
            int processIdFromUri = processService.processIdFromUri(currentLogicalDivision.getLink().getUri());
            Process linkedProcess = processService.getById(processIdFromUri);
            type = processService.getBaseType(processIdFromUri);
            tooltip = getToolTip(ruleset, linkedProcess);
        }

        StructuralElementViewInterface currentStructuralElementView = ruleset.getStructuralElementView(type,
            createProcessForm.getAcquisitionStage(), priorityList);

        TreeNode logicalDivisionNode = new InsertionPositionSelectionTreeNode(parentNode,
                currentStructuralElementView.getLabel(), tooltip);

        boolean linkingAllowedHere = Objects.isNull(currentLogicalDivision.getLink())
                && currentStructuralElementView.getAllowedSubstructuralElements()
                        .containsKey(createProcessForm.getProcessDataTab().getDocType());

        if (linkingAllowedHere) {
            new InsertionPositionSelectionTreeNode(logicalDivisionNode, selectableInsertionPositions.size());
            selectableInsertionPositions.add(new SelectItem(positionPrefix.concat("0"), null));
        }

        List<LogicalDivision> children = currentLogicalDivision.getChildren();
        for (int index = 0; index < children.size(); index++) {

            createInsertionPositionSelectionTreeRecursive(
                positionPrefix + index + 1 + MetadataEditor.INSERTION_POSITION_SEPARATOR, children.get(index),
                logicalDivisionNode, ruleset, priorityList);

            if (linkingAllowedHere) {
                new InsertionPositionSelectionTreeNode(logicalDivisionNode,
                        selectableInsertionPositions.size());
                selectableInsertionPositions.add(new SelectItem(positionPrefix + (index + 1), null));
            }
        }
    }

    /**
     * Determines the overlay text for a node of the tree.
     *
     * @param ruleset
     *            Ruleset of the process
     * @param linkedProcess
     *            Linked child process
     * @return text to be displayed
     * @throws IOException
     *             if the METS file cannot be read
     */
    private List<String> getToolTip(RulesetManagementInterface ruleset, Process linkedProcess) throws IOException {

        Collection<String> summaryKeys = ruleset.getFunctionalKeys(FunctionalMetadata.DISPLAY_SUMMARY);
        List<String> toolTip = new ArrayList<>();
        if (!summaryKeys.isEmpty()) {

            Workpiece workpiece = metsService.loadWorkpiece(processService.getMetadataFileUri(linkedProcess));
            LogicalDivision logicalStructure = workpiece.getLogicalStructure();

            final String metadataLanguage = ServiceManager.getUserService().getCurrentUser().getMetadataLanguage();
            List<LanguageRange> priorityList = Locale.LanguageRange.parse(metadataLanguage);

            for (String key : summaryKeys) {
                String value = MetadataEditor.getMetadataValue(logicalStructure, key);

                if (Objects.nonNull(value)) {
                    Optional<String> label = ruleset.getTranslationForKey(key, priorityList);
                    toolTip.add(label.orElse(key) + ": " + value);
                }
            }
        }

        if (toolTip.isEmpty()) {
            toolTip.add(linkedProcess.toString());
        }

        return toolTip;
    }


    /**
     * Returns the HTML identifier of the selected parent process.
     *
     * @return the identifier of the selected parent process
     */
    public String getChosenParentProcess() {
        return chosenParentProcess;
    }

    /**
     * Sets the HTML identifier of the selected parent process.
     *
     * @param chosenParentProcess
     *            identifier to set
     */
    public void setChosenParentProcess(String chosenParentProcess) {
        this.chosenParentProcess = chosenParentProcess;
    }

    /**
     * Search for possible parent processes and fill with the result the hit
     * selection. If there are more than the maximum number of hits defined as a
     * constant above, the corresponding message is displayed.
     */
    public void searchForParentProcesses() {
        logicalStructure = new DefaultTreeNode();
        selectableInsertionPositions = Collections.emptyList();
        selectedInsertionPosition = null;
        if (searchQuery.trim().isEmpty()) {
            Helper.setMessage("createProcessForm.titleRecordLinkTab.searchButtonClick.empty");
            return;
        }
        try {
            List<Process> processes = ServiceManager.getProcessService().findLinkableParentProcesses(searchQuery,
                    createProcessForm.getTemplate().getRuleset().getId());
            if (processes.isEmpty()) {
                Helper.setMessage("createProcessForm.titleRecordLinkTab.searchButtonClick.noHits");
            }
            indicationOfMoreHitsVisible = processes.size() > MAXIMUM_NUMBER_OF_HITS;
            possibleParentProcesses = ServiceManager.getImportService()
                    .getPotentialParentProcesses(processes, MAXIMUM_NUMBER_OF_HITS);
        } catch (DAOException | IOException e) {
            Helper.setErrorMessage("createProcessForm.titleRecordLinkTab.searchButtonClick.error", e.getMessage(),
                    logger, e);
            indicationOfMoreHitsVisible = false;
            possibleParentProcesses = Collections.emptyList();
        }
        possibleParentProcesses.sort(Comparator.comparing(SelectItem::getLabel));
        for (SelectItem selectItem : possibleParentProcesses) {
            if (!selectItem.isDisabled()) {
                try {
                    int processId = Integer.parseInt(selectItem.getValue().toString());
                    setParentAsTitleRecord(ServiceManager.getProcessService().getById(processId));
                    break;
                } catch (DAOException | NumberFormatException e) {
                    logger.error(e);
                }
            }
        }
    }

    /**
     * Returns the search query typed by the user.
     *
     * @return the search query
     */
    public String getSearchQuery() {
        return searchQuery;
    }

    /**
     * Sets the search query typed by the user.
     *
     * @param searchQuery
     *            search query to set
     */
    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    /**
     * Returns the HTML identifier of the selected insertion position.
     *
     * @return the selected insertion position
     */
    public String getSelectedInsertionPosition() {
        return selectedInsertionPosition;
    }

    /**
     * Sets the HTML identifier of the selected insertion position.
     *
     * @param selectedInsertionPosition
     *            identifier to set
     */
    public void setSelectedInsertionPosition(String selectedInsertionPosition) {
        this.selectedInsertionPosition = selectedInsertionPosition;
    }

    /**
     * Returns whether the hint that there were more hits is visible.
     *
     * @return whether the hint is visible
     */
    public boolean isIndicationOfMoreHitsVisible() {
        return indicationOfMoreHitsVisible;
    }

    /**
     * Returns the list of selectors for selecting a parent process.
     *
     * @return the list of selectors
     */
    public List<SelectItem> getPossibleParentProcesses() {
        return possibleParentProcesses;
    }

    /**
     * Set possibleParentProcesses.
     *
     * @param possibleParentProcesses as list
     */
    public void setPossibleParentProcesses(List<SelectItem> possibleParentProcesses) {
        this.possibleParentProcesses = possibleParentProcesses;
    }

    /**
     * Returns the tree containing the logical structure of the selected parent
     * process and the possible insert positions.
     *
     * @return the tree structure for selecting the insertion position
     */
    public TreeNode getLogicalStructure() {
        return logicalStructure;
    }

    /**
     * Returns the list of selection items for selecting an insertion position.
     *
     * @return selection elements for selecting an insertion position
     */
    public List<SelectItem> getSelectableInsertionPositions() {
        return selectableInsertionPositions;
    }

    /**
     * Returns the process of the selected title record.
     *
     * @return the process of the selected title record
     */
    public Process getTitleRecordProcess() {
        return titleRecordProcess;
    }

    /**
     * Sets the process of the selected title record.
     *
     * @param titleRecordProcess the process of the selected title record
     */
    public void setTitleRecordProcess(Process titleRecordProcess) {
        this.titleRecordProcess = titleRecordProcess;
    }

    /**
     * Set given process "parentProcess" as parent title record of new process.
     *
     * @param parentProcess process to set as parent title record
     */
    public void setParentAsTitleRecord(Process parentProcess) {
        createProcessForm.setEditActiveTabIndex(CreateProcessForm.TITLE_RECORD_LINK_TAB_INDEX);
        try {
            if (ImportService.userMayLinkToParent(parentProcess.getId())) {
                setChosenParentProcess(String.valueOf(parentProcess.getId()));
            } else {
                setChosenParentProcess(null);
            }
        } catch (DAOException e) {
            Helper.setErrorMessage(e);
        }
        chooseParentProcess();
        // only update UI components if FacesContext exists (not the case during integration tests, for example)
        if (Objects.nonNull(FacesContext.getCurrentInstance())) {
            Ajax.update(PARENT_PROCESS_SELECTION);
            Ajax.update(INSERTION_TREE);
        }
    }
}
