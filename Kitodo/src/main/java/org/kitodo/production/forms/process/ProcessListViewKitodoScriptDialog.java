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
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.exceptions.FileStructureValidationException;
import org.kitodo.exceptions.InvalidImagesException;
import org.kitodo.exceptions.MediaNotFoundException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.command.KitodoScriptService;
import org.kitodo.utils.Stopwatch;
import org.xml.sax.SAXException;

@Named("ProcessListViewKitodoScriptDialog")
@ViewScoped
public class ProcessListViewKitodoScriptDialog implements Serializable {

    private static final Logger logger = LogManager.getLogger(ProcessListViewKitodoScriptDialog.class);
    
    private String kitodoScriptSelection;

    @Inject
    private ProcessListView processListView;

    /**
     * Execute Kitodo script for selected processes.
     */
    public void executeKitodoScriptSelection() {
        Stopwatch stopwatch = new Stopwatch(this, "executeKitodoScriptSelection");
        executeKitodoScriptForProcesses(processListView.getSelectedProcesses(), this.kitodoScriptSelection);
        // Clear selection if deleteProcess was executed
        if (Objects.nonNull(kitodoScriptSelection) && kitodoScriptSelection.startsWith("action:deleteProcess")) {
            processListView.clearSelectedProcesses();
        }
        stopwatch.stop();
    }

    private void executeKitodoScriptForProcesses(List<Process> processes, String kitodoScript) {
        KitodoScriptService service = ServiceManager.getKitodoScriptService();
        try {
            service.execute(processes, kitodoScript);
        } catch (DAOException | IOException | InvalidImagesException | SAXException | FileStructureValidationException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        } catch (MediaNotFoundException e) {
            Helper.setWarnMessage(e.getMessage());
        }
    }

    /**
     * Get kitodo script for selected results.
     *
     * @return kitodo script for selected results
     */
    public String getKitodoScriptSelection() {
        Stopwatch stopwatch = new Stopwatch(this, "getKitodoScriptSelection");
        return stopwatch.stop(this.kitodoScriptSelection);
    }

    /**
     * Set kitodo script for selected results.
     *
     * @param kitodoScriptSelection
     *            the kitodoScript
     */
    public void setKitodoScriptSelection(String kitodoScriptSelection) {
        Stopwatch stopwatch = new Stopwatch(this, "setKitodoScriptSelection", "kitodoScriptSelection",
                kitodoScriptSelection);
        this.kitodoScriptSelection = kitodoScriptSelection;
        stopwatch.stop();
    }

}
