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

package org.kitodo.production.forms.copyprocess;

import de.unigoettingen.sub.search.opac.ConfigOpac;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataeditor.rulesetmanagement.ComplexMetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.api.dataformat.IncludedStructuralElement;
import org.kitodo.exceptions.InvalidMetadataValueException;
import org.kitodo.exceptions.NoSuchMetadataFieldException;


public class AdditionalDetailsTab {
    private static final Logger logger = LogManager.getLogger(AdditionalDetailsTab.class);

    protected String docType;
    private final RulesetSetupInterface rulesetSetup;

    private FieldedAdditionalDetailsTableRow additionalDetailsTable = FieldedAdditionalDetailsTableRow.EMPTY;

    /**
     * Constructor.
     * @param rulesetSetup RulesetSetupInterface
     * @param docType docType of the process
     */
    public AdditionalDetailsTab(RulesetSetupInterface rulesetSetup, String docType) {
        this.rulesetSetup = rulesetSetup;
        try {
            this.docType = ConfigOpac.getDoctypeByName(docType).getRulesetType();
        } catch (FileNotFoundException e) {
            logger.error(e.getLocalizedMessage());
        }
    }

    /**
     * Show the additional Details table.
     * @param structure
     *          which its Metadaten are wanted to be shown
     */
    public void show(IncludedStructuralElement structure) {
        StructuralElementViewInterface divisionView = rulesetSetup.getRuleset().getStructuralElementView(
                this.docType, rulesetSetup.getAcquisitionStage(), rulesetSetup.getPriorityList());
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
     * Get docType.
     *
     * @return value of docType
     */
    public String getDocType() {
        return docType;
    }

    /**
     * Set docType.
     *
     * @param docType as java.lang.String
     */
    public void setDocType(String docType) {
        this.docType = docType;
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
     * Get additionalDetailsTable.
     *
     * @return value of additionalDetailsTable
     */
    public FieldedAdditionalDetailsTableRow getAdditionalDetailsTable() {
        return additionalDetailsTable;
    }

    /**
     * Set additionalDetailsTable.
     *
     * @param additionalDetailsTable as org.kitodo.production.forms.copyprocess.FieldedAdditionalDetailsTableRow
     */
    public void setAdditionalDetailsTable(FieldedAdditionalDetailsTableRow additionalDetailsTable) {
        this.additionalDetailsTable = additionalDetailsTable;
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
    public FieldedAdditionalDetailsTableRow addMetadataGroupRow(String metadataId) throws NoSuchMetadataFieldException {
        Collection<MetadataViewInterface> docTypeAddableDivisions = rulesetSetup.getRuleset().getStructuralElementView(
               this.docType,
               rulesetSetup.getAcquisitionStage(),
               rulesetSetup.getPriorityList()).getAddableMetadata(Collections.emptyMap(), Collections.emptyList());

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
}
