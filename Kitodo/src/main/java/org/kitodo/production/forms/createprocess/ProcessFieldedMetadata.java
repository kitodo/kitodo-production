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
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.list.UnmodifiableList;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.MetadataGroup;
import org.kitodo.api.dataeditor.rulesetmanagement.ComplexMetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.Domain;
import org.kitodo.api.dataeditor.rulesetmanagement.InputType;
import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewWithValuesInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.SimpleMetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.api.dataformat.Division;
import org.kitodo.exceptions.InvalidMetadataValueException;
import org.kitodo.exceptions.NoSuchMetadataFieldException;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

public class ProcessFieldedMetadata extends ProcessDetail implements Serializable {
    private static final Logger logger = LogManager.getLogger(ProcessFieldedMetadata.class);

    /**
     * An empty metadata group for the empty metadata panel showing. The empty
     * metadata panel can be displayed if the element selected in the structure
     * window isn’t a structure (has no metadata).
     */
    public static final ProcessFieldedMetadata EMPTY = new ProcessFieldedMetadata();

    /**
     * Fields the user has selected to show in addition, with no data yet.
     */
    private final Collection<String> additionallySelectedFields = new ArrayList<>();

    private boolean copy;

    /**
     * The division this panel is related to, if it isn’t a sub-panel.
     */
    private Division<?> division;

    /**
     * Metadata which is excluded by the rule set.
     */
    private Collection<Metadata> hiddenMetadata;

    /**
     * The metadata object with the content of this panel.
     */
    private final Collection<Metadata> metadata;

    /**
     * The key of the metadata group displaying here.
     */
    private String metadataKey;

    /**
     * The definition of this panel in the rule set.
     */
    private ComplexMetadataViewInterface metadataView;

    /**
     * The tree node that JSF has to display.
     */
    private TreeNode treeNode;

    /**
     * Creates an empty metadata group.
     */
    public ProcessFieldedMetadata() {
        super(null, null);
        this.treeNode = new DefaultTreeNode();
        treeNode.setExpanded(true);
        this.metadata = Collections.emptyList();
        this.hiddenMetadata = Collections.emptyList();
    }

    /**
     * Creates a new root metadata group representing the metadata table
     * content in the processMetadataTab.
     *
     * @param structure
     *            structure selected by the user
     * @param divisionView
     *            information about that structure from the rule set
     */
    public ProcessFieldedMetadata(Division<?> structure, StructuralElementViewInterface divisionView) {
        this(null, structure, divisionView, null, null, structure.getMetadata());
        this.treeNode = new DefaultTreeNode();
        treeNode.setExpanded(true);
        createMetadataTable();
    }

    /**
     * Creates a sub-panel for a metadata group.
     *
     * @param metadataView
     *            information about that group from the rule set
     * @param metadata
     *            data of the group, may be empty but must be modifiable
     */
    private ProcessFieldedMetadata(ProcessFieldedMetadata parent, ComplexMetadataViewInterface metadataView,
            Collection<Metadata> metadata) {
        this(parent, null, metadataView, metadataView.getLabel(), metadataView.getId(), metadata);
    }

    /**
     * Creates a new fielded metadata. This constructor is called from one of
     * the above ones and does the work.
     *
     * @param structure
     *            structure selected by the user, null in case of a sub-panel
     * @param metadataView
     *            information about that structure or group from the rule set
     * @param metadata
     *            metadata, may be empty but must be modifiable
     */
    private ProcessFieldedMetadata(ProcessFieldedMetadata parent, Division<?> structure,
            ComplexMetadataViewInterface metadataView, String label, String metadataKey,
                                   Collection<Metadata> metadata) {
        super(parent, label);
        this.division = structure;
        this.metadata = metadata;
        this.metadataView = metadataView;
        this.metadataKey = metadataKey;
    }

    private ProcessFieldedMetadata(ProcessFieldedMetadata parent, MetadataGroup group) {
        this(parent, null, null, group.getKey(), group.getKey(), group.getGroup());
    }

    private ProcessFieldedMetadata(ProcessFieldedMetadata template) {
        this(template.container, null, template.metadataView, template.label, template.metadataKey,
                new ArrayList<>(template.metadata));
        copy = true;
    }

    /**
     * The method for building the metadata table.
     */
    private void createMetadataTable() {
        // the existing metadata is passed to the rule set, which sorts it
        Map<Metadata, String> metadataWithKeys = addLabels(metadata).parallelStream()
                .collect(Collectors.toMap(Function.identity(), Metadata::getKey, (duplicate1, duplicate2) -> duplicate1));
        List<MetadataViewWithValuesInterface<Metadata>> tableData = metadataView
                .getSortedVisibleMetadata(metadataWithKeys, additionallySelectedFields);

        treeNode.getChildren().clear();
        hiddenMetadata = Collections.emptyList();
        for (MetadataViewWithValuesInterface<Metadata> rowData : tableData) {
            Optional<MetadataViewInterface> optionalMetadataView = rowData.getMetadata();
            Collection<Metadata> values = rowData.getValues();
            if (optionalMetadataView.isPresent()) {
                MetadataViewInterface metadataView = optionalMetadataView.get();
                if (metadataView.isComplex()) {
                    createMetadataGroupPanel((ComplexMetadataViewInterface) metadataView, values);
                } else {
                    createMetadataEntryEdit((SimpleMetadataViewInterface) metadataView, values);
                }
            } else {
                hiddenMetadata = values;
            }
        }
    }

