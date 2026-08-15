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

package org.kitodo.production.services.export;

import java.io.IOException;
import java.net.URI;

import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.exceptions.FileStructureValidationException;
import org.kitodo.export.ExportMets;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetsModsDigitalDocumentHelper;
import org.kitodo.production.services.ServiceManager;
import org.xml.sax.SAXException;

public class MetsExportService {

    /**
     * Exports the METS file of the given process to the authenticated user's home directory.
     *
     * @param process the process to export
     */
    public static void exportToUserHome(Process process)
            throws DAOException, IOException, SAXException, FileStructureValidationException {

        User user = ServiceManager.getUserService().getAuthenticatedUser();
        URI userHome = ServiceManager.getUserService().getHomeDirectory(user);

        LegacyMetsModsDigitalDocumentHelper gdzfile =
                ServiceManager.getProcessService().readMetadataFile(process);
        if (ServiceManager.getProcessService()
                .handleExceptionsForConfiguration(gdzfile, process)) {
            return;
        }
        try {
            ServiceManager.getFileService()
                    .createDirectoryForUser(userHome, user.getLogin());
        } catch (IOException | RuntimeException e) {
            Helper.setErrorMessage("Export canceled, could not create destination directory: " + userHome, e.getMessage());
            return;
        }
        String fileName = Helper.getNormalizedTitle(process.getTitle()) + "_mets.xml";
        URI target = userHome.resolve(userHome.getRawPath() + "/" + fileName);
        new ExportMets().writeMetsFile(process, target, gdzfile);
    }
}
