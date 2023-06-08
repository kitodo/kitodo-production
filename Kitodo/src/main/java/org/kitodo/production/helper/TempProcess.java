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

package org.kitodo.production.helper;

import java.io.IOException;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.kitodo.api.MdSec;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.data.database.beans.Process;
import org.kitodo.exceptions.ProcessGenerationException;
import org.kitodo.production.forms.createprocess.ProcessMetadata;
import org.kitodo.production.services.data.ImportService;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class is used during import.
 */
public class TempProcess {

    private Workpiece workpiece;

    private Process process;

    private NodeList metadataNodes;

    private String atstsl;

    private String tiffHeaderDocumentName;

    private String tiffHeaderImageDescription;

    private int guessedImages;

    private final ProcessMetadata processMetadata;
    private String catalogId = "";

    /**
     * Constructor that creates an instance of TempProcess with the given Process
     * and Workpiece instances.
     *
     * @param process Process of this TempProcess
     * @param workpiece Workpiece of this TempProcess
     */
    public TempProcess(Process process, Workpiece workpiece) {
        this.process = process;
        this.workpiece = workpiece;
        this.processMetadata = new ProcessMetadata();
    }

    /**
     * Constructor that creates an instance of TempProcess with given Process
     * and metadata NodeList.
     *
     * @param process Process of this TempProcess
     * @param nodeList Metadata NodeList of this TempProcess
     * @param docType Document type of process
     */
    public TempProcess(Process process, NodeList nodeList, String docType) {
        this.process = process;
        this.metadataNodes = nodeList;
        this.workpiece = new Workpiece();
        this.workpiece.getLogicalStructure().setType(docType);
        if (nodeList.getLength() != 0) {
            this.workpiece.getLogicalStructure().getMetadata().addAll(
                    ProcessHelper.convertMetadata(this.metadataNodes, MdSec.DMD_SEC));
        }
        this.processMetadata = new ProcessMetadata();
    }

    /**
     * Get workpiece.
     *
     * @return workpiece
     */
    public Workpiece getWorkpiece() {
        return workpiece;
    }

    /**
     * Set workpiece.
     *
     * @param workpiece new Workpiece
     */
    public void setWorkpiece(Workpiece workpiece) {
        this.workpiece = workpiece;
    }

    /**
     * Get process.
     *
     * @return process
     */
    public Process getProcess() {
        return process;
    }

    /**
     * Set process.
     *
     * @param process new Process
     */
    public void setProcess(Process process) {
        this.process = process;
    }

    /**
     * Get metadataNodes.
     *
     * @return value of metadataNodes
     */
    public NodeList getMetadataNodes() {
        return metadataNodes;
    }

    /**
     * Get tiffHeaderDocumentName.
     *
     * @return value of tiffHeaderDocumentName
     */
    public String getTiffHeaderDocumentName() {
        return tiffHeaderDocumentName;
    }

    /**
     * Set tiffHeaderDocumentName.
     *
     * @param tiffHeaderDocumentName as java.lang.String
     */
    public void setTiffHeaderDocumentName(String tiffHeaderDocumentName) {
        this.tiffHeaderDocumentName = tiffHeaderDocumentName;
    }

    /**
     * Get tiffHeaderImageDescription.
     *
     * @return value of tiffHeaderImageDescription
     */
    public String getTiffHeaderImageDescription() {
        return tiffHeaderImageDescription;
    }

    /**
     * Set tiffHeaderImageDescription.
     *
     * @param tiffHeaderImageDescription as java.lang.String
     */
    public void setTiffHeaderImageDescription(String tiffHeaderImageDescription) {
        this.tiffHeaderImageDescription = tiffHeaderImageDescription;
    }

    /**
     * Get guessedImages.
     *
     * @return value of guessedImages
     */
    public int getGuessedImages() {
        return guessedImages;
    }

    /**
     * Set guessedImages.
     *
     * @param guessedImages as int
     */
    public void setGuessedImages(int guessedImages) {
        this.guessedImages = guessedImages;
    }

    /**
     * Get processMetadata.
     *
     * @return value of processMetadata
     */
    public ProcessMetadata getProcessMetadata() {
        return processMetadata;
    }

    /**
     * Verify the doc type of the process. This Method checks whether the process has a metadata
     * of type "docType" and if its value equals the type of the logical root element. If not, the
     * logical root is set to the value of the "docType" metadata.
     *
     * <p>
     * This function is currently only used for the import of prestructured processes.
     */
    public void verifyDocType() throws IOException, ProcessGenerationException {
        if (Objects.nonNull(process.getRuleset())) {
            Collection<String> doctypeMetadata = ImportService.getDocTypeMetadata(process.getRuleset());
            if (doctypeMetadata.isEmpty()) {
                throw new ProcessGenerationException(Helper.getTranslation("newProcess.docTypeMetadataMissing",
                        process.getRuleset().getTitle()));
            }
            if (Objects.nonNull(this.getWorkpiece().getLogicalStructure().getMetadata())) {
                Optional<Metadata> docTypeMetadata = this.getWorkpiece().getLogicalStructure().getMetadata()
                        .stream().filter(metadata -> doctypeMetadata.contains(metadata.getKey())).findFirst();
                if (docTypeMetadata.isPresent() && docTypeMetadata.get() instanceof MetadataEntry) {
                    String docType = ((MetadataEntry)docTypeMetadata.get()).getValue();
                    if (StringUtils.isNotBlank(docType)
                            && !this.getWorkpiece().getLogicalStructure().getType().equals(docType)) {
                        this.getWorkpiece().getLogicalStructure().setType(docType);
                    }
                }
            }
        }
    }

    /**
     * Get atstsl.
     *
     * @return value of atstsl
     */
    public String getAtstsl() {
        return atstsl;
    }

    /**
     * Set atstsl.
     *
     * @param atstsl as string
     */
    public void setAtstsl(String atstsl) {
        this.atstsl = atstsl;
    }

    /**
     * Get catalog ID of this temp process.
     *
     * @param identifierMetadataKeys Collection of metadata keys of identifier metadata
     * @return catalog ID
     */
    public String getCatalogId(Collection<String> identifierMetadataKeys) {
        for (String identifierMetadata : identifierMetadataKeys) {
            if (catalogId.isEmpty() && Objects.nonNull(metadataNodes) && metadataNodes.getLength() > 0) {
                for (int i = 0; i < metadataNodes.getLength(); i++) {
                    Node item = metadataNodes.item(i);
                    Node name = item.getAttributes().getNamedItem("name");
                    if (Objects.nonNull(name) && name.getTextContent().equals(identifierMetadata)) {
                        catalogId = item.getTextContent();
                        break;
                    }
                }
            }
        }
        if (catalogId.isEmpty()) {
            catalogId = " - ";
        }
        return catalogId;
    }
}
