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

import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.data.database.beans.Process;
import org.w3c.dom.NodeList;

/**
 * This class is used during import.
 */
public class TempProcess {

    private static final String DOC_TYPE = "docType";

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
     * This function is currently only used for the import of prestructured processes.
     */
    public void verifyDocType() {
        if (Objects.nonNull(this.getWorkpiece().getRootElement().getMetadata())) {
            Optional<Metadata> docTypeMetadata = this.getWorkpiece().getRootElement().getMetadata()
                    .stream().filter(m -> m.getKey().equals(DOC_TYPE)).findFirst();
            if (docTypeMetadata.isPresent() && docTypeMetadata.get() instanceof MetadataEntry) {
                String docType = ((MetadataEntry)docTypeMetadata.get()).getValue();
                if (StringUtils.isNotBlank(docType)
                        && !this.getWorkpiece().getRootElement().getType().equals(docType)) {
                    this.getWorkpiece().getRootElement().setType(docType);
                }
            }
        }
    }
}
