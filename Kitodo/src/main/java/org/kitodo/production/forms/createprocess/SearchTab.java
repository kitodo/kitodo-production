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
import java.util.List;

import javax.faces.model.SelectItem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;
import org.kitodo.production.forms.CreateProcessForm;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.SelectItemList;
import org.kitodo.production.services.ServiceManager;

public class SearchTab {

    private static final Logger logger = LogManager.getLogger(SearchTab.class);

    private CreateProcessForm createProcessForm;
    private Process templateProcess;

    public SearchTab(CreateProcessForm createProcessForm) {
        this.createProcessForm = createProcessForm;
    }

    /**
     * Get templateProcess.
     *
     * @return value of templateProcess
     */
    public Process getTemplateProcess() {
        return templateProcess;
    }

    /**
     * Set templateProcess.
     *
     * @param templateProcess as java.lang.Process
     */
    public void setTemplateProcess(Process templateProcess) {
        this.templateProcess = templateProcess;
    }

    /**
     * Get Process templates.
     *
     * @return list of SelectItem objects
     */
    public List<SelectItem> getProcessesForChoiceList() {
        return SelectItemList.getProcessesForChoiceList();
    }

    /**
     * Auswahl des Prozesses auswerten.
     */
    public String evaluateTemplateSelection() {
        readTemplateSelection();

        try {
            URI uri = ServiceManager.getProcessService().getMetadataFileUri(templateProcess);
            ServiceManager.getMetsService().loadWorkpiece(uri);
        } catch (IOException e) {
            Helper.setErrorMessage(CreateProcessForm.ERROR_READING, new Object[] {"template-metadata" }, logger, e);
        }
        return null;
    }

    private void readTemplateSelection() {
        readTemplateWorkpieces(this.createProcessForm.getAdditionalDetailsTab().getAdditionalDetailsTableRows(),
                this.templateProcess);
        readTemplateTemplates(this.createProcessForm.getAdditionalDetailsTab().getAdditionalDetailsTableRows(),
                this.templateProcess);
        readTemplateProperties(this.createProcessForm.getProcessDataTab().getDigitalCollections(),
                this.templateProcess);
    }

    private void readTemplateWorkpieces(List<AdditionalDetailsTableRow> additionalFields, Process processForChoice) {
        for (Property workpieceProperty : processForChoice.getWorkpieces()) {
            for (AdditionalDetailsTableRow row : additionalFields) {
                if (row.getLabel().equals(workpieceProperty.getTitle())) {
                    this.createProcessForm.getAdditionalDetailsTab().setAdditionalDetailsRow(row,
                            workpieceProperty.getValue());
                }
                if (workpieceProperty.getTitle().equals("DocType")) {
                    this.createProcessForm.getProcessDataTab().setDocType(workpieceProperty.getValue());
                }
            }
        }
    }

    private void readTemplateTemplates(List<AdditionalDetailsTableRow> additionalFields, Process processForChoice) {
        for (Property templateProperty : processForChoice.getTemplates()) {
            for (AdditionalDetailsTableRow row : additionalFields) {
                if (row.getLabel().equals(templateProperty.getTitle())) {
                    this.createProcessForm.getAdditionalDetailsTab().setAdditionalDetailsRow(row,
                            templateProperty.getValue());
                }
            }
        }
    }

    // TODO: check whether we only need digital collections as "metadata" in the future
    //  in that case: refactor this method to work like "readTemplateTemplates" and "readTemplateWorkpieces"!
    private void readTemplateProperties(List<String> digitalCollections, Process processForChoice) {
        for (Property processProperty : processForChoice.getProperties()) {
            if (processProperty.getTitle().equals("digitalCollection")) {
                digitalCollections.add(processProperty.getValue());
            }
        }
    }
}
