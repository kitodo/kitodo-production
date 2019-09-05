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

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import javax.xml.ws.Service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ProcessService;
import org.kitodo.production.services.file.FileService;

@Named("MigrationForm")
@SessionScoped
public class MigrationForm implements Serializable {

    private static final Logger logger = LogManager.getLogger(MigrationForm.class);
    private List<Project> allProjects;
    private List<Project> selectedProjects;
    private List<Process> processList;
    private boolean projectListShown;

    public void migrateMetadata() throws DAOException {
        List<Process> processes = ServiceManager.getProcessService().getAll();
        FileService fileService = ServiceManager.getFileService();
        URI metadataFilePath;
        for (Process process : processes) {
            try {
                metadataFilePath = fileService.getMetadataFilePath(process, true, true);
                ServiceManager.getDataEditorService().readData(metadataFilePath);
            } catch (IOException e) {
                Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            }
        }
    }

    public void showPossibleProjects() {
        try {
            allProjects = ServiceManager.getProjectService().getAll();
            projectListShown = true;
        } catch (DAOException e) {
            Helper.setErrorMessage("Error during database access");
        }
    }

    public void showProcessesForProjects(){
        ProcessService processService = ServiceManager.getProcessService();
        for (Project project : selectedProjects) {
//            try {
//                //processList.add(processService.getByQuery("SELECT "));
//            } catch (DataException e) {
//                Helper.setErrorMessage("Error during search");
//            }
        }

    }

    /**
     * Get allProjects.
     *
     * @return value of allProjects
     */
    public List<Project> getAllProjects() {
        return allProjects;
    }

    public void setSelectedProjects(List<Project> selectedProjects) {
        this.selectedProjects = selectedProjects;
    }

    public boolean isProjectListShown() {
        return projectListShown;
    }

    /**
     * Get selectedProjects.
     *
     * @return value of selectedProjects
     */
    public List<Project> getSelectedProjects() {
        return selectedProjects;
    }

    /**
     * Get processList.
     *
     * @return value of processList
     */
    public List<Process> getProcessList() {
        return processList;
    }
}
