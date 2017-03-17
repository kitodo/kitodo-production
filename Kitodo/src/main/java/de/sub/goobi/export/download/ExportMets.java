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

import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.config.ConfigProjects;
import de.sub.goobi.export.dms.ExportDms;
import de.sub.goobi.export.dms.ExportDms_CorrectRusdml;
import de.sub.goobi.forms.LoginForm;
import de.sub.goobi.helper.FilesystemHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.VariableReplacer;
import de.sub.goobi.helper.exceptions.ExportFileException;
import de.sub.goobi.helper.exceptions.InvalidImagesException;
import de.sub.goobi.helper.exceptions.UghHelperException;
import de.sub.goobi.metadaten.MetadatenImagesHelper;
import de.sub.goobi.metadaten.copier.CopierData;
import de.sub.goobi.metadaten.copier.DataCopier;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;

import org.goobi.io.SafeFile;

import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.ProjectFileGroup;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.exceptions.SwapException;
import org.kitodo.services.ServiceManager;

import ugh.dl.ContentFile;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.Fileformat;
import ugh.dl.Prefs;
import ugh.dl.VirtualFileGroup;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.WriteException;
import ugh.fileformats.mets.MetsModsImportExport;

public class ExportMets {
    private final ServiceManager serviceManager = new ServiceManager();
    protected Helper help = new Helper();
    protected Prefs myPrefs;

    protected static final Logger myLogger = Logger.getLogger(ExportMets.class);

    /**
     * DMS-Export in das Benutzer-Homeverzeichnis.
     *
     * @param myProcess Process object
     */
    public boolean startExport(Process myProcess)
            throws IOException, InterruptedException, DocStructHasNoTypeException, PreferencesException,
            WriteException, MetadataTypeNotAllowedException, ExportFileException, UghHelperException, ReadException,
            SwapException, DAOException, TypeNotAllowedForParentException {
        LoginForm login = (LoginForm) Helper.getManagedBeanValue("#{LoginForm}");
        String userHome = "";
        if (login != null) {
            userHome = serviceManager.getUserService().getHomeDirectory(login.getMyBenutzer());
        }
        return startExport(myProcess, userHome);
    }

    /**
     * DMS-Export an eine gewünschte Stelle.
     *
     * @param myProcess Process object
     * @param inZielVerzeichnis String
     */
    public boolean startExport(Process myProcess, String inZielVerzeichnis)
            throws IOException, InterruptedException, PreferencesException, WriteException,
            DocStructHasNoTypeException, MetadataTypeNotAllowedException, ExportFileException, UghHelperException,
            ReadException, SwapException, DAOException, TypeNotAllowedForParentException {

        /*
         * Read Document
         */
        this.myPrefs = serviceManager.getRulesetService().getPreferences(myProcess.getRuleset());
        String atsPpnBand = myProcess.getTitle();
        Fileformat gdzfile = serviceManager.getProcessService().readMetadataFile(myProcess);

        String rules = ConfigMain.getParameter("copyData.onExport");
        if (rules != null && !rules.equals("- keine Konfiguration gefunden -")) {
            try {
                new DataCopier(rules).process(new CopierData(gdzfile, myProcess));
            } catch (ConfigurationException e) {
                Helper.setFehlerMeldung("dataCopier.syntaxError", e.getMessage());
                return false;
            } catch (RuntimeException exception) {
                Helper.setFehlerMeldung("dataCopier.runtimeException", exception.getMessage());
                return false;
            }
        }

        /* nur beim Rusdml-Projekt die Metadaten aufbereiten */
        ConfigProjects cp = new ConfigProjects(myProcess.getProject().getTitle());
        if (cp.getParamList("dmsImport.check").contains("rusdml")) {
            ExportDms_CorrectRusdml expcorr = new ExportDms_CorrectRusdml(myProcess, this.myPrefs, gdzfile);
            atsPpnBand = expcorr.correctionStart();
        }

        String zielVerzeichnis = prepareUserDirectory(inZielVerzeichnis);

        String targetFileName = zielVerzeichnis + atsPpnBand + "_mets.xml";
        return writeMetsFile(myProcess, targetFileName, gdzfile, false);
    }

    /**
     * prepare user directory.
     *
     * @param inTargetFolder the folder to prove and maybe create it
     */
    protected String prepareUserDirectory(String inTargetFolder) {
        String target = inTargetFolder;
        User myBenutzer = (User) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
        if (myBenutzer != null) {
            try {
                FilesystemHelper.createDirectoryForUser(target, myBenutzer.getLogin());
            } catch (Exception e) {
                Helper.setFehlerMeldung("Export canceled, could not create destination directory: "
                        + inTargetFolder, e);
            }
        }
        return target;
    }

