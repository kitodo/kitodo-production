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

package org.kitodo.production.forms;

import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.MassImportService;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

@Named("MassImportForm")
@SessionScoped
public class MassImportForm extends BaseForm {

    private int projectId;
    private int templateId;
    private String selectedCatalog;
    private UploadedFile file;

    public void prepareMassImport(int templateId, int projectId) {
        this.projectId = projectId;
        this.templateId = templateId;
    }
    public void handleFileUpload(FileUploadEvent event) {
        MassImportService massImportService = ServiceManager.getMassImportService();
        massImportService.importFromCSV(selectedCatalog, event.getFile());
    }
    /**
     * Get projectId.
     *
     * @return value of projectId
     */
    public int getProjectId() {
        return projectId;
    }

    /**
     * Set projectId.
     *
     * @param projectId
     *            as int
     */
    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    /**
     * Get templateId.
     *
     * @return value of templateId
     */
    public int getTemplateId() {
        return templateId;
    }

    /**
     * Set templateId.
     *
     * @param templateId
     *            as int
     */
    public void setTemplateId(int templateId) {
        this.templateId = templateId;
    }

    /**
     * Get selectedCatalog.
     *
     * @return value of selectedCatalog
     */
    public String getSelectedCatalog() {
        return selectedCatalog;
    }

    /**
     * Set selectedCatalog.
     *
     * @param selectedCatalog as java.lang.String
     */
    public void setSelectedCatalog(String selectedCatalog) {
        this.selectedCatalog = selectedCatalog;
    }

    /**
     * Get file.
     *
     * @return value of file
     */
    public UploadedFile getFile() {
        return file;
    }

    /**
     * Set file.
     *
     * @param file as org.primefaces.model.UploadedFile
     */
    public void setFile(UploadedFile file) {
        this.file = file;
    }
}
