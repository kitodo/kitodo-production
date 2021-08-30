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

package org.kitodo.production.metadata.copier;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.MdSec;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.services.ServiceManager;

public class DataCopyrule {

    private static final Logger logger = LogManager.getLogger(DataCopyrule.class);

    List<String> command;

    public DataCopyrule(List<String> command) {
        this.command = command;
    }

    /**
     * When called, the rule must be applied to the given fileformat.
     *
     * @param data
     *            data to apply yourself on
     */
    public void apply(CopierData data) {
        Workpiece workpiece = data.getDigitalDocument().getWorkpiece();
        List<LogicalDivision> allLogicalDivisions = workpiece.getAllLogicalDivisions();

        for (LogicalDivision child : allLogicalDivisions) {
            MdSec domain = null;
            MetadataEntry metadataEntry = new MetadataEntry();
            metadataEntry.setKey(command.get(0));
            metadataEntry.setValue(command.get(2));
            metadataEntry.setDomain(domain);
            child.getMetadata().add(metadataEntry);

            try (OutputStream out = ServiceManager.getFileService()
                    .write(ServiceManager.getFileService().getMetadataFilePath(data.getProcess()))) {
                ServiceManager.getMetsService().save(workpiece, out);
                ServiceManager.getProcessService().saveToIndex(data.getProcess(), false);
            } catch (IOException | CustomResponseException | DataException e) {
                logger.error("Exception while saving Metadata file", e, e.getMessage());
            }
        }
    }

}
