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

import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.MetadataGroup;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.data.database.beans.Process;
import org.kitodo.exceptions.KitodoScriptExecutionException;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetsModsDigitalDocumentHelper;

public class OverwriteDataScript extends EditDataScript {

    @Override
    public void executeScript(LegacyMetsModsDigitalDocumentHelper metadataFile, Process process,
            MetadataScript metadataScript) throws KitodoScriptExecutionException {
        Workpiece workpiece = metadataFile.getWorkpiece();

        Collection<Metadata> metadataCollection = getMetadataCollection(metadataScript, workpiece);

        generateValueForMetadataScript(metadataScript, metadataCollection, process, metadataFile);

        for (Metadata metadatum : metadataCollection) {
            if (metadatum instanceof MetadataEntry && metadatum.getKey().equals(metadataScript.getMetadataKey())) {
                ((MetadataEntry) metadatum).setValue(metadataScript.getValues().getFirst());
                break;
            }
            if (metadatum instanceof MetadataGroup) {
                Collection<Metadata> group = ((MetadataGroup) metadatum).getMetadata();
                if (metadatum.getKey().equals(metadataScript.getMetadataKey())) {
                    // TODO: implement handling of metadataGroups
                }
                for (Metadata groupelement : group) {
                }
            }
        }
        saveChanges(workpiece, process);
    }
}
