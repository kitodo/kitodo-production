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

package org.kitodo.production.services.dataeditor;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.faces.model.SelectItem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.MetadataGroup;
import org.kitodo.api.dataeditor.DataEditorInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.ComplexMetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.SimpleMetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.api.dataformat.View;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Process;
import org.kitodo.exceptions.InvalidMetadataValueException;
import org.kitodo.production.forms.createprocess.ProcessDetail;
import org.kitodo.production.forms.createprocess.ProcessFieldedMetadata;
import org.kitodo.production.forms.dataeditor.DataEditorForm;
import org.kitodo.production.forms.dataeditor.StructureTreeNode;
import org.kitodo.production.helper.Helper;
import org.kitodo.serviceloader.KitodoServiceLoader;
import org.primefaces.model.TreeNode;

public class DataEditorService {

    private static final Logger logger = LogManager.getLogger(DataEditorService.class);

    /**
     * Reads the data of a given file in xml format. The format of that file
     * needs to be the corresponding to the one which is referenced by the data
     * editor module as data format module.
     *
     * @param xmlFileUri
     *            The path to the metadata file as URI.
     */
    public void readData(URI xmlFileUri) throws IOException {
        DataEditorInterface dataEditor = loadDataEditorModule();
        URI xsltFile = getXsltFileFromConfig();
        dataEditor.readData(xmlFileUri, xsltFile);
    }

    private DataEditorInterface loadDataEditorModule() {
        KitodoServiceLoader<DataEditorInterface> serviceLoader = new KitodoServiceLoader<>(DataEditorInterface.class);
        return serviceLoader.loadModule();
    }

    private URI getXsltFileFromConfig() {
        String path = getXsltFolder();
        String file = ConfigCore.getParameter(ParameterCore.XSLT_FILENAME_METADATA_TRANSFORMATION);
        return Paths.get(path + file).toUri();
    }

    private String getXsltFolder() {
        return ConfigCore.getParameter(ParameterCore.DIR_XSLT);
    }

    /**
     * Retrieve and return list of metadata keys that are used for displaying title information in the metadata editors
     * structure and gallery panels from the Kitodo configuration file.
     *
     * @return list of title metadata keys
     */
    public static List<String> getTitleKeys() {
        return Arrays.stream(ConfigCore.getParameter(ParameterCore.TITLE_KEYS, "").split(","))
                .map(String::trim).collect(Collectors.toList());
    }

    /**
     * Retrieve and return title value from given IncludedStructuralElement.
     *
     * @param element IncludedStructuralElement for which the title value is returned.
     * @param metadataTitleKey as a String that its value will be displayed.
     * @return title value of given element
     */
    public static String getTitleValue(LogicalDivision element, String metadataTitleKey) {
        String[] metadataPath = metadataTitleKey.split("@");
        int lastIndex = metadataPath.length - 1;
        Collection<Metadata> metadata = element.getMetadata();
        for (int i = 0; i < lastIndex; i++) {
            final String metadataKey = metadataPath[i];
            metadata = metadata.stream()
                    .filter(currentMetadata -> Objects.equals(currentMetadata.getKey(), metadataKey))
                    .filter(MetadataGroup.class::isInstance).map(MetadataGroup.class::cast)
                    .flatMap(metadataGroup -> metadataGroup.getGroup().stream())
                    .collect(Collectors.toList());
        }
        Optional<String> metadataTitle = metadata.stream()
                .filter(currentMetadata -> Objects.equals(currentMetadata.getKey(), metadataPath[lastIndex]))
                .filter(MetadataEntry.class::isInstance).map(MetadataEntry.class::cast)
                .map(MetadataEntry::getValue)
                .filter(value -> !value.isEmpty())
                .findFirst();
        if (metadataTitle.isPresent()) {
            return metadataTitle.get();
        }
        return " - ";
    }

    /**
     * Determine and return which metadata can be added to a specific MetadataGroup.
     *
     * @param dataEditor DataEditorForm instance used to determine addable metadata types
     * @param metadataNode TreeNode containing MetadataGroup to check
     * @return List of select items representing addable metadata types
     */
    public static List<SelectItem> getAddableMetadataForGroup(DataEditorForm dataEditor, TreeNode metadataNode) {
        ProcessFieldedMetadata fieldedMetadata = ((ProcessFieldedMetadata) metadataNode.getData());
        ComplexMetadataViewInterface metadataView = fieldedMetadata.getMetadataView();
        List<SelectItem> addableMetadata = new ArrayList<>();
        for (MetadataViewInterface keyView : metadataView.getAddableMetadata(fieldedMetadata.getChildMetadata(),
                fieldedMetadata.getAdditionallySelectedFields())) {
            addableMetadata.add(
                    new SelectItem(keyView.getId(), keyView.getLabel(),
                            keyView instanceof SimpleMetadataViewInterface
                                    ? ((SimpleMetadataViewInterface) keyView).getInputType().toString()
                                    : "dataTable"));
        }
        if (!addableMetadata.isEmpty() && !dataEditor.getProcess().getRuleset().isOrderMetadataByRuleset()) {
            addableMetadata.sort(Comparator.comparing(SelectItem::getLabel));
        }
        return addableMetadata;
    }

