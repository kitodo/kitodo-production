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
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.kitodo.api.MdSec;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.api.dataformat.MediaVariant;
import org.kitodo.api.dataformat.PhysicalDivision;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.api.dataformat.mets.LinkedMetsResource;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.enums.LinkingMode;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.helper.VariableReplacer;
import org.kitodo.production.metadata.MetadataEditor;
import org.kitodo.production.model.Subfolder;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ProcessService;
import org.kitodo.production.services.dataformat.MetsService;

/**
 * Service for schema manipulations.
 */
public class SchemaService {
    /**
     * A service that can read METS files.
     */
    private final MetsService metsService = ServiceManager.getMetsService();

    /**
     * A service that can access processes.
     */
    private final ProcessService processService = ServiceManager.getProcessService();

    /**
     * Temporal method for separate file conversion from ExportMets class
     * (method writeMetsFile).
     *
     * @param workpiece
     *            class inside method is used
     * @param process
     *            object
     */
    public void tempConvert(Workpiece workpiece, Process process) throws IOException, DAOException, URISyntaxException {
        /*
         * wenn Filegroups definiert wurden, werden diese jetzt in die
         * Metsstruktur übernommen
         */
        // Replace all paths with the given VariableReplacer, also the file
        // group paths!
        VariableReplacer vp = new VariableReplacer(workpiece, process, null);

        addVirtualFileGroupsToMetsMods(workpiece.getPhysicalStructure(), process);
        replaceFLocatForExport(workpiece, process);

        // Replace rights and digiprov entries.
        set(workpiece, MdSec.RIGHTS_MD, "owner", vp.replace(process.getProject().getMetsRightsOwner()));
        set(workpiece, MdSec.RIGHTS_MD, "ownerLogo", vp.replace(process.getProject().getMetsRightsOwnerLogo()));
        set(workpiece, MdSec.RIGHTS_MD, "ownerSiteURL", vp.replace(process.getProject().getMetsRightsOwnerSite()));
        set(workpiece, MdSec.RIGHTS_MD, "ownerContact", vp.replace(process.getProject().getMetsRightsOwnerMail()));
        set(workpiece, MdSec.DIGIPROV_MD, "presentation",
            vp.replace(process.getProject().getMetsDigiprovPresentation()));
        set(workpiece, MdSec.DIGIPROV_MD, "reference", vp.replace(process.getProject().getMetsDigiprovReference()));

        set(workpiece, MdSec.TECH_MD, "purlUrl", vp.replace(process.getProject().getMetsPurl()));
        set(workpiece, MdSec.TECH_MD, "contentIDs", vp.replace(process.getProject().getMetsContentIDs()));

        convertChildrenLinksForExportRecursive(workpiece.getLogicalStructure());
        assignViewsFromChildrenRecursive(workpiece.getLogicalStructure());
        enumerateLogicalDivisions(workpiece.getLogicalStructure(), 0, 1, false);
        addLinksToParents(process, workpiece);
    }

    /**
     * At all levels, assigns the views of the children to the included
     * structural elements.
     *
     * @param logicalDivision
     *            logical division on which the recursion is
     *            performed
     */
    private void assignViewsFromChildrenRecursive(LogicalDivision logicalDivision) {
        List<LogicalDivision> children = logicalDivision.getChildren();
        if (!children.isEmpty()) {
            for (LogicalDivision child : children) {
                assignViewsFromChildrenRecursive(child);
            }
            if (Objects.nonNull(logicalDivision.getType())) {
                MetadataEditor.assignViewsFromChildren(logicalDivision);
            }
        }
    }

    private void set(Workpiece workpiece, MdSec domain, String key, String value) {
        if (StringUtils.isNotBlank(value)) {
            MetadataEntry entry = new MetadataEntry();
            entry.setKey(key);
            entry.setDomain(domain);
            entry.setValue(value);
            workpiece.getLogicalStructure().getMetadata().add(entry);
        }
    }

