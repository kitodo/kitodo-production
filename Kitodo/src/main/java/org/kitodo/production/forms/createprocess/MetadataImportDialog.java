package org.kitodo.production.forms.createprocess;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.schemaconverter.ExemplarRecord;
import org.kitodo.data.database.beans.Process;
import org.kitodo.exceptions.ParameterNotFoundException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.TempProcess;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ImportService;
import org.omnifaces.util.Ajax;
import org.primefaces.PrimeFaces;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import javax.faces.model.SelectItem;

public abstract class MetadataImportDialog {

    private static final Logger logger = LogManager.getLogger(MetadataImportDialog.class);
    public final CreateProcessForm createProcessForm;

    static final int ADDITIONAL_FIELDS_TAB_INDEX = 1;
    static final int TITLE_RECORD_LINK_TAB_INDEX = 3;
    static final String FORM_CLIENTID = "editForm";
    static final String INSERTION_TREE = "editForm:processFromTemplateTabView:insertionTree";
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
            setParentAsTitleRecord(parentTempProcess.getProcess());
        } else {
            // case 2: multiple processes imported and one ancestor found in DB => add ancestor to list
            if (Objects.nonNull(parentTempProcess)) {
                this.createProcessForm.getProcesses().add(parentTempProcess);
            }
            this.createProcessForm.setEditActiveTabIndex(ADDITIONAL_FIELDS_TAB_INDEX);
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

    private void setParentAsTitleRecord(Process parentProcess) {
        this.createProcessForm.setEditActiveTabIndex(TITLE_RECORD_LINK_TAB_INDEX);
        ArrayList<SelectItem> parentCandidates = new ArrayList<>();
        parentCandidates.add(new SelectItem(parentProcess.getId().toString(), parentProcess.getTitle()));
        this.createProcessForm.getTitleRecordLinkTab().setPossibleParentProcesses(parentCandidates);
        this.createProcessForm.getTitleRecordLinkTab().setChosenParentProcess((String)parentCandidates.get(0).getValue());
        this.createProcessForm.getTitleRecordLinkTab().chooseParentProcess();
        Ajax.update(INSERTION_TREE);
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
}
