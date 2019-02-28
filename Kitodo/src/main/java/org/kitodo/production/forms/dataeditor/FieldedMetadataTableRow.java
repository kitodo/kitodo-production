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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.faces.model.SelectItem;

import org.apache.commons.lang3.tuple.Pair;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.MetadataGroup;
import org.kitodo.api.dataeditor.rulesetmanagement.ComplexMetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.Domain;
import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewWithValuesInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.SimpleMetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.api.dataformat.Structure;
import org.kitodo.production.helper.Helper;

/**
 * Represents the meta-data panel in the meta-data editor, or a meta-data group
 * within that panel. Basically both are the same, with the exception that the
 * label of the meta-data panel is not used.
 */
public class FieldedMetadataTableRow extends MetadataTableRow {

    /**
     * An empty meta-data panel showing.
     */
    static final FieldedMetadataTableRow EMPTY = new FieldedMetadataTableRow();

    /**
     * Fields the user has selected to show in addition, with no data yet.
     */
    private Collection<String> additionallySelectedFields = new ArrayList<>();

    /**
     * Meta-data which is excluded by the rule set.
     */
    private Collection<Metadata> hiddenMetadata;

    /**
     * The meta-data object with the content of this panel.
     */
    private Collection<Metadata> metadata;

    /**
     * The definition of this panel in the rule set.
     */
    private ComplexMetadataViewInterface metadataView;

    /**
     * The rows that JSF has to display.
     */
    private List<MetadataTableRow> rows = new ArrayList<>();

    /**
     * The structure this panel is related to, if it isn’t a sub-panel.
     */
    private Structure structure;

    /**
     * Creates an empty meta-data panel. This constructor is used to create the
     * ‘EMPTY’ constant above. An empty panel contains no rows and calling
     * {@link #preserve()} does nothing.
     */
    private FieldedMetadataTableRow() {
        super(null, null, null);
        this.rows = Collections.emptyList();
        this.metadata = Collections.emptyList();
        this.hiddenMetadata = Collections.emptyList();
    }

    /**
     * Creates a new meta-data panel to show in the meta-data editor.
     *
     * @param structure
     *            structure selected by the user
     * @param divisionView
     *            information about that structure from the rule set
     */
    FieldedMetadataTableRow(DataEditorForm dataEditor, Structure structure,
            StructuralElementViewInterface divisionView) {
        this(dataEditor, null, structure, divisionView, structure.getMetadata());
    }

    /**
     * Creates a sub-panel for a meta-data group.
     *
     * @param metadataView
     *            information about that group from the rule set
     * @param metadata
     *            data of the group, may be empty but must be modifiable
     */
    private FieldedMetadataTableRow(DataEditorForm dataEditor, FieldedMetadataTableRow container,
            ComplexMetadataViewInterface metadataView, Collection<Metadata> metadata) {
        this(dataEditor, container, null, metadataView, metadata);
    }

    /**
     * Creates a new fielded meta-data panel. This constructor is called from
     * one of the above ones and does the work.
     *
     * @param structure
     *            structure selected by the user, null in case of a sub-panel
     * @param metadataView
     *            information about that structure or group from the rule set
     * @param metadata
     *            meta-data, may be empty but must be modifiable
     */
    private FieldedMetadataTableRow(DataEditorForm dataEditor, FieldedMetadataTableRow container, Structure structure,
            ComplexMetadataViewInterface metadataView, Collection<Metadata> metadata) {
        super(dataEditor, container, metadataView.getId());
        this.structure = structure;
        this.metadata = metadata;
        this.metadataView = metadataView;
        createMetadataTable();
    }

