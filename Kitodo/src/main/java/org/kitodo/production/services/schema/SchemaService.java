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

import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import org.kitodo.api.MdSec;
import org.kitodo.api.dataformat.mets.FLocatXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.FileXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.MetadataXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.MetsXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.UseXmlAttributeAccessInterface;
import org.kitodo.api.ugh.PrefsInterface;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.LinkingMode;
import org.kitodo.data.database.beans.Process;
import org.kitodo.export.ExportMets;
import org.kitodo.production.helper.VariableReplacer;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyInnerPhysicalDocStructHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetsModsDigitalDocumentHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyPrefsHelper;
import org.kitodo.production.model.Subfolder;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.dataformat.MetsService;

/**
 * Service for schema manipulations.
 */
public class SchemaService {

    private static final MetsService METS_SERVICE = ServiceManager.getMetsService();
    private static final UseXmlAttributeAccessInterface LOCAL = new LegacyInnerPhysicalDocStructHelper().local;
    /**
     * Temporal method for separate file conversion from ExportMets class
     * (method writeMetsFile).
     *
     * @param workpiece
     *            class inside method is used
     * @param exportMets
     *            MetsModsImportExport object
     * @param prefs
     *            preferences - Prefs object
     * @param process
     *            object
     */
    public <T extends ExportMets> void tempConvert(MetsXmlElementAccessInterface workpiece, T exportMets,
            PrefsInterface prefs, Process process) {
        /*
         * wenn Filegroups definiert wurden, werden diese jetzt in die
         * Metsstruktur übernommen
         */
        // Replace all paths with the given VariableReplacer, also the file
        // group paths!
        VariableReplacer vp = new VariableReplacer(
                new LegacyMetsModsDigitalDocumentHelper(((LegacyPrefsHelper) prefs).getRuleset(), workpiece), prefs,
                process, null);

        addVirtualFileGroupsToMetsMods(workpiece, process);

        // Replace rights and digiprov entries.
        set(workpiece, MdSec.RIGHTS_MD, "owner", vp.replace(process.getProject().getMetsRightsOwner()));
        set(workpiece, MdSec.RIGHTS_MD, "ownerLogo", vp.replace(process.getProject().getMetsRightsOwnerLogo()));
        set(workpiece, MdSec.RIGHTS_MD, "ownerSiteURL", vp.replace(process.getProject().getMetsRightsOwnerSite()));
        set(workpiece, MdSec.RIGHTS_MD, "ownerContact", vp.replace(process.getProject().getMetsRightsOwnerMail()));
        set(workpiece, MdSec.DIGIPROV_MD, "presentation",
            vp.replace(process.getProject().getMetsDigiprovPresentation()));
        set(workpiece, MdSec.DIGIPROV_MD, "reference", vp.replace(process.getProject().getMetsDigiprovReference()));
        set(workpiece, MdSec.DIGIPROV_MD, "presentationAnchor",
            vp.replace(process.getProject().getMetsDigiprovPresentationAnchor()));
        set(workpiece, MdSec.DIGIPROV_MD, "referenceAnchor",
            vp.replace(process.getProject().getMetsDigiprovReferenceAnchor()));

        set(workpiece, MdSec.TECH_MD, "purlUrl", vp.replace(process.getProject().getMetsPurl()));
        set(workpiece, MdSec.TECH_MD, "contentIDs", vp.replace(process.getProject().getMetsContentIDs()));

    }

    private void set(MetsXmlElementAccessInterface workpiece, MdSec domain, String key, String value) {
        MetadataXmlElementAccessInterface entry = METS_SERVICE.createMetadataXmlElementAccess();
        entry.setType(key);
        entry.setDomain(domain);
        entry.setValue(value);
        workpiece.getStructMap().getMetadata().add(entry);

    }

