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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;

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
    public List<Process> getProcessesForChoiceList() {
        List<Process> processes = new ArrayList<>();
        User currentUser = ServiceManager.getUserService().getCurrentUser();
        for (Project project : currentUser.getProjects()) {
            try {
                processes.addAll(ServiceManager.getProjectService().getById(project.getId()).getProcesses());
            } catch (DAOException e) {
                Helper.setErrorMessage(e);
            }
        }
        processes = processes.stream().filter(Process::getInChoiceListShown).collect(Collectors.toList());
        return processes;
    }

    /**
     * Copy metadata of selected process.
     */
    public void copyMetadata() {
        try {
            URI metadataUri = ServiceManager.getProcessService().getMetadataFileUri(this.originalProcess);
            Workpiece workpiece = ServiceManager.getMetsService().loadWorkpiece(metadataUri);
            LogicalDivision root = workpiece.getRootElement();
            if (StringUtils.isNotBlank(root.getType())) {
                this.createProcessForm.getProcessDataTab().setDocType(root.getType());
            }
            if (Objects.nonNull(originalProcess.getParent())) {
                this.createProcessForm.getTitleRecordLinkTab().setParentAsTitleRecord(originalProcess.getParent());
            }
            this.createProcessForm.getProcessMetadataTab().getProcessDetails().setMetadata(root.getMetadata());
            this.createProcessForm.setEditActiveTabIndex(CreateProcessForm.ADDITIONAL_FIELDS_TAB_INDEX);
        } catch (IOException e) {
            Helper.setErrorMessage(CreateProcessForm.ERROR_READING, new Object[] {"template-metadata" }, logger, e);
        }
    }
}
