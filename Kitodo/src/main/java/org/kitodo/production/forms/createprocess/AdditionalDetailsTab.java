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

package org.kitodo.production.forms.createprocess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataeditor.rulesetmanagement.ComplexMetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.api.dataformat.IncludedStructuralElement;
import org.kitodo.exceptions.InvalidMetadataValueException;
import org.kitodo.exceptions.NoSuchMetadataFieldException;
import org.kitodo.production.forms.CreateProcessForm;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class AdditionalDetailsTab {
    private static final Logger logger = LogManager.getLogger(AdditionalDetailsTab.class);

    private CreateProcessForm createProcessForm;

    private FieldedAdditionalDetailsTableRow additionalDetailsTable = FieldedAdditionalDetailsTableRow.EMPTY;
    private List<String> filledMetadataGroups = new ArrayList<>();

    public AdditionalDetailsTab(CreateProcessForm createProcessForm) {
        this.createProcessForm = createProcessForm;
    }

    public void resetAddtionalDetailsTable() {
        this.additionalDetailsTable = new FieldedAdditionalDetailsTableRow();
    }

    /**
     * Show the additional Details table.
     * @param structure
     *          which its Metadaten are wanted to be shown
     */
    public void show(IncludedStructuralElement structure) {
        StructuralElementViewInterface divisionView = this.createProcessForm.getRuleset().getStructuralElementView(
                this.createProcessForm.getProcessDataTab().getDocType(),
                this.createProcessForm.getAcquisitionStage(),
                this.createProcessForm.getPriorityList());
        additionalDetailsTable = new FieldedAdditionalDetailsTableRow(this, structure, divisionView);
    }

    /**
     * Returns the rows of logical metadata that JSF has to display.
     *
     * @return the rows of logical metadata
     */
    public List<AdditionalDetailsTableRow> getAdditionalDetailsTableRows() {
        return additionalDetailsTable.getRows();
    }

    /**
     * preserve the additional details table.
     */
    public void preserve() {
        try {
            additionalDetailsTable.preserve();
        } catch (NoSuchMetadataFieldException | InvalidMetadataValueException e) {
            logger.error(e.getLocalizedMessage());
        }
    }

    /**
     * Add a metadata group row in additionalDetailsTable.
     * @param metadataId
     *              the key of the metadata group
     * @return the added row as a FieldedAdditionalDetailsTableRow.
     *
     * @throws NoSuchMetadataFieldException
     *          if the metadataId is not allowed as a Metadata for the doctype.
     */
    private FieldedAdditionalDetailsTableRow addMetadataGroupRow(String metadataId) throws NoSuchMetadataFieldException {
        Collection<MetadataViewInterface> docTypeAddableDivisions = this.createProcessForm.getRuleset().getStructuralElementView(
               this.createProcessForm.getProcessDataTab().getDocType(),
                this.createProcessForm.getAcquisitionStage(),
                this.createProcessForm.getPriorityList()).getAddableMetadata(Collections.emptyMap(), Collections.emptyList());

        List<MetadataViewInterface> filteredViews = docTypeAddableDivisions
               .stream()
               .filter(v -> v.getId().equals(metadataId))
               .collect(Collectors.toList());

        if (!filteredViews.isEmpty()) {
            return additionalDetailsTable.createMetadataGroupPanel((ComplexMetadataViewInterface) filteredViews.get(0),
                    Collections.emptyList());
        }
        throw new NoSuchMetadataFieldException(metadataId, "");
    }

    void setAdditionalDetailsTable(List<AdditionalDetailsTableRow> rows, NodeList nodes) {
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            Element element = (Element) node;
            String nodeName = element.getAttribute("name");
            for (AdditionalDetailsTableRow tableRow : rows) {
                if (Objects.nonNull(tableRow.getMetadataID()) && tableRow.getMetadataID().equals(nodeName)) {
                    if (node.getLocalName().equals("metadataGroup")
                            && tableRow instanceof FieldedAdditionalDetailsTableRow) {
                        FieldedAdditionalDetailsTableRow fieldedRow;
                        if (filledMetadataGroups.contains(nodeName)) {
                            try {
                                fieldedRow = addMetadataGroupRow(nodeName);
                                this.additionalDetailsTable.getRows().add(fieldedRow);
                                setAdditionalDetailsTable(fieldedRow.getRows(), element.getChildNodes());
                            } catch (NoSuchMetadataFieldException e) {
                                logger.error(e.getLocalizedMessage());
                            }
                        } else {
                            fieldedRow = (FieldedAdditionalDetailsTableRow) tableRow;
                            filledMetadataGroups.add(nodeName);
                            setAdditionalDetailsTable(fieldedRow.getRows(), element.getChildNodes());
                        }
                    } else if (node.getLocalName().equals("metadata")) {
                        setAdditionalDetailsRow(tableRow, element.getTextContent());
                    }
                    break;
                }
            }
        }
    }

    public static AdditionalDetailsTableRow setAdditionalDetailsRow(AdditionalDetailsTableRow row, String elementText) {
        if (row instanceof TextMetadataTableRow) {
            // TODO: incorporate "initstart" and "initend" values from kitodo_projects.xml like AddtionalField!
            ((TextMetadataTableRow) row).setValue(elementText);
        } else if (row instanceof BooleanMetadataTableRow) {
            ((BooleanMetadataTableRow) row).setActive(Boolean.parseBoolean(elementText));
        } else if (row instanceof SelectMetadataTableRow) {
            ((SelectMetadataTableRow) row).setSelectedItem(elementText);
        }
        return row;
    }

    public static String getMetadataValue(AdditionalDetailsTableRow row) {
        if (row instanceof TextMetadataTableRow) {
            return ((TextMetadataTableRow) row).getValue();
        } else if (row instanceof BooleanMetadataTableRow) {
            return String.valueOf(((BooleanMetadataTableRow) row).isActive());
        } else if (row instanceof SelectMetadataTableRow) {
            return ((SelectMetadataTableRow) row).getSelectedItem();
        } else {
            // TODO: extract value of FieldedMetadataTableRow!
            return "";
        }
    }
}