    private void addVirtualFileGroupsToMetsMods(PhysicalDivision physicalDivision, Process process) {
        String canonical = ServiceManager.getFolderService().getCanonical(process, physicalDivision);
        if (Objects.nonNull(canonical)) {
            removeFLocatsForUnwantedUses(process, physicalDivision, canonical);
            addMissingUses(process, physicalDivision, canonical);
        }
        for (PhysicalDivision child : physicalDivision.getChildren()) {
            addVirtualFileGroupsToMetsMods(child, process);
        }
    }

    private void replaceFLocatForExport(Workpiece workpiece, Process process)
            throws URISyntaxException {
        List<Folder> folders = process.getProject().getFolders();
        VariableReplacer variableReplacer = new VariableReplacer(workpiece, process, null);
        for (PhysicalDivision physicalDivision : workpiece.getAllPhysicalDivisions()) {
            for (Entry<MediaVariant, URI> mediaFileForMediaVariant : physicalDivision.getMediaFiles().entrySet()) {
                for (Folder folder : folders) {
                    if (folder.getFileGroup().equals(mediaFileForMediaVariant.getKey().getUse())) {
                        String mediaFileWithPath = mediaFileForMediaVariant.getValue().toString(); 
                        String mediaFilename = FilenameUtils.getName(mediaFileWithPath);
                        String mediaFile = variableReplacer.containsFiles(folder.getUrlStructure())
                                ? variableReplacer.replaceWithFilename(folder.getUrlStructure(), mediaFileWithPath)
                                : variableReplacer.replace(folder.getUrlStructure() + mediaFilename);
                        mediaFileForMediaVariant.setValue(new URI(mediaFile));
                    }
                }
            }
        }
    }

    /**
     * If the physical division contains a media variant that is unknown, has linking
     * mode NO or has linking mode EXISTING but the file does not exist, remove
     * it.
     */
    private void removeFLocatsForUnwantedUses(Process process,
            PhysicalDivision physicalDivision,
            String canonical) {
        for (Iterator<Entry<MediaVariant, URI>> mediaFilesForMediaVariants = physicalDivision.getMediaFiles().entrySet()
                .iterator(); mediaFilesForMediaVariants.hasNext();) {
            Entry<MediaVariant, URI> mediaFileForMediaVariant = mediaFilesForMediaVariants.next();
            String use = mediaFileForMediaVariant.getKey().getUse();
            Optional<Folder> optionalFolderForUse = process.getProject().getFolders().parallelStream()
                    .filter(folder -> use.equals(folder.getFileGroup())).findAny();
            if (optionalFolderForUse.isEmpty()
                    || optionalFolderForUse.get().getLinkingMode().equals(LinkingMode.NO)
                    || (optionalFolderForUse.get().getLinkingMode().equals(LinkingMode.EXISTING)
                            && new Subfolder(process, optionalFolderForUse.get()).getURIIfExists(canonical)
                                    .isPresent())) {
                mediaFilesForMediaVariants.remove();
            }
        }
    }

    /**
     * If the physical division is missing a variant that has linking mode ALL or has
     * linking mode EXISTING and the file does exist, add it.
     */
    private void addMissingUses(Process process, PhysicalDivision physicalDivision,
            String canonical) {
        for (Folder folder : process.getProject().getFolders()) {
            Subfolder useFolder = new Subfolder(process, folder);
            if (physicalDivision.getMediaFiles().entrySet().parallelStream().map(Entry::getKey).map(MediaVariant::getUse)
                    .noneMatch(use -> use.equals(folder.getFileGroup())) && (folder.getLinkingMode().equals(LinkingMode.ALL)
                        || (folder.getLinkingMode().equals(LinkingMode.EXISTING) && useFolder.getURIIfExists(canonical).isPresent()))) {
                addUse(useFolder, canonical, physicalDivision);
            }
        }
    }

