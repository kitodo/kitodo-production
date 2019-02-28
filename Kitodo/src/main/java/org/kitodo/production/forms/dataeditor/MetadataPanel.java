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

package org.kitodo.production.forms.dataeditor;

import java.util.List;

import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.api.dataformat.Structure;

public class MetadataPanel {

    private final DataEditorForm dataEditor;

    private FieldedMetadataTableRow metadataTable = FieldedMetadataTableRow.EMPTY;

    public MetadataPanel(DataEditorForm dataEditor) {
        this.dataEditor = dataEditor;
    }

    public List<MetadataTableRow> getRows() {
        return metadataTable.getRows();
    }

    public void show(Structure structure) {
        if (structure == null) {
            metadataTable = FieldedMetadataTableRow.EMPTY;
        } else {
            StructuralElementViewInterface divisionView = dataEditor.getRuleset().getStructuralElementView(
                structure.getType(), dataEditor.getAcquisitionStage(), dataEditor.getPriorityList());
            metadataTable = new FieldedMetadataTableRow(dataEditor, structure, divisionView);
        }

    }

    void preserve() throws InvalidMetadataValueException, NoSuchMetadataFieldException {
        metadataTable.preserve();
    }
}
