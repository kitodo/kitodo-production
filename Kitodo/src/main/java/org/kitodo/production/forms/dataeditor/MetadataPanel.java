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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.faces.model.SelectItem;

import org.kitodo.api.Metadata;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.api.dataformat.Structure;
import org.kitodo.production.helper.Helper;

/**
 * Backing bean for the meta-data panel of the meta-data editor.
 */
public class MetadataPanel implements Serializable {
    private static final long serialVersionUID = 1L;

    private String addMetadataKeySelectedItem = "";

    private String addMetadataValue = "";

    private Collection<Metadata> clipboard = new ArrayList<>();

    private final DataEditorForm dataEditor;

    private FieldedMetadataTableRow metadataTable = FieldedMetadataTableRow.EMPTY;

    public MetadataPanel(DataEditorForm dataEditor) {
        this.dataEditor = dataEditor;
    }

    /**
     * The method is executed when a user clicks the add meta-data button. A new
     * meta-data entry will be created with the entered type and value. Actually
     * this procedure is not in the sense of the inventor. Especially with
     * selection types, the user must enter the coded value, which is
     * inconvenient. Nor can this procedure be transferred to meta-data groups.
     * The better approach would be to first create and display a field and then
     * give the user the option to enter or select the value.
     */
    public void addMetadataEntryClick() {
        try {
            /*
             * First, we get the translated label of the table line into which
             * the value must be entered. It could disappear from the list of
             * addable meta-data if we specify the field as additionally
             * selected and the maximum number of occurrences would be reached.
             * That's why we have to do that first.
             */
            String label = metadataTable.getAddableMetadata().parallelStream()
                    .filter(selectItem -> addMetadataKeySelectedItem.equals(selectItem.getValue())).findAny()
                    .orElseThrow(IllegalStateException::new).getLabel();

            /*
             * Then we add the meta-data to add. This will rebuild the table and
             * create an empty table line (somewhere) into which we can enter
             * the value.
             */
            metadataTable.addAdditionallySelectedField(addMetadataKeySelectedItem);

            /*
             * Now we just have to find the line and enter the value. The latter
             * happens differently depending on what kind of input field it is.
             */
            for (MetadataTableRow row : metadataTable.getRows()) {
                if (label.equals(row.getLabel())) {
                    if (row instanceof TextMetadataTableRow) {
                        TextMetadataTableRow textInput = (TextMetadataTableRow) row;
                        if (textInput.getValue().isEmpty()) {
                            textInput.setValue(addMetadataValue);
                            break;
                        }
                    } else if (row instanceof SelectMetadataTableRow) {
                        SelectMetadataTableRow selectInput = (SelectMetadataTableRow) row;
                        if (selectInput.getSelectedItem().isEmpty()) {
                            selectInput.setSelectedItem(addMetadataValue);
                            break;
                        }
                    } else if (row instanceof BooleanMetadataTableRow) {
                        BooleanMetadataTableRow booleanInput = (BooleanMetadataTableRow) row;
                        if (!booleanInput.isOn()) {
                            booleanInput.setOn(!addMetadataValue.isEmpty());
                        }
                    }
                }
            }
        } catch (Exception e) {
            Helper.setErrorMessage(e.getLocalizedMessage());
        }
    }

    public void clear() {
        metadataTable = FieldedMetadataTableRow.EMPTY;
        clipboard.clear();
        addMetadataKeySelectedItem = "";
        addMetadataValue = "";
    }

    public List<SelectItem> getAddMetadataKeyItems() {
        return metadataTable.getAddableMetadata();
    }

    public String getAddMetadataKeySelectedItem() {
        return addMetadataKeySelectedItem;
    }

    public String getAddMetadataValue() {
        return addMetadataValue;
    }

    Collection<Metadata> getClipboard() {
        return clipboard;
    }

    public List<MetadataTableRow> getRows() {
        return metadataTable.getRows();
    }

    void show(Structure structure) {
        if (structure == null) {
            metadataTable = FieldedMetadataTableRow.EMPTY;
        } else {
            StructuralElementViewInterface divisionView = dataEditor.getRuleset().getStructuralElementView(
                structure.getType(), dataEditor.getAcquisitionStage(), dataEditor.getPriorityList());
            metadataTable = new FieldedMetadataTableRow(this, structure, divisionView);
        }

    }

    public void pasteClick() {
        metadataTable.pasteClick();
    }

    void preserve() throws InvalidMetadataValueException, NoSuchMetadataFieldException {
        metadataTable.preserve();
    }

    public void setAddMetadataKeySelectedItem(String addMetadataKeySelectedItem) {
        this.addMetadataKeySelectedItem = addMetadataKeySelectedItem;
    }

    public void setAddMetadataValue(String addMetadataValue) {
        this.addMetadataValue = addMetadataValue;
    }
}
