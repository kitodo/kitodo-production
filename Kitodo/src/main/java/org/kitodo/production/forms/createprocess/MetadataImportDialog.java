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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.MdSec;
import org.kitodo.api.Metadata;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.TempProcess;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ImportService;
import org.omnifaces.util.Ajax;
import org.primefaces.PrimeFaces;

public abstract class MetadataImportDialog {

    private static final Logger logger = LogManager.getLogger(MetadataImportDialog.class);
    public final CreateProcessForm createProcessForm;

    static final String FORM_CLIENTID = "editForm";
    static final String GROWL_MESSAGE =
            "PF('notifications').renderMessage({'summary':'SUMMARY','detail':'DETAIL','severity':'SEVERITY'});";

    /**
     * Standard constructor.
     *
     * @param createProcessForm CreateProcessForm instance to which this ImportDialog is assigned.
     */
    MetadataImportDialog(CreateProcessForm createProcessForm) {
        this.createProcessForm = createProcessForm;
    }

    void showRecord() {
        Ajax.update(FORM_CLIENTID);

        // if fewer processes are imported than configured in the frontend, it can mean that
        // - the OPAC does not have as many processes in the hierarchy or
        // - one process of the hierarchy was already in the DB and import ended at this point
        int numberOfProcesses = this.createProcessForm.getProcesses().size();

        if (numberOfProcesses < 1) {
            Helper.setErrorMessage("Error: list of processes is empty!");
            return;
        }

        TempProcess parentTempProcess = ServiceManager.getImportService().getParentTempProcess();
        if (numberOfProcesses == 1 && Objects.nonNull(parentTempProcess)) {
            // case 1: only one process was imported => load DB parent into "TitleRecordLinkTab"
            this.createProcessForm.getTitleRecordLinkTab().setParentAsTitleRecord(parentTempProcess.getProcess());
        } else {
            // case 2: multiple processes imported and one ancestor found in DB => add ancestor to list
            if (Objects.nonNull(parentTempProcess)) {
                this.createProcessForm.getProcesses().add(parentTempProcess);
            }
            this.createProcessForm.setEditActiveTabIndex(CreateProcessForm.ADDITIONAL_FIELDS_TAB_INDEX);
        }
    }

    /**
     * Show growl message.
     *
     * @param summary General message
     * @param detail Message detail
     */
    public void showGrowlMessage(String summary, String detail) {
        String script = GROWL_MESSAGE.replace("SUMMARY", summary).replace("DETAIL", detail)
                .replace("SEVERITY", "info");
        PrimeFaces.current().executeScript(script);
    }

    /**
     * Get list of catalogs.
     *
     * @return list of catalogs
     */
    public List<String> getCatalogs() {
        try {
            return ServiceManager.getImportService().getAvailableCatalogs();
        } catch (IllegalArgumentException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            return new LinkedList<>();
        }
    }

    /**
     * Fill metadata fields in metadata tab with metadata values of first process in given list "processes"
     * on successful import.
     * @param processes list of TempProcess instances
     */
    void fillCreateProcessForm(LinkedList<TempProcess> processes) {
        this.createProcessForm.setProcesses(processes);
        if (!processes.isEmpty() && processes.getFirst().getMetadataNodes().getLength() > 0) {
            TempProcess firstProcess = processes.getFirst();
            this.createProcessForm.getProcessDataTab()
                    .setDocType(firstProcess.getWorkpiece().getLogicalStructure().getType());
            Collection<Metadata> metadata = ImportService.importMetadata(firstProcess.getMetadataNodes(),
                    MdSec.DMD_SEC);
            createProcessForm.getProcessMetadataTab().getProcessDetails().setMetadata(metadata);
        }
    }
}
