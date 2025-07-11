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

package org.kitodo.production.helper.tasks;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ProcessService;


public class UpdateInternalMetaInformationTask extends EmptyTask {

    private static final Logger logger = LogManager.getLogger(UpdateInternalMetaInformationTask.class);
    private final Project project;
    private final ProcessService processService;

    /**
     * Default constructor.
     *
     * @param project Choosen projet which process meta information should be updated.
     */
    public UpdateInternalMetaInformationTask(Project project) {
        super(project.getTitle());
        this.processService = ServiceManager.getProcessService();
        this.project = project;
    }

    /**
     * Copy constructor. Required for cloning tasks. Cloning is required to be
     * able to restart a task.
     *
     * @param source instance to make a copy from
     */
    private UpdateInternalMetaInformationTask(UpdateInternalMetaInformationTask source) {
        super(source);
        this.project = source.project;
        this.processService = source.processService;
    }

    @Override
    public String getDisplayName() {
        return Helper.getTranslation("taskUpdateInternalMetaInformation");
    }

    @Override
    public UpdateInternalMetaInformationTask replace() {
        return new UpdateInternalMetaInformationTask(this);
    }

    @Override
    public void run() {
        String projectTitle = project.getTitle();
        List<Process> processList = project.getProcesses();
        int processListSize = processList.size();
        int progress = 0;
        String processTitle = "";

        try {
            logger.info("Start updating internal meta information on project {}", projectTitle);
            for (Process process : project.getProcesses()) {
                processTitle = process.getTitle();
                logger.info("Updating internal meta information for process {} (id {})", processTitle, process.getId());
                processService.updateAmountOfInternalMetaInformation(process, true);
                // update progress in user interface
                super.setProgress(100 * ++progress / processListSize);
            }
            logger.info("Updating internal meta information on project {} is finished", projectTitle);
        } catch (DataException exception) {
            Helper.setErrorMessage(exception.getLocalizedMessage(), processTitle, logger, exception);
            super.setException(exception);
        }
        processList.clear();
    }
}
