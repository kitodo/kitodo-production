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

package org.kitodo.production.services.command;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.kitodo.api.MdSec;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.dataformat.IncludedStructuralElement;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.data.database.beans.Process;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetsModsDigitalDocumentHelper;

public class AddDataScript extends EditDataScript {

    /**
     * Executes the given script on the given file for the given process.
     * @param metadataFile the file to edit
     * @param process the related process
     * @param metadataScript the script to execute
     */
    public void executeScript(LegacyMetsModsDigitalDocumentHelper metadataFile, Process process,
            MetadataScript metadataScript) {
        Workpiece workpiece = metadataFile.getWorkpiece();

        Collection<Metadata> metadataCollection = workpiece.getRootElement().getMetadata();;
        generateValueForMetadataScript(metadataScript, metadataCollection, process, metadataFile);

        for (String value : metadataScript.getValues()) {
            MetadataEntry metadataEntry = new MetadataEntry();
            metadataEntry.setKey(metadataScript.getGoal());
            metadataEntry.setValue(value);
            metadataEntry.setDomain(MdSec.DMD_SEC);
            metadataCollection.add(metadataEntry);
        }
        saveChanges(workpiece, process);
    }

}
