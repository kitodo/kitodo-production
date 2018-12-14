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

package org.kitodo.production.services.schema;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.ugh.ContentFileInterface;
import org.kitodo.api.ugh.DigitalDocumentInterface;
import org.kitodo.api.ugh.DocStructInterface;
import org.kitodo.api.ugh.FileformatInterface;
import org.kitodo.api.ugh.MetsModsImportExportInterface;
import org.kitodo.api.ugh.PrefsInterface;
import org.kitodo.api.ugh.VirtualFileGroupInterface;
import org.kitodo.api.ugh.exceptions.PreferencesException;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.config.xml.fileformats.FileFormatsConfig;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.LinkingMode;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.exceptions.InvalidImagesException;
import org.kitodo.export.ExportDms;
import org.kitodo.export.ExportMets;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.VariableReplacer;
import org.kitodo.production.helper.metadata.ImageHelper;
import org.kitodo.production.legacy.UghImplementation;
import org.kitodo.production.services.ServiceManager;

/**
 * Service for schema manipulations.
 */
public class SchemaService {

    private static final Logger logger = LogManager.getLogger(SchemaService.class);
    private List<DocStructInterface> docStructsWithoutPages = new ArrayList<>();

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
    public <T extends ExportMets> MetsModsImportExportInterface tempConvert(FileformatInterface gdzfile, T exportMets,
            MetsModsImportExportInterface metsMods, PrefsInterface prefs, Process process)
            throws IOException, PreferencesException, JAXBException {
        URI imageFolder = ServiceManager.getFileService().getImagesDirectory(process);

        /*
         * before creating mets file, change relative path to absolute -
         */
        DigitalDocumentInterface digitalDocument = gdzfile.getDigitalDocument();
        if (digitalDocument.getFileSet() == null) {
            Helper.setMessage(process.getTitle()
                    + ": digital document does not contain images; temporarily adding them for mets file creation");
            ImageHelper mih = new ImageHelper(prefs, digitalDocument);
            mih.createPagination(process, null);
        }

        /*
         * get the topstruct element of the digital document depending on anchor
         * property
         */
        DocStructInterface topElement = digitalDocument.getLogicalDocStruct();
        if (prefs.getDocStrctTypeByName(topElement.getDocStructType().getName()).getAnchorClass() != null) {
            if (topElement.getAllChildren() == null || topElement.getAllChildren().isEmpty()) {
                throw new PreferencesException(process.getTitle()
                        + ": the topstruct element is marked as anchor, but does not have any children for "
                        + "physical docstrucs");
            } else {
                topElement = topElement.getAllChildren().get(0);
            }
        }

        // if the top element does not have any image related, set them all
        if (topElement.getAllToReferences("logical_physical") == null
                || topElement.getAllToReferences("logical_physical").isEmpty()) {
            if (digitalDocument.getPhysicalDocStruct() != null
                    && digitalDocument.getPhysicalDocStruct().getAllChildren() != null) {
                Helper.setMessage(process.getTitle()
                        + ": topstruct element does not have any referenced images yet; temporarily adding them "
                        + "for mets file creation");
                for (DocStructInterface mySeitenDocStruct : digitalDocument.getPhysicalDocStruct().getAllChildren()) {
                    topElement.addReferenceTo(mySeitenDocStruct, "logical_physical");
                }
            } else {
                if (exportMets instanceof ExportDms && ((ExportDms) exportMets).getExportDmsTask() != null) {
                    ((ExportDms) exportMets).getExportDmsTask().setException(new RuntimeException(
                            process.getTitle() + ": could not find any referenced images, export aborted"));
                } else {
                    Helper.setErrorMessage(
                        process.getTitle() + ": could not find any referenced images, export aborted");
                }
                return null;
            }
        }

        for (ContentFileInterface cf : digitalDocument.getFileSet().getAllFiles()) {
            String location = cf.getLocation();
            // If the file's location string shows no sign of any protocol,
            // use the file protocol.
            if (!location.contains("://")) {
                location = "file://" + location;
            }
            cf.setLocation(location);
        }

        metsMods.setDigitalDocument(digitalDocument);

        /*
         * wenn Filegroups definiert wurden, werden diese jetzt in die
         * Metsstruktur übernommen
         */
        // Replace all paths with the given VariableReplacer, also the file
        // group paths!
        VariableReplacer vp = new VariableReplacer(metsMods.getDigitalDocument(), prefs, process, null);

        addVirtualFileGroupsToMetsMods(metsMods, process, vp);

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

        if (ConfigCore.getBooleanParameterOrDefaultValue(ParameterCore.EXPORT_VALIDATE_IMAGES)) {
            if (containsInvalidImages(prefs, digitalDocument, process)) {
                return null;
            }
        } else {
            // create pagination out of virtual file names
            digitalDocument.addAllContentFiles();
        }

        metsMods.setDigitalDocument(digitalDocument);
        return metsMods;
    }

