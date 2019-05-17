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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;

import javax.faces.model.SelectItem;

import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.metadata.InsertionPosition;
import org.kitodo.production.metadata.MetadataEditor;

public class AddMediaUnitDialog {
    private final DataEditorForm dataEditor;
    private List<SelectItem> possiblePositions;
    private List<SelectItem> possibleTypes;
    private InsertionPosition selectedPosition;
    private String selectedType;


    /**
     * Constructor.
     * @param dataEditor Instance of DataEditorForm where this instance of AddMediaUnitDialog was created.
     */
    AddMediaUnitDialog(DataEditorForm dataEditor) {
        this.dataEditor = dataEditor;
    }

    /**
     * Add a new MediaUnit.
     */
    public void addMediaUnit() {
        if (dataEditor.getSelectedMediaUnit().isPresent()) {
            MetadataEditor.addMediaUnit(selectedType,
                    dataEditor.getSelectedMediaUnit().get(),
                    selectedPosition);
            dataEditor.refreshStructurePanel();
        } else {
            Helper.setErrorMessage("No media unit selected!");
        }
    }

    void prepare() {
        if (dataEditor.getSelectedMediaUnit().isPresent()) {
            preparePossiblePositions();
            preparePossibleTypes();
        } else {
            possiblePositions = Collections.emptyList();
            possibleTypes = Collections.emptyList();
        }
    }

    private void preparePossiblePositions() {
        possiblePositions = new ArrayList<>();
        possiblePositions.add(new SelectItem(InsertionPosition.FIRST_CHILD_OF_CURRENT_ELEMENT,
                Helper.getTranslation("asFirstChildOfCurrentElement")));
        possiblePositions.add(new SelectItem(InsertionPosition.LAST_CHILD_OF_CURRENT_ELEMENT,
                Helper.getTranslation("asLastChildOfCurrentElement")));
    }

    private void preparePossibleTypes() {
        possibleTypes = new ArrayList<>();
        // TODO does this work for MediaUnit?
        String mediaUnitType = dataEditor.getSelectedMediaUnit().orElseThrow(IllegalStateException::new).getType();
        if (Objects.nonNull(mediaUnitType)) {
            StructuralElementViewInterface divisionView = dataEditor.getRuleset().getStructuralElementView(
                mediaUnitType, dataEditor.getAcquisitionStage(), dataEditor.getPriorityList());
            for (Entry<String, String> entry : divisionView.getAllowedSubstructuralElements().entrySet()) {
                possibleTypes.add(new SelectItem(entry.getKey(), entry.getValue()));
            }
        }
    }

    /**
     * Get possiblePositions.
     *
     * @return value of possiblePositions
     */
    public List<SelectItem> getPossiblePositions() {
        return possiblePositions;
    }

    /**
     * Return the possible types of structural elements at the selected position.
     * @return List of possible types
     */
    public List<SelectItem> getPossibleTypes() {
        return possibleTypes;
    }

    /**
     * Get selectedPosition.
     *
     * @return value of selectedPosition
     */
    public InsertionPosition getSelectedPosition() {
        return selectedPosition;
    }

    /**
     * Set selectedPosition.
     *
     * @param selectedPosition as org.kitodo.production.metadata.InsertionPosition
     */
    public void setSelectedPosition(InsertionPosition selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    /**
     * Get selectedType.
     *
     * @return value of selectedType
     */
    public String getSelectedType() {
        return selectedType;
    }

    /**
     * Set selectedType.
     *
     * @param selectedType as java.lang.String
     */
    public void setSelectedType(String selectedType) {
        this.selectedType = selectedType;
    }
}
