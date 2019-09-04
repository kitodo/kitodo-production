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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.file.FileService;

@Named("MigrationForm")
@SessionScoped
public class MigrationForm implements Serializable {

    private static final Logger logger = LogManager.getLogger(MigrationForm.class);

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

}