    private MetsModsImportExportInterface addVirtualFileGroupsToMetsMods(MetsModsImportExportInterface metsMods,
            Process process, VariableReplacer variableReplacer)
            throws PreferencesException, JAXBException {
        List<Folder> folders = process.getProject().getFolders();
        for (Folder folder : folders) {
            // check if source files exists
            if (folder.getLinkingMode().equals(LinkingMode.EXISTING)) {
                URI folderUri = ServiceManager.getProcessService().getMethodFromName(folder.getRelativePath(), process);
                if (ServiceManager.getFileService().fileExist(folderUri)
                        && !ServiceManager.getFileService().getSubUris(folderUri).isEmpty()) {
                    metsMods.getDigitalDocument().getFileSet()
                            .addVirtualFileGroup(setVirtualFileGroup(folder, variableReplacer));
                }
            } else if (!folder.getLinkingMode().equals(LinkingMode.NO)) {
                metsMods.getDigitalDocument().getFileSet()
                        .addVirtualFileGroup(setVirtualFileGroup(folder, variableReplacer));
            }
        }
        return metsMods;
    }

    private VirtualFileGroupInterface setVirtualFileGroup(Folder folder, VariableReplacer variableReplacer)
            throws JAXBException {
        VirtualFileGroupInterface virtualFileGroup = UghImplementation.INSTANCE.createVirtualFileGroup();

        virtualFileGroup.setName(folder.getFileGroup());
        virtualFileGroup.setPathToFiles(variableReplacer.replace(folder.getUrlStructure()));
        virtualFileGroup.setMimetype(folder.getMimeType());
        if (FileFormatsConfig.getFileFormat(folder.getMimeType()).isPresent()) {
            virtualFileGroup.setFileSuffix(FileFormatsConfig.getFileFormat(folder.getMimeType()).get().getExtension(false));
        }
        virtualFileGroup.setOrdinary(!folder.getLinkingMode().equals(LinkingMode.PREVIEW_IMAGE));

        return virtualFileGroup;
    }

    private boolean containsInvalidImages(PrefsInterface prefs, DigitalDocumentInterface digitalDocument,
            Process process) {
        try {
            // TODO: do not replace other file groups with image names
            List<URI> images = new ImageHelper(prefs, digitalDocument).getDataFiles(process);
            List<String> imageStrings = new ArrayList<>();
            for (URI uri : images) {
                imageStrings.add(uri.toString());
            }
            int sizeOfPagination = digitalDocument.getPhysicalDocStruct().getAllChildren().size();
            if (!images.isEmpty()) {
                int sizeOfImages = images.size();
                if (sizeOfPagination == sizeOfImages) {
                    digitalDocument.overrideContentFiles(imageStrings);
                } else {
                    Helper.setErrorMessage("imagePaginationError", new Object[] {sizeOfPagination, sizeOfImages });
                    return true;
                }
            }
        } catch (IndexOutOfBoundsException | InvalidImagesException e) {
            logger.error(e.getMessage(), e);
            return true;
        }
        return false;
    }
}
