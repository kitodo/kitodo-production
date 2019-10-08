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

import javax.faces.model.SelectItem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataeditor.rulesetmanagement.ComplexMetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.SimpleMetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.api.dataformat.IncludedStructuralElement;
import org.kitodo.exceptions.InvalidMetadataValueException;
import org.kitodo.exceptions.NoSuchMetadataFieldException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AdditionalDetailsTab {
    private static final Logger logger = LogManager.getLogger(AdditionalDetailsTab.class);
    private static final String PERSON = "Person";
    private static final String ROLE = "Role";
    private static final String AUTHOR = "Author";
    private static final String FIRST_NAME = "FirstName";
    private static final String LAST_NAME = "LastName";


    private CreateProcessForm createProcessForm;

    private FieldedAdditionalDetailsTableRow additionalDetailsTable = new FieldedAdditionalDetailsTableRow();

    public AdditionalDetailsTab(CreateProcessForm createProcessForm) {
        this.createProcessForm = createProcessForm;
    }

    /**
     * Show the additional Details table.
     * @param structure
     *          which its Metadaten are wanted to be shown
     */
    public void show(IncludedStructuralElement structure) {
        StructuralElementViewInterface divisionView = this.createProcessForm.getRuleset().getStructuralElementView(
                createProcessForm.getProcessDataTab().getRulesetType(),
                this.createProcessForm.getAcquisitionStage(),
                this.createProcessForm.getPriorityList());
        additionalDetailsTable = new FieldedAdditionalDetailsTableRow(this, structure, divisionView);
    }

    /**
     * Set additionalDetailsTable.
     * @param additionalDetailsTable
     *          as FieldedAdditionalDetailsTableRow
     */
    public void setAdditionalDetailsTable(FieldedAdditionalDetailsTableRow additionalDetailsTable) {
        this.additionalDetailsTable = additionalDetailsTable;
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
    private AdditionalDetailsTableRow addAdditionalDetailRow(String metadataId) throws NoSuchMetadataFieldException {
        Collection<MetadataViewInterface> docTypeAddableDivisions = this.createProcessForm.getRuleset().getStructuralElementView(
               this.createProcessForm.getProcessDataTab().getRulesetType(),
                this.createProcessForm.getAcquisitionStage(),
                this.createProcessForm.getPriorityList()).getAddableMetadata(Collections.emptyMap(), Collections.emptyList());

        List<MetadataViewInterface> filteredViews = docTypeAddableDivisions
               .stream()
               .filter(metadataView -> metadataView.getId().equals(metadataId))
               .collect(Collectors.toList());

        if (!filteredViews.isEmpty()) {
            if (filteredViews.get(0).isComplex()) {
                return additionalDetailsTable.createMetadataGroupPanel((ComplexMetadataViewInterface) filteredViews.get(0),
                        Collections.emptyList());
            } else {
                return additionalDetailsTable.createMetadataEntryEdit((SimpleMetadataViewInterface) filteredViews.get(0),
                        Collections.emptyList());

            }
        }
        throw new NoSuchMetadataFieldException(metadataId, "");
    }

    void fillAdditionalDetailsTable(List<AdditionalDetailsTableRow> rows, NodeList nodes, boolean childRows) {
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            Element element = (Element) node;
            String nodeName = element.getAttribute("name");
            boolean filled = false;
            for (AdditionalDetailsTableRow tableRow : rows) {
                if (Objects.nonNull(tableRow.getMetadataID()) && tableRow.getMetadataID().equals(nodeName)) {
                    if (childRows || getMetadataValue(tableRow).isEmpty()) {
                        filled = true;
                        if (node.getLocalName().equals("metadataGroup")
                                && tableRow instanceof FieldedAdditionalDetailsTableRow) {
                            fillAdditionalDetailsTable(((FieldedAdditionalDetailsTableRow) tableRow).getRows(),
                                    element.getChildNodes(), true);
                        } else if (node.getLocalName().equals("metadata")) {
                            setAdditionalDetailsRow(tableRow, element.getTextContent());
                        }
                    }
                    break;
                }
            }
            if (!filled) {
                try {
                    AdditionalDetailsTableRow newRow = addAdditionalDetailRow(nodeName);
                    this.additionalDetailsTable.getRows().add(newRow);
                    if (newRow instanceof FieldedAdditionalDetailsTableRow) {
                        fillAdditionalDetailsTable(((FieldedAdditionalDetailsTableRow) newRow).getRows(), element.getChildNodes(), true);
                    } else {
                        setAdditionalDetailsRow(newRow, element.getTextContent());
                    }
                } catch (NoSuchMetadataFieldException e) {
                    logger.error(e.getMessage());
                }
            }
        }
    }

    /**
     * Set the value of a specific row in the additionalDetailsTab.
     * @param row the row that its value want to be modified
     *      as AdditionalDetailsTableRow
     * @param value
     *       as a java.lang.String
     * @return the modified row
     *      as a AdditionalDetailsTableRow
     */
    public static AdditionalDetailsTableRow setAdditionalDetailsRow(AdditionalDetailsTableRow row, String value) {
        if (row instanceof TextAdditionalDetailsTableRow) {
            // TODO: incorporate "initstart" and "initend" values from kitodo_projects.xml like AddtionalField!
            ((TextAdditionalDetailsTableRow) row).setValue(value);
        } else if (row instanceof BooleanAdditionalDetailsTableRow) {
            ((BooleanAdditionalDetailsTableRow) row).setActive(Boolean.parseBoolean(value));
        } else if (row instanceof SelectAdditionalDetailsTableRow) {
            ((SelectAdditionalDetailsTableRow) row).setSelectedItem(value);
        }
        return row;
    }

    /**
     *  Get the value of a specific row in the additionalDetailsTab.
     * @param row
     *      as AdditionalDetailsTableRow
     * @return the value as a java.lang.String
     */
    public static String getMetadataValue(AdditionalDetailsTableRow row) {
        String value = "";
        if (row instanceof TextAdditionalDetailsTableRow) {
            return ((TextAdditionalDetailsTableRow) row).getValue();
        } else if (row instanceof BooleanAdditionalDetailsTableRow) {
            return String.valueOf(((BooleanAdditionalDetailsTableRow) row).isActive());
        } else if (row instanceof SelectAdditionalDetailsTableRow) {
            return String.join(", ", ((SelectAdditionalDetailsTableRow) row).getSelectedItems());
        } else if (row instanceof FieldedAdditionalDetailsTableRow && row.getMetadataID().equals(PERSON)) {
            value = getCreator(((FieldedAdditionalDetailsTableRow) row).getRows());
        }
        return value;
    }

    /**
     * get all creators names .
     * @param additionalDetailsTableRows
     *              additionalDetailsTableRows
     * @return all creators names as a String
     */
    public static String getListOfCreators(List<AdditionalDetailsTableRow> additionalDetailsTableRows) {
        String listofAuthors = "";
        for (AdditionalDetailsTableRow row : additionalDetailsTableRows) {
            if (row instanceof FieldedAdditionalDetailsTableRow
                    && PERSON.equals(row.getMetadataID())) {
                FieldedAdditionalDetailsTableRow tableRow = (FieldedAdditionalDetailsTableRow) row;
                for (AdditionalDetailsTableRow detailsTableRow : tableRow.getRows()) {
                    if (ROLE.equals(detailsTableRow.getMetadataID())
                            && AUTHOR.equals(AdditionalDetailsTab.getMetadataValue(detailsTableRow))) {
                        listofAuthors = listofAuthors.concat(getCreator(tableRow.getRows()));
                        break;
                    }
                }
            }
        }
        return listofAuthors;
    }

    private static String getCreator(List<AdditionalDetailsTableRow> rows) {
        String author = "";
        for (AdditionalDetailsTableRow row : rows) {
            if (FIRST_NAME.equals(row.getMetadataID())
                    || LAST_NAME.equals(row.getMetadataID())) {
                author = author.concat(AdditionalDetailsTab.getMetadataValue(row));
            }
        }
        return author;
    }
}
