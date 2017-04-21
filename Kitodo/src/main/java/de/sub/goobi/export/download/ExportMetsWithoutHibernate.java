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

import de.sub.goobi.forms.LoginForm;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.VariableReplacerWithoutHibernate;
import de.sub.goobi.helper.exceptions.ExportFileException;
import de.sub.goobi.helper.exceptions.InvalidImagesException;
import de.sub.goobi.helper.exceptions.UghHelperException;
import de.sub.goobi.persistence.apache.FolderInformation;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.goobi.io.SafeFile;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.ProjectFileGroup;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.exceptions.SwapException;
import org.kitodo.data.database.persistence.apache.ProcessManager;
import org.kitodo.data.database.persistence.apache.ProcessObject;
import org.kitodo.data.database.persistence.apache.ProjectManager;
import org.kitodo.data.database.persistence.apache.ProjectObject;
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

public class ExportMetsWithoutHibernate {
    protected Helper help = new Helper();
    protected Prefs myPrefs;
    private FolderInformation fi;
    private ProjectObject project;
    private final ServiceManager serviceManager = new ServiceManager();

    protected static final Logger myLogger = Logger.getLogger(ExportMetsWithoutHibernate.class);

    /**
     * DMS-Export in das Benutzer-Homeverzeichnis.
     *
     * @param process
     *            ProcessObject
     */
    public boolean startExport(ProcessObject process)
            throws IOException, InterruptedException, DocStructHasNoTypeException, PreferencesException, WriteException,
            MetadataTypeNotAllowedException, ExportFileException, UghHelperException, ReadException, SwapException,
            DAOException, TypeNotAllowedForParentException {
        LoginForm login = (LoginForm) Helper.getManagedBeanValue("#{LoginForm}");
        String benutzerHome = "";
        if (login != null) {
            benutzerHome = serviceManager.getUserService().getHomeDirectory(login.getMyBenutzer());
        }
        return startExport(process, benutzerHome);
    }

    /**
     * DMS-Export an eine gewünschte Stelle.
     *
     * @param process
     *            ProcessObject
     * @param inZielVerzeichnis
     *            String
     */
    public boolean startExport(ProcessObject process, String inZielVerzeichnis)
            throws IOException, InterruptedException, PreferencesException, WriteException, DocStructHasNoTypeException,
            MetadataTypeNotAllowedException, ExportFileException, UghHelperException, ReadException, SwapException,
            DAOException, TypeNotAllowedForParentException {

        /*
         * Read Document
         */
        this.myPrefs = serviceManager.getRulesetService()
                .getPreferences(ProcessManager.getRuleset(process.getRulesetId()));

        this.project = ProjectManager.getProjectById(process.getProjectId());
        String atsPpnBand = process.getTitle();
        this.fi = new FolderInformation(process.getId(), process.getTitle());
        Fileformat gdzfile = process.readMetadataFile(this.fi.getMetadataFilePath(), this.myPrefs);

        String zielVerzeichnis = prepareUserDirectory(inZielVerzeichnis);

        String targetFileName = zielVerzeichnis + atsPpnBand + "_mets.xml";
        return writeMetsFile(process, targetFileName, gdzfile, false);
    }

    /**
     * Prepare user directory.
     *
     * @param inTargetFolder
     *            the folder to prove and maybe create it
     */
    protected String prepareUserDirectory(String inTargetFolder) {
        String target = inTargetFolder;
        User myBenutzer = (User) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
        try {
            serviceManager.getFileService().createDirectoryForUser(target, myBenutzer.getLogin());
        } catch (Exception e) {
            Helper.setFehlerMeldung("Export canceled, could not create destination directory: " + inTargetFolder, e);
        }
        return target;
    }

