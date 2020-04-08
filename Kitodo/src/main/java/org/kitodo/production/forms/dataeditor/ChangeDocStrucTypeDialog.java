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

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import javax.faces.model.SelectItem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.api.dataformat.IncludedStructuralElement;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.metadata.MetadataEditor;
import org.kitodo.production.services.ServiceManager;

/**
 * Backing bean for the change doc struc type dialog of the metadata editor.
 */
public class ChangeDocStrucTypeDialog {
    private static final Logger logger = LogManager.getLogger(ChangeDocStrucTypeDialog.class);

    private final DataEditorForm dataEditor;
    private final List<SelectItem> docStructEditTypeSelectionItems = new ArrayList<>();
    private String docStructEditTypeSelectionSelectedItem;

    /**
     * Backing bean for the add doc struc type dialog of the metadata editor.
     *
     * @see "WEB-INF/templates/includes/metadataEditor/dialogs/changeDocStrucType.xhtml"
     */
    ChangeDocStrucTypeDialog(DataEditorForm dataEditor) {
        this.dataEditor = dataEditor;
    }

    /**
     * Edit the doc struc.
     */
    public void editDocStruc() {
        IncludedStructuralElement selectedStructure = getTheCurrentlySelectedIncludedStructuralElement();
        selectedStructure.setType(docStructEditTypeSelectionSelectedItem);
        dataEditor.refreshStructurePanel();
    }

    /**
     * Returns the selected item of the docStructEditTypeSelection drop-down
     * menu.
     *
     * @return the selected item of the docStructEditTypeSelection
     */
    public List<SelectItem> getDocStructEditTypeSelectionItems() {
        return docStructEditTypeSelectionItems;
    }

    /**
     * Return selected doc struct type.
     *
     * @return selected doc struct type
     */
    public String getDocStructEditTypeSelectionSelectedItem() {
        return docStructEditTypeSelectionSelectedItem;
    }

    /**
     * Sets the selected item of the docStructEditTypeSelection drop-down menu.
     *
     * @param docStructEditTypeSelectionSelectedItem
     *            selected item to set
     */
    public void setDocStructEditTypeSelectionSelectedItem(String docStructEditTypeSelectionSelectedItem) {
        this.docStructEditTypeSelectionSelectedItem = docStructEditTypeSelectionSelectedItem;
    }

    /**
     * Prepare popup dialog by retrieving available doc struct types for
     * selected element.
     */
    public void prepare() {
        try {
            IncludedStructuralElement selectedStructure = getTheCurrentlySelectedIncludedStructuralElement();
            Map<String, String> possibleTypes = findAllTypesThisIncludedStructuralElementCanHave(selectedStructure);
            docStructEditTypeSelectionItems.clear();
            for (Entry<String, String> typeOption : possibleTypes.entrySet()) {
                docStructEditTypeSelectionItems.add(new SelectItem(typeOption.getKey(), typeOption.getValue()));
            }
            docStructEditTypeSelectionSelectedItem = selectedStructure.getType();
        } catch (IllegalStateException | IOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
    }

    private IncludedStructuralElement getTheCurrentlySelectedIncludedStructuralElement() {
        if (dataEditor.getSelectedStructure().isPresent()) {
            return dataEditor.getSelectedStructure().get();
        } else {
            throw new IllegalStateException("could not retrieve selected structure");
        }
    }

    private Map<String, String> findAllTypesThisIncludedStructuralElementCanHave(
            IncludedStructuralElement includedStructuralElement) throws IOException {

        Map<String, String> possibleTypes = getAllowedChildTypesFromIncludedStructuralParentElement(
            includedStructuralElement);
        onlyAllowTypesThatSupportAllGivenIncludedStructuralChildElements(includedStructuralElement, possibleTypes);
        return possibleTypes;
    }

    private Map<String, String> getAllowedChildTypesFromIncludedStructuralParentElement(
            IncludedStructuralElement includedStructuralElement) throws IOException {

        IncludedStructuralElement rootElement = dataEditor.getWorkpiece().getRootElement();
        if (rootElement.equals(includedStructuralElement)) {
            if (Objects.isNull(dataEditor.getProcess().getParent())) {
                return dataEditor.getRuleset().getStructuralElements(dataEditor.getPriorityList());
            } else {
                return getAllowedChildTypesFromParentalProcess();
            }
        } else {
            LinkedList<IncludedStructuralElement> ancestors = MetadataEditor
                    .getAncestorsOfStructure(includedStructuralElement, rootElement);
            String parentType = ancestors.getLast().getType();
            return getAllowedSubstructuralElements(parentType);
        }
    }

    private Map<String, String> getAllowedChildTypesFromParentalProcess() throws IOException {
        URI parentMetadataUri = ServiceManager.getProcessService()
                .getMetadataFileUri(dataEditor.getProcess().getParent());
        IncludedStructuralElement parentRootElement = ServiceManager.getMetsService().loadWorkpiece(parentMetadataUri)
                .getRootElement();
        List<IncludedStructuralElement> parentHierarchyPath = MetadataEditor
                .determineIncludedStructuralElementPathToChildRecursive(parentRootElement,
                    dataEditor.getProcess().getId());
        if (parentHierarchyPath.isEmpty()) {
            throw new IllegalStateException("proces is not linked in parent process");
        }
        return getAllowedSubstructuralElements(
            ((LinkedList<IncludedStructuralElement>) parentHierarchyPath).getLast().getType());
    }

    private Map<String, String> getAllowedSubstructuralElements(String parentType) {
        StructuralElementViewInterface parentView = dataEditor.getRuleset().getStructuralElementView(parentType,
            dataEditor.getAcquisitionStage(), dataEditor.getPriorityList());
        return parentView.getAllowedSubstructuralElements();
    }

    private void onlyAllowTypesThatSupportAllGivenIncludedStructuralChildElements(
            IncludedStructuralElement includedStructuralElement, Map<String, String> possibleTypes) {
        if (includedStructuralElement.getChildren().isEmpty()) {
            return;
        }
        Set<String> childTypes = new HashSet<>();
        for (IncludedStructuralElement child : includedStructuralElement.getChildren()) {
            childTypes.add(child.getType());
        }
        for (Iterator<Entry<String, String>> possibleTypesIterator = possibleTypes.entrySet()
                .iterator(); possibleTypesIterator.hasNext();) {
            String typeToCheck = possibleTypesIterator.next().getKey();
            StructuralElementViewInterface viewOnTypeToCheck = dataEditor.getRuleset().getStructuralElementView(
                typeToCheck, dataEditor.getAcquisitionStage(), dataEditor.getPriorityList());
            if (!viewOnTypeToCheck.getAllowedSubstructuralElements().keySet().containsAll(childTypes)) {
                possibleTypesIterator.remove();
            }
        }
    }
}