    private void addVirtualFileGroupsToMetsMods(MetsXmlElementAccessInterface workpiece, Process process) {

        List<Folder> folders = process.getProject().getFolders();
        Subfolder useLocalSubfolder = getUseLocalSubfolder(process);

        for (FileXmlElementAccessInterface mediaUnit : workpiece.getFileGrp()) {
            String canonical = useLocalSubfolder.getCanonical(mediaUnit.getFLocatForUse(LOCAL).getUri());

            /*
             * If the media unit contains a media variant that is unknown, has
             * linking mode NO or has linking mode EXISTING but the file does
             * not exist, remove it.
             */
            for (Entry<? extends UseXmlAttributeAccessInterface, ? extends FLocatXmlElementAccessInterface> mediaFileForMediaVariant
                    : mediaUnit.getAllUsesWithFLocats()) {
                String use = mediaFileForMediaVariant.getKey().getUse();
                Optional<Folder> optionalFolderForUse = folders.parallelStream()
                        .filter(folder -> use.equals(folder.getFileGroup())).findAny();
                if (!optionalFolderForUse.isPresent()
                        || optionalFolderForUse.get().getLinkingMode().equals(LinkingMode.NO)
                        || (optionalFolderForUse.get().getLinkingMode().equals(LinkingMode.EXISTING)
                                && new Subfolder(process, optionalFolderForUse.get()).getURIIfExists(canonical)
                                        .isPresent())) {
                    mediaUnit.removeFLocatForUse(mediaFileForMediaVariant.getKey());
                }
            }

            /*
             * If the media unit is missing a variant that has linking mode ALL
             * or has linking mode EXISTING and the file does exist, add it.
             */
            for (Folder folder : folders) {
                Subfolder useFolder = new Subfolder(process, folder);
                if (!mediaUnit.getAllUsesWithFLocats().parallelStream().map(Entry::getKey)
                        .map(UseXmlAttributeAccessInterface::getUse)
                        .anyMatch(use -> use.equals(folder.getFileGroup()))) {
                    if ((folder.getLinkingMode().equals(LinkingMode.ALL)
                            || (folder.getLinkingMode().equals(LinkingMode.EXISTING)
                                    && useFolder.getURIIfExists(canonical).isPresent()))) {
                        addUse(useFolder, canonical, mediaUnit);
                    }
                }
            }
        }
    }

    /**
     * Returns the USE="LOCAL" subfolder.
     * 
     * @param process
     *            process whose USE="LOCAL" subfolder shall be returned
     * @return the subfolder with USE="LOCAL"
     */
    private Subfolder getUseLocalSubfolder(Process process) {
        Folder useLocalFolder = process.getProject().getGeneratorSource();
        if (useLocalFolder == null) {
            Optional<Folder> optionalUseLocalFolderByName = process.getProject().getFolders().parallelStream()
                    .filter(folder -> folder.getFileGroup().equals("LOCAL")).findAny();
            if (optionalUseLocalFolderByName.isPresent()) {
                useLocalFolder = optionalUseLocalFolderByName.get();
            }
        }
        Subfolder useLocalSubfolder = new Subfolder(process, useLocalFolder);
        return useLocalSubfolder;
    }

    /**
     * Adds a use to a media unit.
     * 
     * @param useFolder
     *            use folder for the use
     * @param canonical
     *            the canonical part of the file name of the media file
     * @param mediaUnit
     *            media unit to add to
     */
    private void addUse(Subfolder useFolder, String canonical, FileXmlElementAccessInterface mediaUnit) {
        UseXmlAttributeAccessInterface mediaVariant = METS_SERVICE.createUseXmlAttributeAccess();
        mediaVariant.setUse(useFolder.getFolder().getFileGroup());
        mediaVariant.setMimeType(useFolder.getFolder().getMimeType());
        FLocatXmlElementAccessInterface mediaFile = METS_SERVICE.createFLocatXmlElementAccess();
        mediaFile.setUri(useFolder.getUri(canonical));
        mediaUnit.putFLocatForUse(mediaVariant, mediaFile);
    }
}
