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

package org.kitodo.production.forms.dataeditor;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.metadata.MetadataEditor;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.dataeditor.DataEditorService;
import org.primefaces.PrimeFaces;

public class LinkProcessDialog extends AddNodeDialog {

    private static final Logger logger = LogManager.getLogger(LinkProcessDialog.class);

    private String processNumber = "";
    private List<Process> processes = Collections.emptyList();
    private Process selectedProcess = null;

    LinkProcessDialog(DataEditorForm form) {
        super(form);
    }

    /**
     * Function for the button for the search. Looks for suitable processes. If
     * the process number is a number and the process exists, it is already
     * found. Otherwise it must be searched for, excluding the wrong ruleset or
     * the wrong client.
     */
    public void search() {
        if (processNumber.trim().isEmpty()) {
            alert(Helper.getTranslation("dialogAddDocStrucType.searchButtonClick.empty"));
            return;
        }
        try {
            Set<String> allowedSubstructuralElements = new HashSet<>();
            StructuralElementViewInterface structuralElement = DataEditorService.getStructuralElementView(this.dataEditor);
            if (Objects.nonNull(structuralElement)) {
                allowedSubstructuralElements = structuralElement.getAllowedSubstructuralElements().keySet();
            }
            List<Integer> ids = ServiceManager.getProcessService().findLinkableChildProcesses(processNumber,
                            dataEditor.getProcess().getRuleset().getId(), allowedSubstructuralElements)
                    .stream().map(Process::getId).toList();
            if (ids.isEmpty()) {
                alert(Helper.getTranslation("dialogAddDocStrucType.searchButtonClick.noHits"));
            }
            processes = new LinkedList<>();
            for (int processId : ids) {
                processes.add(ServiceManager.getProcessService().getById(processId));
            }
        } catch (DAOException e) {
            logger.catching(e);
            alert(Helper.getTranslation("dialogAddDocStrucType.searchButtonClick.error", e.getMessage()));
        }
    }

    /**
     * Adds the link when the user clicks OK.
     */
    public void addProcessLink() {
        dataEditor.getCurrentChildren().add(selectedProcess);
        MetadataEditor.addLink(getTargetLogicalDivisionFromNodeSelection().orElseThrow(IllegalStateException::new),
                selectedProcess.getId());
        dataEditor.getStructurePanel().show(true);
        dataEditor.getPaginationPanel().show();
        selectedProcess = null;
        processes = Collections.emptyList();
    }

    public void resetValues() {
        processNumber = "";
        processes = Collections.emptyList();
    }

    /**
     * Returns the process number. The process number is an input field where
     * the user can enter the process number or the process title, and then it
     * is searched for. But search is only when the button is clicked (too much
     * load otherwise).
     *
     * @return the process number
     */
    public String getProcessNumber() {
        return processNumber;
    }

    /**
     * Sets the process number when the user entered it.
     *
     * @param processNumber
     *            process number to set
     */
    public void setProcessNumber(String processNumber) {
        this.processNumber = processNumber;
    }

    /**
     * Returns the list of items to populate the drop-down list to select a
     * process.
     *
     * @return the list of processes
     */
    public List<Process> getProcesses() {
        return processes;
    }

    /**
     * Returns the process selected by the user in the drop-down list.
     *
     * @return the selected process
     */
    public Process getSelectedProcess() {
        return selectedProcess;
    }

    /**
     * Sets the number of the process selected by the user.
     *
     * @param selectedProcess
     *            selected process
     */
    public void setSelectedProcess(Process selectedProcess) {
        this.selectedProcess = selectedProcess;
    }


    /**
     * Displays a dialog box with a message to the user.
     *
     * @param message
     *            message to show
     */
    private void alert(String message) {
        PrimeFaces.current().executeScript("alert('" + message + "');");
    }

}
