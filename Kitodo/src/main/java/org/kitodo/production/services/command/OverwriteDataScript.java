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

import org.kitodo.api.MdSec;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.MetadataGroup;
import org.kitodo.api.dataformat.IncludedStructuralElement;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.data.database.beans.Process;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetsModsDigitalDocumentHelper;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class OverwriteDataScript extends EditDataScript {

    @Override
    public void executeScript(LegacyMetsModsDigitalDocumentHelper metadataFile, Process process,
            MetadataScript metadataScript) {
        Workpiece workpiece = metadataFile.getWorkpiece();
        List<IncludedStructuralElement> allIncludedStructuralElements = workpiece
                .getAllIncludedStructuralElements();

        IncludedStructuralElement child = allIncludedStructuralElements.get(0);
        Collection<Metadata> metadata = child.getMetadata();
        if (Objects.isNull(metadataScript.getValue())) {
            generateValueForMetadataScript(metadataScript, metadata);
        }
        for (Metadata metadatum : metadata) {
            if (metadatum instanceof MetadataEntry) {
                if (metadatum.getKey().equals(metadataScript.getGoal())) {
                    ((MetadataEntry) metadatum).setValue(metadataScript.getValue());
                    break;
                }
            }
            if (metadatum instanceof MetadataGroup) {
                Collection<Metadata> group = ((MetadataGroup) metadatum).getGroup();
                if (metadatum.getKey().equals(metadataScript.getGoal())) {
                    // TODO: implement handling of metadataGroups
                }
                for (Metadata groupelement : group) {
                }
            }
        }
        saveChanges(workpiece, process);
    }
}
