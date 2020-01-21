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
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

import javax.faces.model.SelectItem;

import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.api.dataformat.MediaUnit;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.metadata.InsertionPosition;
import org.kitodo.production.metadata.MetadataEditor;

public class AddMediaUnitDialog {
    private final DataEditorForm dataEditor;
    private List<SelectItem> possiblePositions;
    private List<SelectItem> possibleTypes;
    private InsertionPosition selectedPosition = InsertionPosition.LAST_CHILD_OF_CURRENT_ELEMENT;
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
        Optional<MediaUnit> selectedMediaUnit = dataEditor.getSelectedMediaUnit();
        if (selectedMediaUnit.isPresent()) {
            MediaUnit mediaUnit = MetadataEditor.addMediaUnit(selectedType, dataEditor.getWorkpiece(),
                    selectedMediaUnit.get(),
                    selectedPosition);
            dataEditor.refreshStructurePanel();
            dataEditor.getStructurePanel().selectMediaUnit(mediaUnit);
        } else {
            Helper.setErrorMessage("No media unit selected!");
        }
    }

    /**
     * Prepare popup dialog by retrieving available insertion positions and media unit types for selected element.
     */
    public void prepare() {
        if (dataEditor.getSelectedMediaUnit().isPresent()) {
            preparePossiblePositions();
            preparePossibleTypes();
        } else {
            possiblePositions = Collections.emptyList();
            possibleTypes = Collections.emptyList();
        }
    }

    private void preparePossiblePositions() {
        Optional<MediaUnit> selectedMediaUnit = dataEditor.getSelectedMediaUnit();
        if (selectedMediaUnit.isPresent()) {
            possiblePositions = new ArrayList<>();
            possiblePositions.add(new SelectItem(InsertionPosition.FIRST_CHILD_OF_CURRENT_ELEMENT,
                    Helper.getTranslation("dataEditor.position.asFirstChildOfCurrentElement")));
            possiblePositions.add(new SelectItem(InsertionPosition.LAST_CHILD_OF_CURRENT_ELEMENT,
                    Helper.getTranslation("dataEditor.position.asLastChildOfCurrentElement")));
            List<MediaUnit> parents = MetadataEditor.getAncestorsOfMediaUnit(selectedMediaUnit.get(),
                    dataEditor.getWorkpiece().getMediaUnit());
            if (parents.size() > 0) {
                possiblePositions.add(new SelectItem(InsertionPosition.BEFORE_CURRENT_ELEMENT,
                        Helper.getTranslation("dataEditor.position.beforeCurrentElement")));
                possiblePositions.add(new SelectItem(InsertionPosition.AFTER_CURRENT_ELEMENT,
                        Helper.getTranslation("dataEditor.position.afterCurrentElement")));
            }
        }
    }

    /**
     * Update list of available types that can be added to the currently selected media units in the currently selected
     * position.
     */
    public void preparePossibleTypes() {
        possibleTypes = new ArrayList<>();

        Optional<MediaUnit> selectedMediaUnit = dataEditor.getSelectedMediaUnit();
        if (selectedMediaUnit.isPresent()) {
            StructuralElementViewInterface divisionView = null;

            if (InsertionPosition.FIRST_CHILD_OF_CURRENT_ELEMENT.equals(selectedPosition)
                    || InsertionPosition.LAST_CHILD_OF_CURRENT_ELEMENT.equals(selectedPosition)) {
                divisionView = dataEditor.getRuleset().getStructuralElementView(
                    selectedMediaUnit.orElseThrow(IllegalStateException::new).getType(),
                        dataEditor.getAcquisitionStage(),
                        dataEditor.getPriorityList()
                );

            } else if (InsertionPosition.BEFORE_CURRENT_ELEMENT.equals(selectedPosition)
                    || InsertionPosition.AFTER_CURRENT_ELEMENT.equals(selectedPosition)) {
                LinkedList<MediaUnit> parents = MetadataEditor.getAncestorsOfMediaUnit(
                    selectedMediaUnit.get(),
                        dataEditor.getWorkpiece().getMediaUnit());
                if (!parents.isEmpty()) {
                    divisionView = dataEditor.getRuleset().getStructuralElementView(
                            parents.getLast().getType(),
                            dataEditor.getAcquisitionStage(),
                            dataEditor.getPriorityList());
                }
            }
            if (Objects.nonNull(divisionView)) {
                for (Entry<String, String> entry : divisionView.getAllowedSubstructuralElements().entrySet()) {
                    possibleTypes.add(new SelectItem(entry.getKey(), entry.getValue()));
                }
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