    /**
     * Adds a use to a physical division.
     *
     * @param subfolder
     *            subfolder for the use
     * @param canonical
     *            the canonical part of the file name of the media file
     * @param physicalDivision
     *            physical division to add to
     */
    private void addUse(Subfolder subfolder, String canonical, PhysicalDivision physicalDivision) {
        MediaVariant mediaVariant = new MediaVariant();
        mediaVariant.setUse(subfolder.getFolder().getFileGroup());
        mediaVariant.setMimeType(subfolder.getFolder().getMimeType());
        URI mediaFile = subfolder.getRelativeFilePath(canonical);
        physicalDivision.getMediaFiles().put(mediaVariant, mediaFile);
    }

    /**
     * Replaces internal links in child structure elements with a publicly
     * resolvable link. Checks whether the linked process has not yet been
     * exported, in which case the link from the parental list will be deleted.
     *
     * @param structure
     *            current structure
     * @return whether the current structure shall be deleted
     */
    private boolean convertChildrenLinksForExportRecursive(LogicalDivision structure) throws DAOException, IOException {

        LinkedMetsResource link = structure.getLink();
        if (Objects.nonNull(link)) {
            int linkedProcessId = processService.processIdFromUri(link.getUri());
            Process process = processService.getById(linkedProcessId);
            if (!process.isExported()) {
                return true;
            }
            setLinkForExport(structure, process);
            copyLabelAndOrderlabel(process, structure);
        }
        for (Iterator<LogicalDivision> iterator = structure.getChildren().iterator(); iterator.hasNext();) {
            if (convertChildrenLinksForExportRecursive(iterator.next())) {
                iterator.remove();
            }
        }
        return false;
    }

    private int enumerateLogicalDivisions(LogicalDivision logicalDivision, int elementCount,
            int journalIssueCount, boolean journalIssue) {

        boolean untyped = Objects.isNull(logicalDivision.getType());
        logicalDivision.setOrder(untyped ? 0 : (journalIssue ? journalIssueCount++ : elementCount));
        for (int i = 0; i < logicalDivision.getChildren().size(); i++) {
            journalIssueCount = enumerateLogicalDivisions(logicalDivision.getChildren().get(i), i + 1,
                journalIssueCount, untyped);
        }
        return journalIssueCount;
    }

    private void addLinksToParents(Process process, Workpiece workpiece) throws IOException {
        Process parentProcess = process.getParent();
        while (Objects.nonNull(parentProcess)) {
            addParentLinkForExport(workpiece, parentProcess);
            parentProcess = parentProcess.getParent();
        }
    }

    private void addParentLinkForExport(Workpiece workpiece, Process parent) throws IOException {
        LogicalDivision linkHolder = new LogicalDivision();
        linkHolder.setLink(new LinkedMetsResource());
        setLinkForExport(linkHolder, parent);
        linkHolder.getChildren().add(workpiece.getLogicalStructure());
        copyLabelAndOrderlabel(parent, linkHolder);
        workpiece.setLogicalStructure(linkHolder);
    }

    private void setLinkForExport(LogicalDivision structure, Process process) throws IOException {

        LinkedMetsResource link = structure.getLink();
        link.setLoctype("URL");
        String uriWithVariables = process.getProject().getMetsPointerPath();
        Workpiece workpiece = uriWithVariables.contains("$(meta.") ? ServiceManager.getMetsService()
                .loadWorkpiece(ServiceManager.getProcessService().getMetadataFileUri(process)) : null;
        VariableReplacer variableReplacer = new VariableReplacer(workpiece, process, null);
        String linkUri = variableReplacer.replace(uriWithVariables);
        link.setUri(URI.create(linkUri));
        structure.setType(ServiceManager.getProcessService().getBaseType(process));
    }

    private void copyLabelAndOrderlabel(Process source, LogicalDivision destination) throws IOException {
        URI sourceMetadataUri = processService.getMetadataFileUri(source);
        LogicalDivision sourceRoot = metsService.loadWorkpiece(sourceMetadataUri).getLogicalStructure();
        if (Objects.isNull(destination.getLabel())) {
            destination.setLabel(sourceRoot.getLabel());
        }
        if (Objects.isNull(destination.getOrderlabel())) {
            destination.setOrderlabel(sourceRoot.getOrderlabel());
        }
    }
}