    /**
     * Determine and return which metadata can be added to the currently selected IncludedStructuralElement.
     * @param dataEditor DataEditorForm instance used to determine addable metadata types
     * @param currentElement whether addable metadata should be determined for currently selected
     *                       IncludedStructuralElement or not (if "false", it is determined for a new
     *                       IncludedStructuralElement added by "AddDocStrucElementDialog"!)
     * @param metadataNodes TreeNodes containing the metadata already assigned to the current structure element
     * @param structureType type of the new structure to be added and for which addable metadata is to be determined
     * @return List of select items representing addable metadata types
     */
    public static List<SelectItem> getAddableMetadataForStructureElement(DataEditorForm dataEditor,
                                                                         boolean currentElement,
                                                                         List<TreeNode> metadataNodes,
                                                                         String structureType,
                                                                         boolean isLogicalStructure) {
        List<SelectItem> addableMetadata = new ArrayList<>();
        Collection<Metadata> existingMetadata = Collections.emptyList();
        StructuralElementViewInterface structureView;
        try {
            if (currentElement) {
                structureView = getStructuralElementView(dataEditor);
                if(Objects.isNull(structureView)) {
                    return Collections.emptyList();
                }
                existingMetadata = getExistingMetadataRows(metadataNodes);
            } else {
                structureView = dataEditor.getRulesetManagement()
                        .getStructuralElementView(structureType,
                                dataEditor.getAcquisitionStage(), dataEditor.getPriorityList());
            }
            Collection<String> additionalFields = isLogicalStructure ? dataEditor.getMetadataPanel()
                    .getLogicalMetadataTable().getAdditionallySelectedFields() : dataEditor.getMetadataPanel()
                    .getPhysicalMetadataTable().getAdditionallySelectedFields();
            Collection<MetadataViewInterface> addableKeyViews = structureView
                    .getAddableMetadata(existingMetadata, additionalFields);
            for (MetadataViewInterface keyView : addableKeyViews) {
                addableMetadata.add(
                        new SelectItem(keyView.getId(), keyView.getLabel(),
                                keyView instanceof SimpleMetadataViewInterface
                                        ? ((SimpleMetadataViewInterface) keyView).getInputType().toString()
                                        : "dataTable"));
            }
        } catch (InvalidMetadataValueException e) {
            Helper.setErrorMessage(e);
        }
        return addableMetadata.stream().sorted(Comparator.comparing(SelectItem::getLabel)).collect(Collectors.toList());
    }

    /**
     * Determine and return list of metadata that can be added to currently selected structure element.
     *
     * @param dataEditor DataEditorForm instance used to determine list of addable metadata types
     * @return List of select items representing addable metadata types
     */
    public static List<SelectItem> getAddableMetadataForStructureElement(DataEditorForm dataEditor) {
        return getAddableMetadataForStructureElement(dataEditor,
                true, dataEditor.getMetadataPanel().getLogicalMetadataRows().getChildren(), null, true);
    }

    /**
     * Determine and return list of metadata that can be added to currently selected media unit.
     *
     * @param dataEditor DataEditorForm instance used to determine list of addable metadata types
     * @return List of select items representing addable metadata types
     */
    public static List<SelectItem> getAddableMetadataForMediaUnit(DataEditorForm dataEditor) {
        return getAddableMetadataForStructureElement(dataEditor,
                true, dataEditor.getMetadataPanel().getPhysicalMetadataRows().getChildren(), null, false);
    }

    /**
     * Determine and return StructureElementViewInterface corresponding to structure element currently selected or to be
     * added via AddDocStructTypeDialog.
     *
     * @param dataEditor DataEditorForm instance used to determine StructureElementViewInterface
     * @return StructureElementViewInterface corresponding to structure element currently selected or to be added
     */
    public static StructuralElementViewInterface getStructuralElementView(DataEditorForm dataEditor) {
        Optional<LogicalDivision> selectedStructure = dataEditor.getSelectedStructure();
        if (selectedStructure.isPresent()) {
            return dataEditor.getRulesetManagement()
                    .getStructuralElementView(selectedStructure.get().getType(),
                            dataEditor.getAcquisitionStage(), dataEditor.getPriorityList());
        }

        TreeNode selectedLogicalNode = dataEditor.getStructurePanel().getSelectedLogicalNode();
        if (Objects.isNull(selectedLogicalNode)) {
            throw new IllegalStateException("No logical node selected");
        }

        if (!(selectedLogicalNode.getData() instanceof StructureTreeNode)) {
            throw new IllegalStateException("Selected logical node data is of bad type: "
                    + (Objects.isNull(selectedLogicalNode.getData()) ? "null"
                            : selectedLogicalNode.getData().getClass().getName()));
        }
        StructureTreeNode structureTreeNode = (StructureTreeNode) selectedLogicalNode.getData();

        Object dataObject = structureTreeNode.getDataObject();
        // data object is null for structures inside parent processes
        if (Objects.isNull(dataObject)) {
            return null;
        }

        // default case
        if (dataObject instanceof View) {
            View view = (View) dataObject;
            if (Objects.isNull(view.getPhysicalDivision())) {
                throw new IllegalStateException("View has no physical division assigned");
            }

            return dataEditor.getRulesetManagement().getStructuralElementView(
                    view.getPhysicalDivision().getType(),
                        dataEditor.getAcquisitionStage(), dataEditor.getPriorityList());
        }

        // data object is a Process for parent processes
        if (dataObject instanceof Process) {
            return null;
        }

        throw new IllegalStateException("Data object is of bad type: "
                + (Objects.isNull(dataObject) ? "null"
                    : dataObject.getClass().getName()));
    }

    private static Collection<Metadata> getExistingMetadataRows(List<TreeNode> metadataTreeNodes)
            throws InvalidMetadataValueException {
        Collection<Metadata> existingMetadataRows = new ArrayList<>();

        for (TreeNode metadataNode : metadataTreeNodes) {
            if (metadataNode.getData() instanceof ProcessDetail) {
                try {
                    existingMetadataRows.addAll(((ProcessDetail) metadataNode.getData()).getMetadata());
                } catch (NullPointerException e) {
                    logger.error(e);
                }
            }
        }
        return existingMetadataRows;
    }
}
