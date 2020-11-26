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

import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.dataformat.IncludedStructuralElement;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.data.database.beans.Process;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetsModsDigitalDocumentHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class DeleteDataScript extends EditDataScript {

    public void executeScript(LegacyMetsModsDigitalDocumentHelper metadataFile, Process process,
                               MetadataScript metadataScript) {
        Workpiece workpiece = metadataFile.getWorkpiece();
        List<IncludedStructuralElement> allIncludedStructuralElements = workpiece.getAllIncludedStructuralElements();

        IncludedStructuralElement child = allIncludedStructuralElements.get(0);
        Collection<Metadata> metadataCollection = child.getMetadata();

        if (Objects.isNull(metadataScript.getValue())) {
            generateValueForMetadataScript(metadataScript, metadataCollection);
        }

        List<Metadata> metadataCollectionCopy = new ArrayList<>();
        metadataCollectionCopy.addAll(metadataCollection);

        for (Metadata metadata : metadataCollectionCopy) {
            if (metadata.getKey().equals(metadataScript.getGoal())) {
                if (Objects.isNull(metadataScript.getValue())) {
                    metadataCollection.remove(metadata);
                } else if (metadataScript.getValue().equals(((MetadataEntry) metadata).getValue())) {
                    metadataCollection.remove(metadata);
                }
            }
        }
        saveChanges(workpiece, process);

    }


}
