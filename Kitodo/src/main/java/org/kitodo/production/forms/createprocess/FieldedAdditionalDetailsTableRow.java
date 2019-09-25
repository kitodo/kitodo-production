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

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.MetadataGroup;
import org.kitodo.api.dataeditor.rulesetmanagement.ComplexMetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.Domain;
import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewWithValuesInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.SimpleMetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.api.dataformat.IncludedStructuralElement;
import org.kitodo.exceptions.InvalidMetadataValueException;
import org.kitodo.exceptions.NoSuchMetadataFieldException;


/**
 * Represents the metadata panel in the metadata editor, or a metadata group
 * within that panel. Basically both are the same, with the exception that the
 * label of the metadata panel is not used.
 */
public class FieldedAdditionalDetailsTableRow extends AdditionalDetailsTableRow implements Serializable {

    private static final Logger logger = LogManager.getLogger(FieldedAdditionalDetailsTableRow.class);

    /**
     * An empty metadata group for the empty metadata panel showing. The empty
     * metadata panel can be displayed if the element selected in the structure
     * window isn’t a structure (has no metadata).
     */
    static final FieldedAdditionalDetailsTableRow EMPTY = new FieldedAdditionalDetailsTableRow();

    /**
     * Fields the user has selected to show in addition, with no data yet.
     */
    private Collection<String> additionallySelectedFields = new ArrayList<>();

    /**
     * Metadata which is excluded by the rule set.
     */
    private Collection<Metadata> hiddenMetadata;

    /**
     * The metadata object with the content of this panel.
     */
    private Collection<Metadata> metadata;

    /**
     * The definition of this panel in the rule set.
     */
    private ComplexMetadataViewInterface metadataView;

    /**
     * The rows that JSF has to display.
     */
    private List<AdditionalDetailsTableRow> rows = new ArrayList<>();

    /**
     * The structure this panel is related to, if it isn’t a sub-panel.
     */
    private IncludedStructuralElement structure;

    /**
     * Creates an empty metadata group. This constructor is used to create the
     * {@link #EMPTY} constant above.
     */
    public FieldedAdditionalDetailsTableRow() {
        super(null, null, null);
        this.rows = new ArrayList<>();
        this.metadata = Collections.emptyList();
        this.hiddenMetadata = Collections.emptyList();
    }

    /**
     * Creates a new root metadata group representing the metadata table
     * content in the metadata panel.
     *
     * @param tab
     *            MetadataPanel to which the FieldedMetadataTableRow is added
     * @param structure
     *            structure selected by the user
     * @param divisionView
     *            information about that structure from the rule set
     */
    public FieldedAdditionalDetailsTableRow(AdditionalDetailsTab tab, IncludedStructuralElement structure,
                                     StructuralElementViewInterface divisionView) {
        this(tab, null, structure, divisionView, structure.getMetadata());
    }

    /**
     * Creates a new root metadata group representing the metadata table
     * content in the metadata panel.
     *
     * @param tab
     *            metadata panel on which this row is showing
     * @param metadata
     *            content for the metadata table
     * @param divisionView
     *            information about that structure from the rule set
     */
    FieldedAdditionalDetailsTableRow(AdditionalDetailsTab tab, Collection<Metadata> metadata, StructuralElementViewInterface divisionView) {
        this(tab, null, null, divisionView, metadata);
    }

    /**
     * Creates a sub-panel for a metadata group.
     *
     * @param panel
     *            metadata panel on which this row is showing
     * @param container
     *            parental metadata group
     * @param metadataView
     *            information about that group from the rule set
     * @param metadata
     *            data of the group, may be empty but must be modifiable
     */
    private FieldedAdditionalDetailsTableRow(AdditionalDetailsTab panel, FieldedAdditionalDetailsTableRow container,
                                             ComplexMetadataViewInterface metadataView, Collection<Metadata> metadata) {
        this(panel, container, null, metadataView, metadata);
    }

    /**
     * Creates a new fielded metadata panel. This constructor is called from
     * one of the above ones and does the work.
     *
     * @param panel
     *            metadata panel on which this row is showing
     * @param container
     *            parental metadata group
     * @param structure
     *            structure selected by the user, null in case of a sub-panel
     * @param metadataView
     *            information about that structure or group from the rule set
     * @param metadata
     *            metadata, may be empty but must be modifiable
     */
    public FieldedAdditionalDetailsTableRow(AdditionalDetailsTab panel, FieldedAdditionalDetailsTableRow container,
                                            IncludedStructuralElement structure,
                                             ComplexMetadataViewInterface metadataView, Collection<Metadata> metadata) {
        super(panel, container, metadataView.getId());
        this.structure = structure;
        this.metadata = metadata;
        this.metadataView = metadataView;
        createMetadataTable();
    }

