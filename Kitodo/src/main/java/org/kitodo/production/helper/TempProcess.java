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
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.data.database.beans.Process;
import org.kitodo.exceptions.ProcessGenerationException;
import org.kitodo.production.services.data.ImportService;
import org.w3c.dom.NodeList;

/**
 * This class is used during import.
 */
public class TempProcess {

    private Workpiece workpiece;

    private Process process;

    private NodeList metadataNodes;

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
     * Verify the doc type of the process. This Method checks whether the process has a metadata
     * of type "docType" and if its value equals the type of the logical root element. If not, the
     * logical root is set to the value of the "docType" metadata.
     *
     * <p>This function is currently only used for the import of prestructured processes.</p>
     */
    public void verifyDocType() throws IOException, ProcessGenerationException {
        if (Objects.nonNull(process.getRuleset())) {
            List<String> doctypeMetadata = ImportService.getDocTypeMetadata(process.getRuleset());
            if (doctypeMetadata.isEmpty()) {
                throw new ProcessGenerationException("No doc type metadata defined in ruleset!");
            }
            if (Objects.nonNull(this.getWorkpiece().getLogicalStructure().getMetadata())) {
                Optional<Metadata> docTypeMetadata = this.getWorkpiece().getLogicalStructure().getMetadata()
                        .stream().filter(m -> m.getKey().equals(doctypeMetadata.get(0))).findFirst();
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
}
