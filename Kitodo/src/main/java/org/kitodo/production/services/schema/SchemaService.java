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

import org.apache.commons.lang.StringUtils;
import org.kitodo.api.MdSec;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.api.dataformat.MediaUnit;
import org.kitodo.api.dataformat.MediaVariant;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.api.dataformat.mets.LinkedMetsResource;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.enums.LinkingMode;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.helper.VariableReplacer;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyPrefsHelper;
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
     * @param prefs
     *            preferences - Prefs object
     * @param process
     *            object
     */
    public void tempConvert(Workpiece workpiece, LegacyPrefsHelper prefs,
            Process process) throws IOException, DAOException, URISyntaxException {
        /*
         * wenn Filegroups definiert wurden, werden diese jetzt in die
         * Metsstruktur übernommen
         */
        // Replace all paths with the given VariableReplacer, also the file
        // group paths!
        VariableReplacer vp = new VariableReplacer(workpiece, process, null);

        addVirtualFileGroupsToMetsMods(workpiece.getMediaUnit(), process);
        replaceFLocatForExport(workpiece, process, prefs.getRuleset());

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

        convertChildrenLinksForExportRecursive(workpiece, workpiece.getLogicalStructure(), prefs);
        assignViewsFromChildrenRecursive(workpiece.getLogicalStructure());
        enumerateLogicalDivisions(workpiece.getLogicalStructure(), 0, 1, false);
        addLinksToParents(process, prefs, workpiece);
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
        if (StringUtils.isNotEmpty(value)) {
            MetadataEntry entry = new MetadataEntry();
            entry.setKey(key);
            entry.setDomain(domain);
            entry.setValue(value);
            workpiece.getLogicalStructure().getMetadata().add(entry);
        }
    }

    private void addVirtualFileGroupsToMetsMods(MediaUnit mediaUnit, Process process) {
        String canonical = ServiceManager.getFolderService().getCanonical(process, mediaUnit);
        if (Objects.nonNull(canonical)) {
            removeFLocatsForUnwantedUses(process, mediaUnit, canonical);
            addMissingUses(process, mediaUnit, canonical);
        }
        for (MediaUnit child : mediaUnit.getChildren()) {
            addVirtualFileGroupsToMetsMods(child, process);
        }
    }

    private void replaceFLocatForExport(Workpiece workpiece, Process process, RulesetManagementInterface ruleset)
            throws URISyntaxException {
        List<Folder> folders = process.getProject().getFolders();
        VariableReplacer variableReplacer = new VariableReplacer(workpiece, process, null);
        for (MediaUnit mediaUnit : workpiece.getAllMediaUnits()) {
            for (Entry<MediaVariant, URI> mediaFileForMediaVariant : mediaUnit.getMediaFiles().entrySet()) {
                for (Folder folder : folders) {
                    if (folder.getFileGroup().equals(mediaFileForMediaVariant.getKey().getUse())) {
                        int lastSeparator = mediaFileForMediaVariant.getValue().toString().lastIndexOf('/');
                        String lastSegment = mediaFileForMediaVariant.getValue().toString()
                                .substring(lastSeparator + 1);
                        mediaFileForMediaVariant
                                .setValue(new URI(variableReplacer.replace(folder.getUrlStructure() + lastSegment)));
                    }
                }
            }
        }
    }

    /**
     * If the media unit contains a media variant that is unknown, has linking
     * mode NO or has linking mode EXISTING but the file does not exist, remove
     * it.
     */
    private void removeFLocatsForUnwantedUses(Process process,
            MediaUnit mediaUnit,
            String canonical) {
        for (Iterator<Entry<MediaVariant, URI>> mediaFilesForMediaVariants = mediaUnit.getMediaFiles().entrySet()
                .iterator(); mediaFilesForMediaVariants.hasNext();) {
            Entry<MediaVariant, URI> mediaFileForMediaVariant = mediaFilesForMediaVariants.next();
            String use = mediaFileForMediaVariant.getKey().getUse();
            Optional<Folder> optionalFolderForUse = process.getProject().getFolders().parallelStream()
                    .filter(folder -> use.equals(folder.getFileGroup())).findAny();
            if (!optionalFolderForUse.isPresent()
                    || optionalFolderForUse.get().getLinkingMode().equals(LinkingMode.NO)
                    || (optionalFolderForUse.get().getLinkingMode().equals(LinkingMode.EXISTING)
                            && new Subfolder(process, optionalFolderForUse.get()).getURIIfExists(canonical)
                                    .isPresent())) {
                mediaFilesForMediaVariants.remove();
            }
        }
    }

    /**
     * If the media unit is missing a variant that has linking mode ALL or has
     * linking mode EXISTING and the file does exist, add it.
     */
    private void addMissingUses(Process process, MediaUnit mediaUnit,
            String canonical) {
        for (Folder folder : process.getProject().getFolders()) {
            Subfolder useFolder = new Subfolder(process, folder);
            if (mediaUnit.getMediaFiles().entrySet().parallelStream().map(Entry::getKey).map(MediaVariant::getUse)
                    .noneMatch(use -> use.equals(folder.getFileGroup())) && (folder.getLinkingMode().equals(LinkingMode.ALL)
                        || (folder.getLinkingMode().equals(LinkingMode.EXISTING) && useFolder.getURIIfExists(canonical).isPresent()))) {
                addUse(useFolder, canonical, mediaUnit);
            }
        }
    }

    /**
     * Adds a use to a media unit.
     *
     * @param subfolder
     *            subfolder for the use
     * @param canonical
     *            the canonical part of the file name of the media file
     * @param mediaUnit
     *            media unit to add to
     */
    private void addUse(Subfolder subfolder, String canonical, MediaUnit mediaUnit) {
        MediaVariant mediaVariant = new MediaVariant();
        mediaVariant.setUse(subfolder.getFolder().getFileGroup());
        mediaVariant.setMimeType(subfolder.getFolder().getMimeType());
        URI mediaFile = subfolder.getRelativeFilePath(canonical);
        mediaUnit.getMediaFiles().put(mediaVariant, mediaFile);
    }

    /**
     * Replaces internal links in child structure elements with a publicly
     * resolvable link. Checks whether the linked process has not yet been
     * exported, in which case the link from the parental list will be deleted.
     *
     * @param workpiece
     *            current workpiece
     * @param structure
     *            current structure
     * @param prefs
     *            legacy ruleset wrapper
     * @return whether the current structure shall be deleted
     */
    private boolean convertChildrenLinksForExportRecursive(Workpiece workpiece, LogicalDivision structure,
                                               LegacyPrefsHelper prefs) throws DAOException, IOException {

        LinkedMetsResource link = structure.getLink();
        if (Objects.nonNull(link)) {
            int linkedProcessId = processService.processIdFromUri(link.getUri());
            Process process = processService.getById(linkedProcessId);
            if (!process.isExported()) {
                return true;
            }
            setLinkForExport(structure, process, prefs, workpiece);
            copyLabelAndOrderlabel(process, structure);
        }
        for (Iterator<LogicalDivision> iterator = structure.getChildren().iterator(); iterator.hasNext();) {
            if (convertChildrenLinksForExportRecursive(workpiece, iterator.next(), prefs)) {
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

    private void addLinksToParents(Process process, LegacyPrefsHelper prefs, Workpiece workpiece) throws IOException {
        Process parentProcess = process.getParent();
        while (Objects.nonNull(parentProcess)) {
            addParentLinkForExport(prefs, workpiece, parentProcess);
            parentProcess = parentProcess.getParent();
        }
    }

    private void addParentLinkForExport(LegacyPrefsHelper prefs, Workpiece workpiece, Process parent)
            throws IOException {

        LogicalDivision linkHolder = new LogicalDivision();
        linkHolder.setLink(new LinkedMetsResource());
        setLinkForExport(linkHolder, parent, prefs, workpiece);
        linkHolder.getChildren().add(workpiece.getLogicalStructure());
        copyLabelAndOrderlabel(parent, linkHolder);
        workpiece.setLogicalStructure(linkHolder);
    }

    private void setLinkForExport(LogicalDivision structure, Process process, LegacyPrefsHelper prefs,
            Workpiece workpiece) {

        LinkedMetsResource link = structure.getLink();
        link.setLoctype("URL");
        String uriWithVariables = process.getProject().getMetsPointerPath();
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