    /**
     * The method for building the metadata table if the group and thus
     * everything in it is undefined in the ruleset.
     */
    private void createUndefinedMetadataTable() {
        treeNode.getChildren().clear();
        hiddenMetadata = Collections.emptyList();
        for (Metadata entry : metadata) {
            createMetadataEntryEdit(null, Collections.singletonList(entry));
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
        if (Objects.nonNull(division)) {
            displayMetadata = new ArrayList<>(metadata);
            for (URI contentId : division.getContentIds()) {
                MetadataEntry contentIdEntry = new MetadataEntry();
                contentIdEntry.setKey("CONTENTIDS");
                contentIdEntry.setValue(contentId.toString());
                displayMetadata.add(contentIdEntry);
            }
            if (Objects.nonNull(division.getLabel())) {
                MetadataEntry label = new MetadataEntry();
                label.setKey("LABEL");
                label.setValue(division.getLabel());
                displayMetadata.add(label);
            }
            if (Objects.nonNull(division.getOrderlabel())) {
                MetadataEntry label = new MetadataEntry();
                label.setKey("ORDERLABEL");
                label.setValue(division.getOrderlabel());
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
     */
    public void createMetadataGroupPanel(ComplexMetadataViewInterface complexMetadataView,
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
                    throw new IllegalStateException("Got simple metadata entry with key \"" + metadataKey
                            + "\" which is declared as substructured key in the rule set.");
                }
                break;
            default:
                throw new IllegalStateException("Too many (" + values.size() + ") complex metadata of type \""
                        + metadataKey + "\" in a single row. Must be 0 or 1 per row.");
        }
        ProcessFieldedMetadata metadata = new ProcessFieldedMetadata(this, complexMetadataView, value);
        metadata.treeNode = new DefaultTreeNode(metadata, treeNode);
        metadata.createMetadataTable();
        metadata.treeNode.setExpanded(true);
    }

    /**
     * Creates an object to represent a single-row metadata input.
     *
     * @param simpleMetadataView
     *            presentation information about the metadata entry from the
     *            ruleset
     * @param values
     *            the value(s) to be displayed
     */
    public void createMetadataEntryEdit(SimpleMetadataViewInterface simpleMetadataView,
                                                 Collection<Metadata> values) {

        ProcessDetail data;
        try {
            InputType inputType = Objects.isNull(simpleMetadataView) ? InputType.ONE_LINE_TEXT
                    : simpleMetadataView.getInputType();
            switch (inputType) {
                case MULTIPLE_SELECTION:
                case MULTI_LINE_SINGLE_SELECTION:
                case ONE_LINE_SINGLE_SELECTION:
                    data = new ProcessSelectMetadata(this, simpleMetadataView, simpleValues(values));
                    break;
                case BOOLEAN:
                    data = new ProcessBooleanMetadata(this, simpleMetadataView, oneValue(values, MetadataEntry.class));
                    break;
                case DATE:
                case INTEGER:
                case MULTI_LINE_TEXT:
                case ONE_LINE_TEXT:
                    data = new ProcessTextMetadata(this, simpleMetadataView, oneValue(values, MetadataEntry.class));
                    break;
                default:
                    throw new IllegalStateException("complete switch");
            }
            new DefaultTreeNode(data, treeNode).setExpanded(true);
        } catch (IllegalStateException e) {
            ProcessFieldedMetadata metadata = new ProcessFieldedMetadata(this, oneValue(values, MetadataGroup.class));
            metadata.treeNode = new DefaultTreeNode(metadata, treeNode);
            metadata.createUndefinedMetadataTable();
            metadata.treeNode.setExpanded(true);
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
     * @param <T>
     *
     * @param values
     *            values obtained
     * @return the only entry or null
     */
    @SuppressWarnings("unchecked")
    private <T extends Metadata> T oneValue(Collection<Metadata> values, Class<T> subclass) {
        switch (values.size()) {
            case 0:
                return null;
            case 1:
                Metadata nextMetadata = values.iterator().next();
                if (subclass.isAssignableFrom(nextMetadata.getClass())) {
                    return (T) nextMetadata;
                } else {
                    throw new IllegalStateException("Got complex metadata entry with key \"" + nextMetadata.getKey()
                            + "\" which isn't declared as substructured key in the rule set.");
                }
            default:
                throw new IllegalStateException("Too many (" + values.size() + ") metadata of type \""
                        + values.iterator().next().getKey() + "\" in a single row. Must be 0 or 1 per row.");
        }
    }

    /**
     * Adds an additionally selected field.
     *
     * @param additionallySelectedField
     *            additionally selected field to add
     * @throws NoSuchMetadataFieldException
     *             if the method has to save the entries in order to rebuild the
     *             display with the new field, but when saving an attempt is
     *             made to write a non-existing &lt;mets:div> attribute
     */
    public void addAdditionallySelectedField(String additionallySelectedField) throws NoSuchMetadataFieldException {
        additionallySelectedFields.add(additionallySelectedField);
        try {
            preserve();
        } catch (InvalidMetadataValueException e) {
            logger.info(e.getLocalizedMessage(), e);
        }
        createMetadataTable();
    }

    /**
     * Duplicates a process detail.
     *
     * @param processDetail
     *            process detail to copy
     */
    public void copy(ProcessDetail processDetail) {
        if (Objects.isNull(division)) {
            container.copy(processDetail);
        } else {
            searchRecursiveAndCopy(treeNode, processDetail);
        }
    }

    private static boolean searchRecursiveAndCopy(TreeNode treeNode, ProcessDetail processDetail) {
        List<TreeNode> children = treeNode.getChildren();
        for (int index = 0; index < children.size(); index++) {
            TreeNode child = children.get(index);
            Object childData = child.getData();
            if (Objects.equals(childData, processDetail)) {
                Object copyData = null;
                if (childData instanceof ProcessSimpleMetadata) {
                    copyData = ((ProcessSimpleMetadata) childData).getClone();
                } else if (childData instanceof ProcessFieldedMetadata) {
                    copyData = new ProcessFieldedMetadata((ProcessFieldedMetadata) childData);
                }
                TreeNode copy = new DefaultTreeNode(copyData);
                copy.setParent(treeNode);
                copy.setExpanded(child.isExpanded());
                treeNode.getChildren().add(index + 1, copy);
            } else if (searchRecursiveAndCopy(child, processDetail)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getMetadataID() {
        return metadataKey;
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
        assert division == null;
        MetadataGroup result = new MetadataGroup();
        result.setKey(metadataKey);
        result.setDomain(DOMAIN_TO_MDSEC.get(metadataView.getDomain().orElse(Domain.DESCRIPTION)));
        try {
            this.preserve();
        } catch (NoSuchMetadataFieldException e) {
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
    public List<ProcessDetail> getRows() {
        List<ProcessDetail> rows = new ArrayList<>();
        for (TreeNode child : treeNode.getChildren()) {
            rows.add((ProcessDetail) child.getData());
        }
        return new UnmodifiableList<>(rows);
    }

    @Override
    Pair<BiConsumer<Division<?>, String>, String> getStructureFieldValue() {
        return null;
    }

    public TreeNode getTreeNode() {
        return treeNode;
    }

    @Override
    public boolean isUndefined() {
        return Objects.isNull(metadataView) || metadataView.isUndefined();
    }

    @Override
    public boolean isRequired() {
        return Objects.nonNull(metadataView) && metadataView.getMinOccurs() > 0;
    }

    @Override
    public boolean isValid() {
        for (ProcessDetail row : getRows()) {
            if (!row.isValid()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Reads the contents of the processMetadataTab and stores the values in the
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
    public void preserve() throws InvalidMetadataValueException, NoSuchMetadataFieldException {
        try {
            if (Objects.nonNull(division)) {
                division.getContentIds().clear();
            }
            metadata.clear();
            for (TreeNode child : treeNode.getChildren()) {
                ProcessDetail row = (ProcessDetail) child.getData();
                Pair<BiConsumer<Division<?>, String>, String> metsFieldValue = row.getStructureFieldValue();
                if (Objects.nonNull(metsFieldValue)) {
                    metsFieldValue.getKey().accept(division, metsFieldValue.getValue());
                } else {
                    metadata.addAll(row.getMetadata());
                }
            }
            metadata.addAll(hiddenMetadata);
        } catch (InvalidMetadataValueException invalidValueException) {
            if (Objects.isNull(division)) {
                invalidValueException.addParent(metadataKey);
            }
            throw invalidValueException;
        }
        if (copy) {
            MetadataGroup metadataGroup = new MetadataGroup();
            Optional<Domain> optionalDomain = metadataView.getDomain();
            optionalDomain.ifPresent(domain -> metadataGroup.setDomain(DOMAIN_TO_MDSEC.get(domain)));
            metadataGroup.setGroup(metadata);
            container.metadata.add(metadataGroup);
            copy = false;
        }
    }

    /**
     * Removes a process detail.
     *
     * @param toDelete
     *            process detail to delete
     */
    public void remove(ProcessDetail toDelete) {
        Iterator<TreeNode> treeNodesIterator = treeNode.getChildren().iterator();
        while (treeNodesIterator.hasNext()) {
            TreeNode treeNode = treeNodesIterator.next();
            if (treeNode.getData().equals(toDelete)) {
                treeNodesIterator.remove();
                break;
            }
        }
    }

    /**
     * Overwrites the metadata of this process fielded metadata.
     *
     * @param metadata
     *            metadata to overwrite with
     */
    public void setMetadata(Collection<Metadata> metadata) {
        this.metadata.clear();
        this.metadata.addAll(metadata);
        this.treeNode = new DefaultTreeNode();
        treeNode.setExpanded(true);
        createMetadataTable();
    }
}
