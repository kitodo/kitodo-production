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

package org.kitodo.export;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataformat.mets.MetsXmlElementAccessInterface;
import org.kitodo.api.ugh.FileformatInterface;
import org.kitodo.api.ugh.exceptions.MetadataTypeNotAllowedException;
import org.kitodo.api.ugh.exceptions.PreferencesException;
import org.kitodo.api.ugh.exceptions.ReadException;
import org.kitodo.api.ugh.exceptions.WriteException;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.User;
import org.kitodo.exceptions.ExportFileException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetsModsDigitalDocumentHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyPrefsHelper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.file.FileService;

public class ExportMets {
    private final FileService fileService = ServiceManager.getFileService();
    protected LegacyPrefsHelper myPrefs;

    private static final Logger logger = LogManager.getLogger(ExportMets.class);

    /**
     * DMS-Export in das Benutzer-Homeverzeichnis.
     *
     * @param process
     *            Process object
     */
    public boolean startExport(Process process) throws IOException, PreferencesException, WriteException,
            MetadataTypeNotAllowedException, ExportFileException, ReadException, JAXBException {
        User user = ServiceManager.getUserService().getAuthenticatedUser();
        URI userHome = ServiceManager.getUserService().getHomeDirectory(user);
        return startExport(process, userHome);
    }

    /**
     * DMS-Export an eine gewünschte Stelle.
     *
     * @param process
     *            Process object
     * @param userHome
     *            String
     */
    public boolean startExport(Process process, URI userHome) throws IOException, PreferencesException, WriteException,
            MetadataTypeNotAllowedException, ExportFileException, ReadException, JAXBException {

        /*
         * Read Document
         */
        this.myPrefs = ServiceManager.getRulesetService().getPreferences(process.getRuleset());
        String atsPpnBand = ServiceManager.getProcessService().getNormalizedTitle(process.getTitle());
        FileformatInterface gdzfile = ServiceManager.getProcessService().readMetadataFile(process);

        if (ServiceManager.getProcessService().handleExceptionsForConfiguration(gdzfile, process)) {
            return false;
        }

        prepareUserDirectory(userHome);

        String targetFileName = atsPpnBand + "_mets.xml";
        URI metaFile = userHome.resolve(userHome.getRawPath() + "/" + targetFileName);
        return writeMetsFile(process, metaFile, gdzfile);
    }

    /**
     * prepare user directory.
     *
     * @param targetFolder
     *            the folder to prove and maybe create it
     */
    protected void prepareUserDirectory(URI targetFolder) {
        User user = ServiceManager.getUserService().getAuthenticatedUser();
        try {
            fileService.createDirectoryForUser(targetFolder, user.getLogin());
        } catch (IOException | RuntimeException e) {
            Helper.setErrorMessage("Export canceled, could not create destination directory: " + targetFolder, logger,
                e);
        }
    }

    /**
     * write MetsFile to given Path.
     *
     * @param process
     *            the Process to use
     * @param metaFile
     *            the meta file which should be written
     * @param gdzfile
     *            the FileFormat-Object to use for Mets-Writing
     * @return true or false
     */
    protected boolean writeMetsFile(Process process, URI metaFile, FileformatInterface gdzfile)
            throws IOException {

        MetsXmlElementAccessInterface workpiece = ((LegacyMetsModsDigitalDocumentHelper) gdzfile).getWorkpiece();
        ServiceManager.getSchemaService().tempConvert(workpiece, this, this.myPrefs, process);
        /*
         * We write to the user’s home directory or to the hotfolder here, not
         * to a content repository, therefore no use of file service.
         */
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            workpiece.save(out);
            try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(out.toByteArray())) {
                StreamSource source = new StreamSource(byteArrayInputStream);
                try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(new File(metaFile)))) {
                    URI xslFile = XsltHelper.getXsltFileFromConfig(process);
                    bufferedOutputStream.write(XsltHelper.transformXmlByXslt(source, xslFile).toByteArray());
                } catch (TransformerException e) {
                    logger.error("Writing Mets file failed!", e);
                }
            }
        }

        Helper.setMessage(process.getTitle() + ": ", "exportFinished");
        return true;
    }
}