    /**
     * The method for building the metadata table.
     */
    private void createMetadataTable() {
        // the existing metadata is passed to the rule set, which sorts it
        Map<Metadata, String> metadataWithKeys = addLabels(metadata).parallelStream()
                .collect(Collectors.toMap(Function.identity(), Metadata::getKey));
        List<MetadataViewWithValuesInterface<Metadata>> tableData = metadataView
                .getSortedVisibleMetadata(metadataWithKeys, additionallySelectedFields);

        rows.clear();
        hiddenMetadata = Collections.emptyList();
        for (MetadataViewWithValuesInterface<Metadata> rowData : tableData) {
            Optional<MetadataViewInterface> optionalMetadataView = rowData.getMetadata();
            Collection<Metadata> values = rowData.getValues();
            if (optionalMetadataView.isPresent()) {
                MetadataViewInterface metadataView = optionalMetadataView.get();
                if (metadataView.isComplex()) {
                    rows.add(createMetadataGroupPanel((ComplexMetadataViewInterface) metadataView, values));
                } else {
                    rows.add(createMetadataEntryEdit((SimpleMetadataViewInterface) metadataView, values));
                }
            } else {
                hiddenMetadata = values;
            }
        }
    }

    /**
     * Reads the labels from the structure (if any) and adds them to the
     * returned metadata collection.
     *
     * @param metadata
     *            available metadata
     * @return metadata with labels, if any
     */
    private Collection<Metadata> addLabels(Collection<Metadata> metadata) {
        Collection<Metadata> displayMetadata = metadata;
        if (Objects.nonNull(structure)) {
            displayMetadata = new ArrayList<>(metadata);
            if (Objects.nonNull(structure.getLabel())) {
                MetadataEntry label = new MetadataEntry();
                label.setKey("LABEL");
                label.setValue(structure.getLabel());
                displayMetadata.add(label);
            }
            if (Objects.nonNull(structure.getOrderlabel())) {
                MetadataEntry label = new MetadataEntry();
                label.setKey("ORDERLABEL");
                label.setValue(structure.getLabel());
                displayMetadata.add(label);
            }
        }
        return displayMetadata;
    }

    /**
     * Creates an object to represent a metadata group. This is done by
     * creating a {@code FieldedMetadataGroup} recursively.
     *
     * @param complexMetadataView
     *            information about that group from the rule set
     * @param values
     *            data for that group, must contain at most one element
     * @return a sub-panel for JSF to render
     */
    public FieldedAdditionalDetailsTableRow  createMetadataGroupPanel(ComplexMetadataViewInterface complexMetadataView,
                                                                      Collection<Metadata> values) {
        Collection<Metadata> value;

        switch (values.size()) {
            case 0:
                value = new ArrayList<>();
                break;
            case 1:
                Metadata nextMetadata = values.iterator().next();
                if (nextMetadata instanceof MetadataGroup) {
                    MetadataGroup metadataGroup = (MetadataGroup) nextMetadata;
                    value = metadataGroup.getGroup();
                } else {
                    throw new IllegalStateException("Got simple metadata entry with key \"" + metadataView.getId()
                            + "\" which is declared as substructured key in the rule set.");
                }
                break;
            default:
                throw new IllegalStateException("Too many (" + values.size() + ") complex metadata of type \""
                        + metadataView.getId() + "\" in a single row. Must be 0 or 1 per row.");
        }
        return new FieldedAdditionalDetailsTableRow(tab, this, complexMetadataView, value);
    }

    /**
     * Creates an object to represent a single-row metadata input.
     *
     * @param simpleMetadataView
     *            presentation information about the metadata entry from the
     *            ruleset
     * @param values
     *            the value(s) to be displayed
     * @return a backing bean for the row
     */
    public AdditionalDetailsTableRow createMetadataEntryEdit(SimpleMetadataViewInterface simpleMetadataView,
                                                     Collection<Metadata> values) {
        switch (simpleMetadataView.getInputType()) {
            case MULTIPLE_SELECTION:
            case MULTI_LINE_SINGLE_SELECTION:
            case ONE_LINE_SINGLE_SELECTION:
                return new SelectMetadataTableRow(tab, this, simpleMetadataView, simpleValues(values));
            case BOOLEAN:
                return new BooleanMetadataTableRow(tab, this, simpleMetadataView, oneSimpleValue(values));
            case DATE:
            case INTEGER:
            case MULTI_LINE_TEXT:
            case ONE_LINE_TEXT:
                return new TextMetadataTableRow(tab, this, simpleMetadataView, oneSimpleValue(values));
            default:
                throw new IllegalStateException("complete switch");
        }
    }

