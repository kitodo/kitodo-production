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
import java.util.Objects;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.Metadata;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.api.dataformat.IncludedStructuralElement;
import org.kitodo.api.dataformat.MediaUnit;
import org.kitodo.production.helper.Helper;

/**
 * Backing bean for the metadata panel of the metadata editor.
 */
public class MetadataPanel implements Serializable {

    private static final Logger logger = LogManager.getLogger(MetadataPanel.class);

    private String addMetadataKeySelectedItem = "";

    private Collection<Metadata> clipboard = new ArrayList<>();

    private final RulesetSetupInterface rulesetSetup;

    private FieldedMetadataTableRow logicalMetadataTable = FieldedMetadataTableRow.EMPTY;
    private FieldedMetadataTableRow physicalMetadataTable = FieldedMetadataTableRow.EMPTY;

    MetadataPanel(RulesetSetupInterface rulesetSetup) {
        this.rulesetSetup = rulesetSetup;
    }

    // TODO create similar method for physical metadata entries
    /**
     * The method is executed when a user clicks the add metadata button. A new
     * metadata entry will be created with the entered type and value. Actually
     * this procedure is not in the sense of the inventor. Especially with
     * selection types, the user must enter the coded value, which is
     * inconvenient. Nor can this procedure be transferred to metadata groups.
     * The better approach would be to first create and display a field and then
     * give the user the option to enter or select the value.
     */
    public void addMetadataEntry() {
        try {
            /*
             * We add the metadata to add. This will rebuild the table and
             * create an empty table line (somewhere) into which we can enter
             * the value.
             */
            logicalMetadataTable.addAdditionallySelectedField(addMetadataKeySelectedItem);
        } catch (NoSuchMetadataFieldException e) {
            Helper.setErrorMessage(e.getLocalizedMessage());
        }
    }

    /**
     * Empties the metadata panel.
     */
    public void clear() {
        logicalMetadataTable = FieldedMetadataTableRow.EMPTY;
        physicalMetadataTable = FieldedMetadataTableRow.EMPTY;
        clipboard.clear();
        addMetadataKeySelectedItem = "";
    }

    /**
     * Set addMetadataKeySelectedItem.
     *
     * @param addMetadataKeySelectedItem as java.lang.String
     */
    public void setAddMetadataKeySelectedItem(String addMetadataKeySelectedItem) {
        this.addMetadataKeySelectedItem = addMetadataKeySelectedItem;
    }

    Collection<Metadata> getClipboard() {
        return clipboard;
    }

    /**
     * Returns the rows of logical metadata that JSF has to display.
     *
     * @return the rows of logical metadata
     */
    public List<MetadataTableRow> getLogicalMetadataRows() {
        return logicalMetadataTable.getRows();
    }

    /**
     * Returns the rows of physical metadata that JSF has to display.
     *
     * @return the rows of physical metadata
     */
    public List<MetadataTableRow> getPhysicalMetadataRows() {
        return physicalMetadataTable.getRows();
    }

    void showLogical(Optional<IncludedStructuralElement> optionalStructure) {
        if (optionalStructure.isPresent()) {
            StructuralElementViewInterface divisionView = rulesetSetup.getRuleset().getStructuralElementView(
                    optionalStructure.get().getType(), rulesetSetup.getAcquisitionStage(), rulesetSetup.getPriorityList());
            logicalMetadataTable = new FieldedMetadataTableRow(this, optionalStructure.get(), divisionView);
        } else {
            logicalMetadataTable = FieldedMetadataTableRow.EMPTY;
        }

    }

    void showPageInLogical(MediaUnit mediaUnit) {
        if (Objects.nonNull(mediaUnit)) {
            StructuralElementViewInterface divisionView = rulesetSetup.getRuleset().getStructuralElementView(
                    mediaUnit.getType(), rulesetSetup.getAcquisitionStage(), rulesetSetup.getPriorityList());
            logicalMetadataTable = new FieldedMetadataTableRow(this, mediaUnit.getMetadata(), divisionView);
        } else {
            logicalMetadataTable = FieldedMetadataTableRow.EMPTY;
        }

    }

    void showPhysical(Optional<MediaUnit> optionalMediaUnit) {
        if (optionalMediaUnit.isPresent() && Objects.nonNull(optionalMediaUnit.get().getType())) {
            StructuralElementViewInterface divisionView = rulesetSetup.getRuleset().getStructuralElementView(
                    optionalMediaUnit.get().getType(), rulesetSetup.getAcquisitionStage(), rulesetSetup.getPriorityList());
            physicalMetadataTable = new FieldedMetadataTableRow(this, optionalMediaUnit.get().getMetadata(), divisionView);
        } else {
            physicalMetadataTable = FieldedMetadataTableRow.EMPTY;
        }

    }

    /**
     * Callback function 'paste' button in MetadataPanel. (Not yet implemented!)
     */
    public void pasteClick() {
        logicalMetadataTable.pasteClick();
    }

    void preserve() {
        try {
            this.preserveLogical();
            this.preservePhysical();
        } catch (InvalidMetadataValueException | NoSuchMetadataFieldException e) {
            logger.info(e.getMessage());
        }
    }

    void preserveLogical() throws InvalidMetadataValueException, NoSuchMetadataFieldException {
        logicalMetadataTable.preserve();
    }

    void preservePhysical() throws InvalidMetadataValueException, NoSuchMetadataFieldException {
        physicalMetadataTable.preserve();
    }
}
