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

package de.sub.goobi.export.download;

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.config.ConfigProjects;
import de.sub.goobi.export.dms.ExportDms_CorrectRusdml;
import de.sub.goobi.forms.LoginForm;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.ExportFileException;
import de.sub.goobi.metadaten.copier.CopierData;
import de.sub.goobi.metadaten.copier.DataCopier;
import java.io.IOException;
import java.net.URI;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.ugh.FileformatInterface;
import org.kitodo.api.ugh.MetsModsImportExportInterface;
import org.kitodo.api.ugh.PrefsInterface;
import org.kitodo.api.ugh.UghImplementation;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.User;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.file.FileService;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.WriteException;

public class ExportMets {
    private final ServiceManager serviceManager = new ServiceManager();

    private final FileService fileService = serviceManager.getFileService();
    protected Helper help = new Helper();
    protected PrefsInterface myPrefs;

    protected static final Logger logger = LogManager.getLogger(ExportMets.class);

    /**
     * DMS-Export in das Benutzer-Homeverzeichnis.
     *
     * @param process
     *            Process object
     */
    public boolean startExport(Process process)
            throws IOException, DocStructHasNoTypeException, PreferencesException, WriteException,
            MetadataTypeNotAllowedException, ExportFileException, ReadException, TypeNotAllowedForParentException {
        LoginForm login = (LoginForm) Helper.getManagedBeanValue("#{LoginForm}");
        if (login != null) {
            URI userHome = serviceManager.getUserService().getHomeDirectory(login.getMyBenutzer());
            return startExport(process, userHome);
        }
        return false;
    }

    /**
     * DMS-Export an eine gewünschte Stelle.
     *
     * @param process
     *            Process object
     * @param userHome
     *            String
     */
    public boolean startExport(Process process, URI userHome)
            throws IOException, PreferencesException, WriteException, DocStructHasNoTypeException,
            MetadataTypeNotAllowedException, ExportFileException, ReadException, TypeNotAllowedForParentException {

        /*
         * Read Document
         */
        this.myPrefs = serviceManager.getRulesetService().getPreferences(process.getRuleset());
        String atsPpnBand = process.getTitle();
        FileformatInterface gdzfile = serviceManager.getProcessService().readMetadataFile(process);

        String rules = ConfigCore.getParameter("copyData.onExport");
        if (rules != null && !rules.equals("- keine Konfiguration gefunden -")) {
            try {
                new DataCopier(rules).process(new CopierData(gdzfile, process));
            } catch (ConfigurationException e) {
                Helper.setFehlerMeldung("dataCopier.syntaxError", e.getMessage());
                return false;
            } catch (RuntimeException exception) {
                Helper.setFehlerMeldung("dataCopier.runtimeException", exception.getMessage());
                return false;
            }
        }

        // only for the metadata of the RUSDML project
        ConfigProjects cp = new ConfigProjects(process.getProject().getTitle());
        if (cp.getParamList("dmsImport.check").contains("rusdml")) {
            ExportDms_CorrectRusdml exportCorrect = new ExportDms_CorrectRusdml(process, this.myPrefs, gdzfile);
            atsPpnBand = exportCorrect.correctionStart();
        }

        prepareUserDirectory(userHome);

        String targetFileName = atsPpnBand + "_mets.xml";
        URI metaFile = userHome.resolve(targetFileName);
        return writeMetsFile(process, metaFile, gdzfile, false);
    }

    /**
     * prepare user directory.
     *
     * @param targetFolder
     *            the folder to prove and maybe create it
     */
    protected void prepareUserDirectory(URI targetFolder) {
        User user = (User) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
        if (user != null) {
            try {
                fileService.createDirectoryForUser(targetFolder, user.getLogin());
            } catch (Exception e) {
                Helper.setFehlerMeldung("Export canceled, could not create destination directory: " + targetFolder, e);
            }
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
     * @param writeLocalFileGroup
     *            true or false
     * @return true or false
     */
    protected boolean writeMetsFile(Process process, URI metaFile, FileformatInterface gdzfile, boolean writeLocalFileGroup)
            throws PreferencesException, WriteException, IOException, TypeNotAllowedForParentException {

        MetsModsImportExportInterface mm = UghImplementation.INSTANCE.createMetsModsImportExport(this.myPrefs);
        mm.setWriteLocal(writeLocalFileGroup);
        mm = serviceManager.getSchemaService().tempConvert(gdzfile, this, mm, this.myPrefs, process);
        if (mm != null) {
            mm.write(metaFile.getRawPath());
            Helper.setMeldung(null, process.getTitle() + ": ", "ExportFinished");
            return true;
        }
        Helper.setFehlerMeldung(process.getTitle() + ": was not finished!");
        return false;
    }
}
