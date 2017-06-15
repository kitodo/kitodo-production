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

package org.kitodo.services.schema;

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.export.dms.ExportDms;
import de.sub.goobi.export.download.ExportMets;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.VariableReplacer;
import de.sub.goobi.helper.exceptions.InvalidImagesException;
import de.sub.goobi.metadaten.MetadatenImagesHelper;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.ProjectFileGroup;

import org.kitodo.services.ServiceManager;
import ugh.dl.ContentFile;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.Fileformat;
import ugh.dl.Prefs;
import ugh.dl.VirtualFileGroup;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.fileformats.mets.MetsModsImportExport;

/**
 * Service for schema manipulations.
 */
public class SchemaService {

    private static final Logger logger = LogManager.getLogger(SchemaService.class);
    private final ServiceManager serviceManager = new ServiceManager();

    /**
     * Temporal method for separate file conversion from ExportMets class
     * (method writeMetsFile).
     *
     * @param exportMets
     *            class inside method is used
     * @param metsMods
     *            MetsModsImportExport object
     * @param prefs
     *            preferences - Prefs object
     * @param process
     *            object
     * @return MetsModsImportExport object
     */
    public <T extends ExportMets> MetsModsImportExport tempConvert(Fileformat gdzfile, T exportMets,
            MetsModsImportExport metsMods, Prefs prefs, Process process)
            throws IOException, PreferencesException, TypeNotAllowedForParentException {
        URI imageFolder = serviceManager.getFileService().getImagesDirectory(process);

        /*
         * before creating mets file, change relative path to absolute -
         */
        DigitalDocument digitalDocument = gdzfile.getDigitalDocument();
        if (digitalDocument.getFileSet() == null) {
            Helper.setMeldung(process.getTitle()
                    + ": digital document does not contain images; temporarily adding them for mets file creation");
            MetadatenImagesHelper mih = new MetadatenImagesHelper(prefs, digitalDocument);
            mih.createPagination(process, null);
        }

        /*
         * get the topstruct element of the digital document depending on anchor
         * property
         */
        DocStruct topElement = digitalDocument.getLogicalDocStruct();
        if (prefs.getDocStrctTypeByName(topElement.getType().getName()).getAnchorClass() != null) {
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
            if (digitalDocument.getPhysicalDocStruct() != null
                    && digitalDocument.getPhysicalDocStruct().getAllChildren() != null) {
                Helper.setMeldung(process.getTitle()
                        + ": topstruct element does not have any referenced images yet; temporarily adding them "
                        + "for mets file creation");
                for (DocStruct mySeitenDocStruct : digitalDocument.getPhysicalDocStruct().getAllChildren()) {
                    topElement.addReferenceTo(mySeitenDocStruct, "logical_physical");
                }
            } else {
                if (exportMets instanceof ExportDms && ((ExportDms) exportMets).exportDmsTask != null) {
                    ((ExportDms) exportMets).exportDmsTask.setException(new RuntimeException(
                            process.getTitle() + ": could not find any referenced images, export aborted"));
                } else {
                    Helper.setFehlerMeldung(
                            process.getTitle() + ": could not find any referenced images, export aborted");
                }
                return null;
            }
        }

        for (ContentFile cf : digitalDocument.getFileSet().getAllFiles()) {
            String location = cf.getLocation();
            // If the file's location string shoes no sign of any protocol,
            // use the file protocol.
            if (!location.contains("://")) {
                location = "file://" + location;
            }
            String url = new URL(location).getFile();
            URI uri = !url.startsWith(imageFolder.getPath()) ? imageFolder : URI.create("");
            uri = uri.resolve(url);
            cf.setLocation(uri.toString());
        }

        metsMods.setDigitalDocument(digitalDocument);

        /*
         * wenn Filegroups definiert wurden, werden diese jetzt in die
         * Metsstruktur übernommen
         */
        // Replace all paths with the given VariableReplacer, also the file
        // group paths!
        VariableReplacer vp = new VariableReplacer(metsMods.getDigitalDocument(), prefs, process, null);
        List<ProjectFileGroup> fileGroups = process.getProject().getProjectFileGroups();

        if (fileGroups != null && fileGroups.size() > 0) {
            for (ProjectFileGroup pfg : fileGroups) {
                // check if source files exists
                if (pfg.getFolder() != null && pfg.getFolder().length() > 0) {
                    URI folder = serviceManager.getProcessService().getMethodFromName(pfg.getFolder(), process);
                    if (serviceManager.getFileService().fileExist(folder)
                            && serviceManager.getFileService().getSubUris(folder).size() > 0) {
                        metsMods.getDigitalDocument().getFileSet().addVirtualFileGroup(setVirtualFileGroup(pfg, vp));
                    }
                } else {
                    metsMods.getDigitalDocument().getFileSet().addVirtualFileGroup(setVirtualFileGroup(pfg, vp));
                }
            }
        }

        // Replace rights and digiprov entries.
        metsMods.setRightsOwner(vp.replace(process.getProject().getMetsRightsOwner()));
        metsMods.setRightsOwnerLogo(vp.replace(process.getProject().getMetsRightsOwnerLogo()));
        metsMods.setRightsOwnerSiteURL(vp.replace(process.getProject().getMetsRightsOwnerSite()));
        metsMods.setRightsOwnerContact(vp.replace(process.getProject().getMetsRightsOwnerMail()));
        metsMods.setDigiprovPresentation(vp.replace(process.getProject().getMetsDigiprovPresentation()));
        metsMods.setDigiprovReference(vp.replace(process.getProject().getMetsDigiprovReference()));
        metsMods.setDigiprovPresentationAnchor(vp.replace(process.getProject().getMetsDigiprovPresentationAnchor()));
        metsMods.setDigiprovReferenceAnchor(vp.replace(process.getProject().getMetsDigiprovReferenceAnchor()));

        metsMods.setPurlUrl(vp.replace(process.getProject().getMetsPurl()));
        metsMods.setContentIDs(vp.replace(process.getProject().getMetsContentIDs()));

        // Set mets pointers. MetsPointerPathAnchor or mptrAnchorUrl is the
        // pointer used to point to the superordinate (anchor) file, that is
        // representing a “virtual” group such as a series. Several anchors
        // pointer paths can be defined/ since it is possible to define several
        // levels of superordinate structures (such as the complete edition of
        // a daily newspaper, one year ouf of that edition, …)
        String anchorPointersToReplace = process.getProject().getMetsPointerPath();
        metsMods.setMptrUrl(null);
        for (String anchorPointerToReplace : anchorPointersToReplace.split(Project.ANCHOR_SEPARATOR)) {
            String anchorPointer = vp.replace(anchorPointerToReplace);
            metsMods.setMptrUrl(anchorPointer);
        }

        // metsPointerPathAnchor or mptrAnchorUrl is the pointer used to point
        // from the (lowest) superordinate
        // (anchor) file to the lowest level file (the non-anchor file).
        String metsPointerToReplace = process.getProject().getMetsPointerPathAnchor();
        String metsPointer = vp.replace(metsPointerToReplace);
        metsMods.setMptrAnchorUrl(metsPointer);

        if (ConfigCore.getBooleanParameter("ExportValidateImages", true)) {
            try {
                // TODO andere Dateigruppen nicht mit image Namen ersetzen
                List<URI> images = new MetadatenImagesHelper(prefs, digitalDocument).getDataFiles(process);
                List<String> imageStrings = new ArrayList<>();
                for (URI uri : images) {
                    imageStrings.add(uri.toString());
                }
                int sizeOfPagination = digitalDocument.getPhysicalDocStruct().getAllChildren().size();
                if (images.size() > 0) {
                    int sizeOfImages = images.size();
                    if (sizeOfPagination == sizeOfImages) {
                        digitalDocument.overrideContentFiles(imageStrings);
                    } else {
                        List<String> param = new ArrayList<>();
                        param.add(String.valueOf(sizeOfPagination));
                        param.add(String.valueOf(sizeOfImages));
                        Helper.setFehlerMeldung(Helper.getTranslation("imagePaginationError", param));
                        return null;
                    }
                }
            } catch (IndexOutOfBoundsException | InvalidImagesException e) {
                logger.error(e);
                return null;
            }
        } else {
            // create pagination out of virtual file names
            digitalDocument.addAllContentFiles();
        }
        return metsMods;
    }

    private VirtualFileGroup setVirtualFileGroup(ProjectFileGroup projectFileGroup, VariableReplacer variableReplacer) {
        VirtualFileGroup virtualFileGroup = new VirtualFileGroup();

        virtualFileGroup.setName(projectFileGroup.getName());
        virtualFileGroup.setPathToFiles(variableReplacer.replace(projectFileGroup.getPath()));
        virtualFileGroup.setMimetype(projectFileGroup.getMimeType());
        virtualFileGroup.setFileSuffix(projectFileGroup.getSuffix());
        virtualFileGroup.setOrdinary(!projectFileGroup.isPreviewImage());

        return virtualFileGroup;
    }
}