    /**
     * Returns the collection of simple metadata entries. Throws an
     * IllegalStateException if a cannot be casted.
     *
     * @param values
     *            values obtained
     * @return a collection of simple metadata entries
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private Collection<MetadataEntry> simpleValues(Collection<Metadata> values) {
        Optional<Metadata> fault = values.parallelStream().filter(entry -> !(entry instanceof MetadataEntry)).findAny();
        if (fault.isPresent()) {
            throw new IllegalStateException("Got complex metadata entry with key \"" + fault.get().getKey()
                    + "\" which isn't declared as substructured key in the rule set.");
        }
        return (Collection) values;
    }

    /**
     * Returns the only metadata entry or null. Throws an IllegalStateException
     * if the value is ambiguous or cannot be casted.
     *
     * @param values
     *            values obtained
     * @return the only entry or null
     */
    private MetadataEntry oneSimpleValue(Collection<Metadata> values) {
        switch (values.size()) {
            case 0:
                return null;
            case 1:
                Metadata nextMetadata = values.iterator().next();
                if (nextMetadata instanceof MetadataEntry) {
                    return (MetadataEntry) nextMetadata;
                } else {
                    throw new IllegalStateException("Got complex metadata entry with key \"" + nextMetadata.getKey()
                            + "\" which isn't declared as substructured key in the rule set.");
                }
            default:
                throw new IllegalStateException("Too many (" + values.size() + ") metadata of type \""
                        + values.iterator().next().getKey() + "\" in a single row. Must be 0 or 1 per row.");
        }
    }

   public void addAdditionallySelectedField(String additionallySelectedField) throws NoSuchMetadataFieldException {
        additionallySelectedFields.add(additionallySelectedField);
        try {
            preserve();
        } catch (InvalidMetadataValueException e) {
            logger.info(e.getLocalizedMessage(), e);
        }
        createMetadataTable();
    }


    @Override
    public String getMetadataID() {
        return metadataView.getId();
    }

    @Override
    public String getInput() {
        return "dataTable";
    }

    /**
     * Returns the metadata of a metadata group, when used recursively.
     *
     * @return the metadata of the metadata group
     * @throws InvalidMetadataValueException
     *             if some value is invalid
     */
    @Override
    public Collection<Metadata> getMetadata() throws InvalidMetadataValueException {
        assert structure == null;
        MetadataGroup result = new MetadataGroup();
        result.setKey(metadataView.getId());
        result.setDomain(DOMAIN_TO_MDSEC.get(metadataView.getDomain().orElse(Domain.DESCRIPTION)));
        try {
            this.preserve();
        } catch (NoSuchMetadataFieldException e) {
            /*
             * The domain attribute is not evaluated on members of metadata
             * groups, so writing to fields can never happen here and thus
             * cannot cause the exception.
             */
            throw new IllegalStateException("never happening exception");
        }
        result.setGroup(metadata instanceof List ? (List<Metadata>) metadata : new ArrayList<>(metadata));
        return Collections.singletonList(result);
    }

    /**
     * Returns the rows that JSF has to display.
     *
     * @return the rows that JSF has to display
     */
    public List<AdditionalDetailsTableRow> getRows() {
        return rows;
    }

    @Override
    Pair<Method, Object> getStructureFieldValue() {
        return null;
    }

    @Override
    public boolean isUndefined() {
        return metadataView.isUndefined();
    }

    @Override
    public boolean isRequired() {
        return metadataView.getMinOccurs() > 0;
    }

    @Override
    public boolean isValid() {
        for (AdditionalDetailsTableRow row : getRows()) {
            if (!row.isValid()) return false;
        }
        return true;
    }

    /**
     * Reads the contents of the metadata panel and stores the values in the
     * appropriate place. If the line is used to edit a field of the METS
     * structure, this field is set, otherwise the metadata will be stored in
     * the list. The hidden metadata is also written back there again.
     *
     * @throws InvalidMetadataValueException
     *             if the content of a metadata input field is syntactically
     *             wrong
     * @throws NoSuchMetadataFieldException
     *             if an input shall be saved to a field of the structure, but
     *             there is no setter corresponding to the name configured in
     *             the rule set
     */
    void preserve() throws InvalidMetadataValueException, NoSuchMetadataFieldException {
        try {
            metadata.clear();
            for (AdditionalDetailsTableRow row : rows) {
                Pair<Method, Object> metsFieldValue = row.getStructureFieldValue();
                if (Objects.nonNull(metsFieldValue)) {
                    try {
                        metsFieldValue.getKey().invoke(structure, metsFieldValue.getValue());
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new IllegalStateException(e);
                    }
                } else {
                    metadata.addAll(row.getMetadata());
                }
            }
            metadata.addAll(hiddenMetadata);
        } catch (InvalidMetadataValueException invalidValueException) {
            if (Objects.isNull(structure)) {
                invalidValueException.addParent(metadataView.getId());
            }
            throw invalidValueException;
        }
    }
}