    private final void createMetadataTable() {
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
     * returned meta-data collection.
     *
     * @param metadata
     *            available meta-data
     * @return meta-data with labels, if any
     */
    private final Collection<Metadata> addLabels(Collection<Metadata> metadata) {
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
     * Creates an object to represent a meta-data group. This is done by
     * creating a {@code FieldedMetadataGroup} recursively.
     *
     * @param complexMetadataView
     *            information about that group from the rule set
     * @param values
     *            data for that group, must contain at most one element
     * @return a sub-panel for JSF to render
     */
    private final FieldedMetadataTableRow createMetadataGroupPanel(ComplexMetadataViewInterface complexMetadataView,
            Collection<Metadata> values) {
        Collection<Metadata> value;
        switch (values.size()) {
            case 0:
                value = new ArrayList<>();
                break;
            case 1: {
                Metadata metadata = values.iterator().next();
                if (metadata instanceof MetadataGroup) {
                    MetadataGroup metadataGroup = (MetadataGroup) metadata;
                    value = metadataGroup.getGroup();
                } else {
                    throw new IllegalStateException("Got simple meta-data entry with key \"" + metadataView.getId()
                            + "\" which is declared as substructured key in the rule set.");
                }
                break;
            }
            default:
                throw new IllegalStateException("Too many (" + values.size() + ") complex meta-data of type \""
                        + metadataView.getId() + "\" in a single row. Must be 0 or 1 per row.");
        }
        return new FieldedMetadataTableRow(dataEditor, this, complexMetadataView, value);
    }

    private final MetadataTableRow createMetadataEntryEdit(SimpleMetadataViewInterface simpleMetadataView,
            Collection<Metadata> values) {
        switch (simpleMetadataView.getInputType()) {
            case MULTIPLE_SELECTION:
            case MULTI_LINE_SINGLE_SELECTION:
            case ONE_LINE_SINGLE_SELECTION:
                return new SelectMetadataTableRow(dataEditor, this, simpleMetadataView, simpleValues(values));
            case BOOLEAN:
                return new BooleanMetadataTableRow(dataEditor, this, simpleMetadataView, oneSimpleValue(values));
            case DATE:
            case INTEGER:
            case MULTI_LINE_TEXT:
            case ONE_LINE_TEXT:
                return new TextMetadataTableRow(dataEditor, this, simpleMetadataView, oneSimpleValue(values));
            default:
                throw new IllegalStateException("complete switch");
        }
    }

    /**
     * Returns the collection of simple meta-data entries. Throws an
     * IllegalStateException if a cannot be casted.
     *
     * @param values
     *            values obtained
     * @return a collection of simple meta-data entries
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final Collection<MetadataEntry> simpleValues(Collection<Metadata> values) {
        Optional<Metadata> fault = values.parallelStream().filter(entry -> !(entry instanceof MetadataEntry)).findAny();
        if (fault.isPresent()) {
            throw new IllegalStateException("Got complex meta-data entry with key \"" + fault.get().getKey()
                    + "\" which isn't declared as substructured key in the rule set.");
        }
        return (Collection<MetadataEntry>) (Collection) values;
    }

    /**
     * Returns the only meta-data entry or null. Throws an IllegalStateException
     * if the value is ambiguous or cannot be casted.
     *
     * @param values
     *            values obtained
     * @return the only entry or null
     */
    private final MetadataEntry oneSimpleValue(Collection<Metadata> values) {
        switch (values.size()) {
            case 0:
                return null;
            case 1: {
                Metadata metadata = values.iterator().next();
                if (metadata instanceof MetadataEntry) {
                    return (MetadataEntry) metadata;
                } else {
                    throw new IllegalStateException("Got complex meta-data entry with key \"" + metadata.getKey()
                            + "\" which isn't declared as substructured key in the rule set.");
                }
            }
            default:
                throw new IllegalStateException("Too many (" + values.size() + ") meta-data of type \""
                        + values.iterator().next().getKey() + "\" in a single row. Must be 0 or 1 per row.");
        }
    }

    void addAdditionallySelectedField(String additionallySelectedField)
            throws InvalidMetadataValueException, NoSuchMetadataFieldException {
        additionallySelectedFields.add(additionallySelectedField);
        preserve();
        createMetadataTable();
    }

    public List<SelectItem> getAddableMetadata() {
        Map<Metadata, String> metadataWithKeys = addLabels(metadata).parallelStream()
                .collect(Collectors.toMap(Function.identity(), Metadata::getKey));
        return metadataView.getAddableMetadata(metadataWithKeys, additionallySelectedFields).stream()
                .map(addable -> new SelectItem(addable.getId(), addable.getLabel())).collect(Collectors.toList());
    }

    @Override
    public String getInput() {
        return "dataTable";
    }

    /**
     * Returns the meta-data of a meta-data group, when used recursively.
     *
     * @return the meta-data of the meta-data group
     * @throws InvalidMetadataValueException
     *             if some value is invalid
     */
    @Override
    Collection<Metadata> getMetadata() throws InvalidMetadataValueException {
        assert structure == null;
        MetadataGroup result = new MetadataGroup();
        result.setKey(metadataView.getId());
        result.setDomain(DOMAIN_TO_MDSEC.get(metadataView.getDomain().orElse(Domain.DESCRIPTION)));
        try {
            this.preserve();
        } catch (NoSuchMetadataFieldException e) {
            /*
             * The domain attribute is not evaluated on members of meta-data
             * groups, so writing to fields can never happen here and thus
             * cannot cause the exception.
             */
            throw new IllegalStateException("never happening exception");
        }
        result.setGroup(metadata instanceof List ? (List<Metadata>) metadata : new ArrayList<>(metadata));
        return Arrays.asList(result);
    }

    public List<MetadataTableRow> getRows() {
        return rows;
    }

    @Override
    public boolean isUndefined() {
        return metadataView.isUndefined();
    }

    /**
     * Reads the contents of the meta-data panel and stores the values in the
     * appropriate place. If the line is used to edit a field of the METS
     * structure, this field is set, otherwise the meta-data will be stored in
     * the list. The hidden meta-data is also written back there again.
     *
     * @throws InvalidMetadataValueException
     *             if the content of a meta-data input field is syntactically
     *             wrong
     * @throws NoSuchMetadataFieldException
     *             if an input shall be saved to a field of the structure, but
     *             there is no setter corresponding to the name configured in
     *             the rule set
     */
    void preserve() throws InvalidMetadataValueException, NoSuchMetadataFieldException {
        try {
            metadata.clear();
            for (MetadataTableRow row : rows) {
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

    void remove(MetadataTableRow rowToDelete) {
        rows.remove(rowToDelete);
    }

    public void pasteClick() {
        try {
            Collection<Metadata> clipboard = dataEditor.getClipboard();
            preserve();
            metadata.addAll(clipboard);
            clipboard.clear();
            createMetadataTable();
        } catch (Exception e) {
            Helper.setErrorMessage(e.getLocalizedMessage());
        }
    }
}