    /**
     * write MetsFile to given Path.
     *
     * @param process
     *            the Process to use
     * @param targetFileName
     *            the filename where the metsfile should be written
     * @param gdzfile
     *            the FileFormat-Object to use for Mets-Writing
     */
    protected boolean writeMetsFile(ProcessObject process, String targetFileName, Fileformat gdzfile,
            boolean writeLocalFilegroup) throws PreferencesException, WriteException, IOException, InterruptedException,
            SwapException, DAOException, TypeNotAllowedForParentException {
        this.fi = new FolderInformation(process.getId(), process.getTitle());
        this.myPrefs = serviceManager.getRulesetService()
                .getPreferences(ProcessManager.getRuleset(process.getRulesetId()));
        this.project = ProjectManager.getProjectById(process.getProjectId());
        MetsModsImportExport mm = new MetsModsImportExport(this.myPrefs);
        mm.setWriteLocal(writeLocalFilegroup);
        String imageFolderPath = this.fi.getImagesDirectory();
        SafeFile imageFolder = new SafeFile(imageFolderPath);
        /*
         * before creating mets file, change relative path to absolute -
         */
        DigitalDocument dd = gdzfile.getDigitalDocument();
        if (dd.getFileSet() == null) {
            Helper.setFehlerMeldung(process.getTitle() + ": digital document does not contain images; aborting");
            return false;
        }

        /*
         * get the topstruct element of the digital document depending on anchor
         * property
         */
        DocStruct topElement = dd.getLogicalDocStruct();
        if (this.myPrefs.getDocStrctTypeByName(topElement.getType().getName()).getAnchorClass() != null) {
            if (topElement.getAllChildren() == null || topElement.getAllChildren().size() == 0) {
                throw new PreferencesException(process.getTitle()
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
                Helper.setMeldung(process.getTitle()
                        + ": topstruct element does not have any referenced images yet; temporarily adding them "
                        + "for mets file creation");
                for (DocStruct mySeitenDocStruct : dd.getPhysicalDocStruct().getAllChildren()) {
                    topElement.addReferenceTo(mySeitenDocStruct, "logical_physical");
                }
            } else {
                Helper.setFehlerMeldung(process.getTitle() + ": could not find any referenced images, export aborted");
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
         * wenn Filegroups definiert wurden, werden diese jetzt in die
         * Metsstruktur übernommen
         */
        // Replace all paths with the given VariableReplacer, also the file
        // group paths!
        VariableReplacerWithoutHibernate vp = new VariableReplacerWithoutHibernate(mm.getDigitalDocument(),
                this.myPrefs, process, null);
        List<ProjectFileGroup> myFilegroups = ProjectManager.getFilegroupsForProjectId(this.project.getId());

        if (myFilegroups != null && myFilegroups.size() > 0) {
            for (ProjectFileGroup pfg : myFilegroups) {
                // check if source files exists
                if (pfg.getFolder() != null && pfg.getFolder().length() > 0) {
                    SafeFile folder = new SafeFile(this.fi.getMethodFromName(pfg.getFolder()));
                    if (folder.exists() && folder.list().length > 0) {
                        VirtualFileGroup v = new VirtualFileGroup();
                        v.setName(pfg.getName());
                        v.setPathToFiles(vp.replace(pfg.getPath()));
                        v.setMimetype(pfg.getMimeType());
                        v.setFileSuffix(pfg.getSuffix());
                        mm.getDigitalDocument().getFileSet().addVirtualFileGroup(v);
                    }
                } else {

                    VirtualFileGroup v = new VirtualFileGroup();
                    v.setName(pfg.getName());
                    v.setPathToFiles(vp.replace(pfg.getPath()));
                    v.setMimetype(pfg.getMimeType());
                    v.setFileSuffix(pfg.getSuffix());
                    mm.getDigitalDocument().getFileSet().addVirtualFileGroup(v);
                }
            }
        }

        // Replace rights and digiprov entries.
        mm.setRightsOwner(vp.replace(this.project.getMetsRightsOwner()));
        mm.setRightsOwnerLogo(vp.replace(this.project.getMetsRightsOwnerLogo()));
        mm.setRightsOwnerSiteURL(vp.replace(this.project.getMetsRightsOwnerSite()));
        mm.setRightsOwnerContact(vp.replace(this.project.getMetsRightsOwnerMail()));
        mm.setDigiprovPresentation(vp.replace(this.project.getMetsDigiprovPresentation()));
        mm.setDigiprovReference(vp.replace(this.project.getMetsDigiprovReference()));
        mm.setDigiprovPresentationAnchor(vp.replace(this.project.getMetsDigiprovPresentationAnchor()));
        mm.setDigiprovReferenceAnchor(vp.replace(this.project.getMetsDigiprovReferenceAnchor()));

        mm.setPurlUrl(vp.replace(this.project.getMetsPurl()));
        mm.setContentIDs(vp.replace(this.project.getMetsContentIDs()));

        // Set mets pointers. MetsPointerPathAnchor or mptrAnchorUrl is the
        // pointer used to point to the superordinate (anchor) file, that is
        // representing a “virtual” group such as a series. Several anchors
        // pointer paths can be defined/ since it is possible to define several
        // levels of superordinate structures (such as the complete edition of
        // a daily newspaper, one year ouf of that edition, …)
        String anchorPointersToReplace = this.project.getMetsPointerPath();
        mm.setMptrUrl(null);
        for (String anchorPointerToReplace : anchorPointersToReplace.split(Project.ANCHOR_SEPARATOR)) {
            String anchorPointer = vp.replace(anchorPointerToReplace);
            mm.setMptrUrl(anchorPointer);
        }

        // metsPointerPathAnchor or mptrAnchorUrl is the pointer used to point
        // from the (lowest) superordinate (anchor) file to the lowest level
        // file (the non-anchor file).
        String anchor = this.project.getMetsPointerPathAnchor();
        String pointer = vp.replace(anchor);
        mm.setMptrAnchorUrl(pointer);

        try {
            // TODO andere Dateigruppen nicht mit image Namen ersetzen
            List<String> images = this.fi.getDataFiles();
            if (images != null) {
                int sizeOfPagination = dd.getPhysicalDocStruct().getAllChildren().size();
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
        } catch (InvalidImagesException e) {
            myLogger.error(e);
        }
        mm.write(targetFileName);
        Helper.setMeldung(null, process.getTitle() + ": ", "ExportFinished");
        return true;
    }
}