    /**
     * write MetsFile to given Path.
     *
     * @param myProcess the Process to use
     * @param targetFileName the filename where the metsfile should be written
     * @param gdzfile the FileFormat-Object to use for Mets-Writing
     */

    protected boolean writeMetsFile(Process myProcess, String targetFileName, Fileformat gdzfile, boolean writeLocalFilegroup)
            throws PreferencesException, WriteException, IOException, InterruptedException, SwapException, DAOException,
            TypeNotAllowedForParentException {

        MetsModsImportExport mm = new MetsModsImportExport(this.myPrefs);
        mm.setWriteLocal(writeLocalFilegroup);
        String imageFolderPath = serviceManager.getProcessService().getImagesDirectory(myProcess);
        SafeFile imageFolder = new SafeFile(imageFolderPath);
        /*
         * before creating mets file, change relative path to absolute -
         */
        DigitalDocument dd = gdzfile.getDigitalDocument();
        if (dd.getFileSet() == null) {
            Helper.setMeldung(myProcess.getTitle()
                    + ": digital document does not contain images; temporarily adding them for mets file creation");
            MetadatenImagesHelper mih = new MetadatenImagesHelper(this.myPrefs, dd);
            mih.createPagination(myProcess, null);
        }

        /*
         * get the topstruct element of the digital document depending on anchor property
         */
        DocStruct topElement = dd.getLogicalDocStruct();
        if (this.myPrefs.getDocStrctTypeByName(topElement.getType().getName()).getAnchorClass() != null) {
            if (topElement.getAllChildren() == null || topElement.getAllChildren().size() == 0) {
                throw new PreferencesException(myProcess.getTitle()
                        + ": the topstruct element is marked as anchor, but does not have any children for "
                        + "physical docstrucs");
            } else {
                topElement = topElement.getAllChildren().get(0);
            }
        }

        /*
         * if the top element does not have any image related, set them all
         */
        if (topElement.getAllToReferences("logical_physical") == null
                || topElement.getAllToReferences("logical_physical").size() == 0) {
            if (dd.getPhysicalDocStruct() != null && dd.getPhysicalDocStruct().getAllChildren() != null) {
                Helper.setMeldung(myProcess.getTitle()
                        + ": topstruct element does not have any referenced images yet; temporarily adding them "
                        + "for mets file creation");
                for (DocStruct mySeitenDocStruct : dd.getPhysicalDocStruct().getAllChildren()) {
                    topElement.addReferenceTo(mySeitenDocStruct, "logical_physical");
                }
            } else {
                if (this instanceof ExportDms && ((ExportDms) this).exportDmsTask != null) {
                    ((ExportDms) this).exportDmsTask.setException(new RuntimeException(myProcess.getTitle()
                            + ": could not find any referenced images, export aborted"));
                } else {
                    Helper.setFehlerMeldung(myProcess.getTitle()
                            + ": could not find any referenced images, export aborted");
                }
                return false;
            }
        }

        for (ContentFile cf : dd.getFileSet().getAllFiles()) {
            String location = cf.getLocation();
            // If the file's location string shoes no sign of any protocol,
            // use the file protocol.
            if (!location.contains("://")) {
                location = "file://" + location;
            }
            String url = new URL(location).getFile();
            SafeFile f = new SafeFile(!url.startsWith(imageFolder.toURL().getPath()) ? imageFolder : null, url);
            cf.setLocation(f.toURI().toString());
        }

        mm.setDigitalDocument(dd);

        /*
         * wenn Filegroups definiert wurden, werden diese jetzt in die Metsstruktur übernommen
         */
        // Replace all paths with the given VariableReplacer, also the file group paths!
        VariableReplacer vp = new VariableReplacer(mm.getDigitalDocument(), this.myPrefs, myProcess, null);
        List<ProjectFileGroup> myFilegroups = myProcess.getProject().getProjectFileGroups();

        if (myFilegroups != null && myFilegroups.size() > 0) {
            for (ProjectFileGroup pfg : myFilegroups) {
                // check if source files exists
                if (pfg.getFolder() != null && pfg.getFolder().length() > 0) {
                    SafeFile folder = new SafeFile(serviceManager.getProcessService()
                            .getMethodFromName(pfg.getFolder(), myProcess));
                    if (folder.exists() && folder.list().length > 0) {
                        VirtualFileGroup v = new VirtualFileGroup();
                        v.setName(pfg.getName());
                        v.setPathToFiles(vp.replace(pfg.getPath()));
                        v.setMimetype(pfg.getMimeType());
                        v.setFileSuffix(pfg.getSuffix());
                        v.setOrdinary(!pfg.isPreviewImage());
                        mm.getDigitalDocument().getFileSet().addVirtualFileGroup(v);
                    }
                } else {

                    VirtualFileGroup v = new VirtualFileGroup();
                    v.setName(pfg.getName());
                    v.setPathToFiles(vp.replace(pfg.getPath()));
                    v.setMimetype(pfg.getMimeType());
                    v.setFileSuffix(pfg.getSuffix());
                    v.setOrdinary(!pfg.isPreviewImage());
                    mm.getDigitalDocument().getFileSet().addVirtualFileGroup(v);
                }
            }
        }

        // Replace rights and digiprov entries.
        mm.setRightsOwner(vp.replace(myProcess.getProject().getMetsRightsOwner()));
        mm.setRightsOwnerLogo(vp.replace(myProcess.getProject().getMetsRightsOwnerLogo()));
        mm.setRightsOwnerSiteURL(vp.replace(myProcess.getProject().getMetsRightsOwnerSite()));
        mm.setRightsOwnerContact(vp.replace(myProcess.getProject().getMetsRightsOwnerMail()));
        mm.setDigiprovPresentation(vp.replace(myProcess.getProject().getMetsDigiprovPresentation()));
        mm.setDigiprovReference(vp.replace(myProcess.getProject().getMetsDigiprovReference()));
        mm.setDigiprovPresentationAnchor(vp.replace(myProcess.getProject().getMetsDigiprovPresentationAnchor()));
        mm.setDigiprovReferenceAnchor(vp.replace(myProcess.getProject().getMetsDigiprovReferenceAnchor()));

        mm.setPurlUrl(vp.replace(myProcess.getProject().getMetsPurl()));
        mm.setContentIDs(vp.replace(myProcess.getProject().getMetsContentIDs()));

        // Set mets pointers. MetsPointerPathAnchor or mptrAnchorUrl  is the
        // pointer used to point to the superordinate (anchor) file, that is
        // representing a “virtual” group such as a series. Several anchors
        // pointer paths can be defined/ since it is possible to define several
        // levels of superordinate structures (such as the complete edition of
        // a daily newspaper, one year ouf of that edition, …)
        String anchorPointersToReplace = myProcess.getProject().getMetsPointerPath();
        mm.setMptrUrl(null);
        for (String anchorPointerToReplace : anchorPointersToReplace.split(Project.ANCHOR_SEPARATOR)) {
            String anchorPointer = vp.replace(anchorPointerToReplace);
            mm.setMptrUrl(anchorPointer);
        }

        // metsPointerPathAnchor or mptrAnchorUrl is the pointer used to point from the (lowest) superordinate
        // (anchor) file to the lowest level file (the non-anchor file).
        String metsPointerToReplace = myProcess.getProject().getMetsPointerPathAnchor();
        String metsPointer = vp.replace(metsPointerToReplace);
        mm.setMptrAnchorUrl(metsPointer);

        if (ConfigMain.getBooleanParameter("ExportValidateImages", true)) {
            try {
                // TODO andere Dateigruppen nicht mit image Namen ersetzen
                List<String> images = new MetadatenImagesHelper(this.myPrefs, dd).getDataFiles(myProcess);
                int sizeOfPagination = dd.getPhysicalDocStruct().getAllChildren().size();
                if (images != null) {
                    int sizeOfImages = images.size();
                    if (sizeOfPagination == sizeOfImages) {
                        dd.overrideContentFiles(images);
                    } else {
                        List<String> param = new ArrayList<String>();
                        param.add(String.valueOf(sizeOfPagination));
                        param.add(String.valueOf(sizeOfImages));
                        Helper.setFehlerMeldung(Helper.getTranslation("imagePaginationError", param));
                        return false;
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                myLogger.error(e);
                return false;
            } catch (InvalidImagesException e) {
                myLogger.error(e);
                return false;
            }
        } else {
            // create pagination out of virtual file names
            dd.addAllContentFiles();

        }
        mm.write(targetFileName);
        Helper.setMeldung(null, myProcess.getTitle() + ": ", "ExportFinished");
        return true;
    }
}
