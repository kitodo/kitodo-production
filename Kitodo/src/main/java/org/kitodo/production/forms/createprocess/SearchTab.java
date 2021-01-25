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
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.dto.ProcessDTO;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ImportService;

public class SearchTab {

    private static final Logger logger = LogManager.getLogger(SearchTab.class);

    private final CreateProcessForm createProcessForm;
    private Process originalProcess;

    SearchTab(CreateProcessForm createProcessForm) {
        this.createProcessForm = createProcessForm;
    }

    /**
     * Get originalProcess.
     *
     * @return value of originalProcess
     */
    public Process getOriginalProcess() {
        return originalProcess;
    }

    /**
     * Set originalProcess.
     *
     * @param originalProcess as java.lang.Process
     */
    public void setOriginalProcess(Process originalProcess) {
        this.originalProcess = originalProcess;
    }

    /**
     * Get Process templates.
     *
     * @return list of SelectItem objects
     */
    public List<ProcessDTO> getProcessesForChoiceList() {
        try {
            return ServiceManager.getProcessService().findAll();
        } catch (DataException e) {
            Helper.setErrorMessage(CreateProcessForm.ERROR_READING, logger, e);
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * Evaluate selected template.
     */
    public void evaluateTemplateSelection() {
        fillProcessDetailsFromOriginalProcessProperties();
        try {
            URI uri = ServiceManager.getProcessService().getMetadataFileUri(originalProcess);
            ServiceManager.getMetsService().loadWorkpiece(uri);
        } catch (IOException e) {
            Helper.setErrorMessage(CreateProcessForm.ERROR_READING, new Object[] {"template-metadata" }, logger, e);
        }
    }

    private void fillProcessDetailsFromOriginalProcessProperties() {
        fillProcessDetailsFromWorkpieceProperties();
        fillProcessDetailsFromTemplateProperties();
    }

    private void fillProcessDetailsFromWorkpieceProperties() {
        for (Property workpieceProperty : this.originalProcess.getWorkpieces()) {
            for (ProcessDetail detail : this.createProcessForm.getProcessMetadataTab().getProcessDetailsElements()) {
                if (detail.getLabel().equals(workpieceProperty.getTitle())) {
                    ImportService.setProcessDetailValue(detail, workpieceProperty.getValue());
                }
                if (workpieceProperty.getTitle().equals("DocType")) {
                    this.createProcessForm.getProcessDataTab().setDocType(workpieceProperty.getValue());
                }
            }
        }
    }

    private void fillProcessDetailsFromTemplateProperties() {
        for (Property templateProperty : this.originalProcess.getTemplates()) {
            for (ProcessDetail processDetail : this.createProcessForm.getProcessMetadataTab().getProcessDetailsElements()) {
                if (processDetail.getLabel().equals(templateProperty.getTitle())) {
                    ImportService.setProcessDetailValue(processDetail, templateProperty.getValue());
                }
            }
        }
    }
}
