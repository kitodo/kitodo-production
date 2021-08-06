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

package org.kitodo.dataeditor.ruleset;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale.LanguageRange;
import java.util.Optional;
import java.util.TreeMap;

import org.kitodo.api.Metadata;
import org.kitodo.api.dataeditor.rulesetmanagement.ComplexMetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.Domain;
import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewWithValuesInterface;
import org.kitodo.dataeditor.ruleset.xml.Key;
import org.kitodo.dataeditor.ruleset.xml.Ruleset;

/**
 * A nested key view opens a view of a nested key. This is a key that breaks
 * down into subkey.
 *
 * @param <D>
 *            Type of declaration. Normally a key declaration, but since the
 *            division view is also a nested key view, it will then be a
 *            division declaration.
 */
class NestedKeyView<D extends KeyDeclaration> extends AbstractKeyView<D> implements ComplexMetadataViewInterface {
    /**
     * Marks keys for which the user has requested an additional blank field.
     * This process can be parallelized because the auxiliary table is not
     * changed. Apart from the parallelization, the implementation has the same
     * meaning with the following code:
     *
     * <pre>
     * for (String additionallySelectedKey : additionallySelectedKeys) {
     *     auxiliaryTable.get(additionallySelectedKey).addOneAdditionalField();
     * }
     * </pre>
     *
     * @param <V>
     *            the type of metadata values
     * @param additionallySelectedKeys
     *            key for which the user has requested an additional blank field
     * @param auxiliaryTable
     *            auxiliary table
     * @throws NullPointerException
     *             When the API caller tries to add a field that does not exist
     *             in the ruleset.
     */
    private static <V> void addFieldsForAdditionallySelectedKeys(Collection<String> additionallySelectedKeys,
                                                                 LinkedHashMap<String,
                                                                         AuxiliaryTableRow> auxiliaryTable) {

        additionallySelectedKeys.parallelStream().map(auxiliaryTable::get)
                .forEach(AuxiliaryTableRow::addOneAdditionalField);
    }

    /**
     * Appends the rows to the auxiliary table. Since the order of the rows is
     * relevant, you cannot parallelize here.
     *
     * @param rows
     *            rows to append
     * @param auxiliaryTable
     *            auxiliary table to append to
     */
    private static void appendRowsToAuxiliaryTable(Collection<AuxiliaryTableRow> rows,
                                                   LinkedHashMap<String, AuxiliaryTableRow> auxiliaryTable) {

        for (AuxiliaryTableRow auxiliaryTableRow : rows) {
            auxiliaryTable.put(auxiliaryTableRow.getId(), auxiliaryTableRow);
        }
    }

    /**
     * Sorts the table by copying it into a TreeMap with the translated label as
     * the key. The label will also be appended with the id if there are ever
     * two metadata keys with the same label in the rule set. Since TreeMap is
     * not thread safe, it cannot be parallelized here.
     *
     * @param <V>
     *            the type of metadata values
     * @param auxiliaryTableToBeSorted
     *            auxiliary table to be sorted
     * @param priorityList
     *            the wish list of the user’s preferred human language
     * @return sorted list of fields
     */
    private static <V> Collection<AuxiliaryTableRow> sort(
            HashMap<String, AuxiliaryTableRow> auxiliaryTableToBeSorted, List<LanguageRange> priorityList) {

        TreeMap<String, AuxiliaryTableRow> sorted = new TreeMap<>();
        for (AuxiliaryTableRow auxiliaryTableRow : auxiliaryTableToBeSorted.values()) {
            sorted.put(auxiliaryTableRow.getLabel(priorityList) + '\037' + auxiliaryTableRow.getId(),
                auxiliaryTableRow);
        }
        return sorted.values();
    }

    /**
     * Whether the nested key view is a division.
     */
    protected boolean division;

    /**
     * The ruleset.
     */
    protected Ruleset ruleset;

    /**
     * The settings.
     */
    protected Settings settings;

    /**
     * Manufacturer for a nested subkey. As you can see from the constructor
     * being private, this shows that it is called only from this class to
     * create a nested subkey view for a nested key view. In that case, this is
     * always a grouped metadata.
     *
     * @param ruleset
     *            the ruleset
     * @param declaration
     *            the declaration
     * @param rule
     *            the rule
     * @param settings
     *            the settings
     * @param priorityList
     *            the user’s wish list for the best possible translation
     */
    public NestedKeyView(Ruleset ruleset, D declaration, Rule rule, Settings settings,
            List<LanguageRange> priorityList) {

        super(declaration, rule, priorityList);
        this.ruleset = ruleset;
        this.settings = settings;
        this.division = false;
    }

    /**
     * Creates a new nested key view. Showing that the constructor is protected
     * shows that it is being used by a subclass, that’s division view. A
     * division view is a nested key view with additional division function.
     *
     * @param ruleset
     *            the ruleset
     * @param divisionDeclaration
     *            the division declaration
     * @param rule
     *            the rule
     * @param settings
     *            the settings
     * @param priorityList
     *            the user’s wish list for the best possible translation
     * @param division
     *            Whether it is a division. Always true. This is a marker
     *            parameter because two different constructors in Java can only
     *            differ in their parameters and otherwise they would be the
     *            same.
     */
    protected NestedKeyView(Ruleset ruleset, D divisionDeclaration, Rule rule, Settings settings,
            List<LanguageRange> priorityList, boolean division) {
        super(divisionDeclaration, rule, priorityList);

        this.ruleset = ruleset;
        this.settings = settings;
        this.division = division;
    }

    /**
     * Adds any permission rules the children of the restriction rule used here
     * are added to the table, as they affect the representation of the
     * children.
     *
     * @param auxiliaryTable
     *            auxiliary table
     */
    private void addAnyRules(LinkedHashMap<String, AuxiliaryTableRow> auxiliaryTable) {

        auxiliaryTable.entrySet().parallelStream().forEach(entry -> entry.getValue()
                .setRule(rule.getRuleForKey(entry.getKey(), division)));
    }

    /**
     * Adds the keys selected by the user to be added to the list of keys to
     * add. This procedure is a part of the function to
     * {@link #createAuxiliaryTableWithKeysToBeSorted(LinkedHashMap, Rule, Collection, Collection)}.
     *
     * @param auxiliaryTable
     *            the target table. If the key is already here, it will not be
     *            added a second time.
     * @param rule
     *            the rule provides the key
     * @param additionalKeys
     *            list of the key marked as being added by the user
     * @param toBeSorted
     *            write access to the list of keys to be sorted
     */
    private void addAdditionalKeys(LinkedHashMap<String, AuxiliaryTableRow> auxiliaryTable,
                                   Rule rule, Collection<String> additionalKeys,
                                   HashMap<String, AuxiliaryTableRow> toBeSorted) {
        for (String additionalKey : additionalKeys) {
            if (!auxiliaryTable.containsKey(additionalKey) && !toBeSorted.containsKey(additionalKey)) {
                Optional<Key> optionalKey = rule.isUnspecifiedUnrestricted() ? Optional.empty()
                        : ruleset.getKey(additionalKey);
                KeyDeclaration keyDeclaration = optionalKey.map(key -> new KeyDeclaration(ruleset, key))
                        .orElseGet(() -> new KeyDeclaration(ruleset, additionalKey));
                toBeSorted.put(additionalKey, new AuxiliaryTableRow(keyDeclaration, settings));
            }
        }
    }

    /**
     * If the rule is unspecified unrestricted, the remaining keys are added to
     * the keys to be sorted. This procedure is a part of the function to
     * {@link #createAuxiliaryTableWithKeysToBeSorted(LinkedHashMap, Rule, Collection, Collection)}.
     *
     * @param auxiliaryTable
     *            the target table. If the key is already here, it will not be
     *            added a second time.
     * @param keyDeclarations
     *            the keys to be checked
     * @param toBeSorted
     *            write access to the list of keys to be sorted
     */
    private void addRemainingKeys(LinkedHashMap<String, AuxiliaryTableRow> auxiliaryTable,
                                  Collection<KeyDeclaration> keyDeclarations,
                                  HashMap<String, AuxiliaryTableRow> toBeSorted) {
        for (KeyDeclaration keyDeclaration : keyDeclarations) {
            if (!auxiliaryTable.containsKey(keyDeclaration.getId())) {
                toBeSorted.put(keyDeclaration.getId(), new AuxiliaryTableRow(keyDeclaration, settings));
            }
        }
    }

    /**
     * Generates an auxiliary table for additional keys. These will later be
     * sorted alphabetically and the key will be added below the given order.
     * This table contains values if there is no restriction rule or if the
     * restriction rule specifies unspecified as unrestricted. Otherwise the
     * table is empty for now. However, it can still receive elements in the
     * further course, namely when metadata entries name a key that is not
     * specified in the rule set. Then in this table namely for undefined key
     * views are created.
     *
     * @param auxiliaryTable
     *            table with already pre-sorted keys. Keys that are in this
     *            table are not included in the table of keys yet to be sorted.
     * @param rule
     *            optionally a rule
     * @param keyDeclarations
     *            all key declarations
     * @param additionalKeys
     *            which keys the user has additionally selected
     * @return an auxiliary table for additional keys yet to be sorted
     */
    private HashMap<String, AuxiliaryTableRow> createAuxiliaryTableWithKeysToBeSorted(
            LinkedHashMap<String, AuxiliaryTableRow> auxiliaryTable, Rule rule,
            Collection<KeyDeclaration> keyDeclarations, Collection<String> additionalKeys) {

        HashMap<String, AuxiliaryTableRow> toBeSorted = new HashMap<>();

        if (rule.isUnspecifiedUnrestricted()) {
            addRemainingKeys(auxiliaryTable, keyDeclarations, toBeSorted);
        }
        addAdditionalKeys(auxiliaryTable, rule, additionalKeys, toBeSorted);
        return toBeSorted;
    }

    /**
     * This is actually a major part of the functionality of this class that
     * generates the helper table. You can see that here the individual
     * functions are called from the class.
     *
     * @param currentEntries
     *            which metadata are present in the view
     * @param additionalKeys
     *            which keys the user has additionally selected
     * @return A helper table for all metadata keys
     */
    private Collection<AuxiliaryTableRow> createAuxiliaryTable(Collection<Metadata> currentEntries,
            Collection<String> additionalKeys) {

        LinkedHashMap<String, AuxiliaryTableRow> auxiliaryTable = createAuxiliaryTableWithPreSortedKeys(
            rule.getExplicitlyPermittedKeys(declaration));
        HashMap<String, AuxiliaryTableRow> auxiliaryTableToBeSorted = createAuxiliaryTableWithKeysToBeSorted(
            auxiliaryTable, rule, declaration.getKeyDeclarations(), additionalKeys);
        storeValues(currentEntries, auxiliaryTable, auxiliaryTableToBeSorted);
        appendRowsToAuxiliaryTable(sort(auxiliaryTableToBeSorted, priorityList), auxiliaryTable);
        addAnyRules(auxiliaryTable);
        addFieldsForAdditionallySelectedKeys(additionalKeys, auxiliaryTable);
        return auxiliaryTable.values();
    }

    /**
     * Generates the auxiliary table with the keys already specified by the
     * restriction rule in their order. Since the order of the keys is relevant,
     * you cannot parallelize them here.
     *
     * @param explicitlyPermittedKeys
     *            keys given in their order by the restriction rule
     * @return the auxiliary table
     */
    private LinkedHashMap<String, AuxiliaryTableRow> createAuxiliaryTableWithPreSortedKeys(
            List<KeyDeclaration> explicitlyPermittedKeys) {

        LinkedHashMap<String, AuxiliaryTableRow> auxiliaryTable = new LinkedHashMap<>();
        for (KeyDeclaration keyDeclaration : explicitlyPermittedKeys) {
            auxiliaryTable.put(keyDeclaration.getId(), new AuxiliaryTableRow(keyDeclaration, settings));
        }
        return auxiliaryTable;
    }

    /**
     * Here the addable metadata keys are fetched. To do this, the table must
     * first be made and then an output made.
     *
     * @param currentEntries
     *            metadata objects that have already been entered, along with
     *            their key
     * @param additionalKeys
     *            metadata keys that the user has already selected
     */
    @Override
    public Collection<MetadataViewInterface> getAddableMetadata(Collection<Metadata> currentEntries,
            Collection<String> additionalKeys) {

        return getPossibleMetadata(currentEntries, additionalKeys, false);
    }

    private Collection<MetadataViewInterface> getPossibleMetadata(Collection<Metadata> currentEntries,
            Collection<String> additionalKeys, boolean all) {

        Collection<MetadataViewInterface> addableMetadata = new LinkedList<>();
        for (AuxiliaryTableRow auxiliaryTableRow : createAuxiliaryTable(currentEntries, additionalKeys)) {
            if (all || auxiliaryTableRow.isPossibleToExpandAnotherField()) {
                addableMetadata.add(rowToView(auxiliaryTableRow));
            }
        }
        return addableMetadata;
    }

    /**
     * Returns a full list of all metadata entries that are allowed as children
     * of this nested key view.
     *
     * @return all allowed children
     */
    @Override
    public Collection<MetadataViewInterface> getAllowedMetadata() {
        return getPossibleMetadata(Collections.emptyList(), Collections.emptySet(), true);
    }

    /**
     * Creates a metadata view for one line of the auxiliary table.
     *
     * @param row
     *            row to make a view for
     * @return metadata view
     */
    private MetadataViewInterface rowToView(AuxiliaryTableRow row) {
        return row.isComplexKey() ? getNestedKeyView(row.getId())
                : new KeyView(row.getKey(), rule.getRuleForKey(row.getId(), division), settings, priorityList);
    }

    /**
     * Creates a key view for a grouped key. This is the case when when
     * {@code <key>} elements occur within another {@code <key>} element in the
     * ruleset.
     *
     * @param keyId
     *            identifier for key in the nest
     * @return a view on the child of the group
     */
    private NestedKeyView<KeyDeclaration> getNestedKeyView(String keyId) {
        Rule ruleForKey = rule.getRuleForKey(keyId, division);
        if (division) {
            ruleForKey.merge(ruleset.getRuleForKey(keyId));
        }
        return new NestedKeyView<>(ruleset, declaration.getSubkeyDeclaration(keyId), ruleForKey,
                settings.getSettingsForKey(keyId), priorityList);
    }

    /**
     * Sorts and visibly outputs the metadata. That’s the main function of the
     * mask. The structure of metadata mask is like this: If there is rule,
     * then the order of elements is by rule. But if the rule allows more
     * elements or if there are data elements that are not in the rule, then
     * they come in alphabetical order below them. The metadata elements must
     * be sorted and therefore sometimes more fields are needed. Exceptions here
     * are multiple selections only once, and with multiple values.
     *
     * @param currentEntries
     *            metadata objects that have already been entered, along with
     *            heir key
     * @param additionalKeys
     *            metadata keys that the user has already selected
     * @return mask
     */
    @Override
    public List<MetadataViewWithValuesInterface> getSortedVisibleMetadata(Collection<Metadata> currentEntries,
            Collection<String> additionalKeys) {

        LinkedList<MetadataViewWithValuesInterface> sortedVisibleMetadata = new LinkedList<>();
        Collection<Metadata> excludedDataObjects = new HashSet<>();
        for (AuxiliaryTableRow auxiliaryTableRow : createAuxiliaryTable(currentEntries, additionalKeys)) {
            if (auxiliaryTableRow.isContainingExcludedData()) {
                excludedDataObjects.addAll(auxiliaryTableRow.getDataObjects(0));
            } else {
                for (int i = 0; i < auxiliaryTableRow.getNumberOfTypeViewsToGenerate(); i++) {
                    Optional<MetadataViewInterface> definedTypeView = Optional.of(rowToView(auxiliaryTableRow));
                    sortedVisibleMetadata.add(new FormRow(definedTypeView, auxiliaryTableRow.getDataObjects(i)));
                }
            }
        }
        if (!excludedDataObjects.isEmpty()) {
            sortedVisibleMetadata.addFirst(new FormRow(Optional.empty(), excludedDataObjects));
        }
        return sortedVisibleMetadata;
    }

    /**
     * Adds the already entered metadata to the helper tables. For metadata
     * that does not belong to any field, an undefined field is created. Since
     * LinkedHashMap is not thread safe, it cannot be parallelized here.
     *
     * @param enteredMetaData
     *            already entered metadata to be added to the auxiliary tables
     * @param sortedAuxiliaryTable
     *            help table with rows with default sorting
     * @param auxiliaryTableToBeSorted
     *            help table with rows that still have to be sorted
     */
    private void storeValues(Collection<Metadata> enteredMetaData,
                             LinkedHashMap<String, AuxiliaryTableRow> sortedAuxiliaryTable,
                             HashMap<String, AuxiliaryTableRow> auxiliaryTableToBeSorted) {

        for (Metadata metadata : enteredMetaData) {
            String keyId = metadata.getKey();
            if (sortedAuxiliaryTable.containsKey(keyId)) {
                sortedAuxiliaryTable.get(keyId).add(metadata);
            } else {
                auxiliaryTableToBeSorted.computeIfAbsent(keyId,
                    missing -> retrieveOrCompute(keyId));
                auxiliaryTableToBeSorted.get(keyId).add(metadata);
            }
        }
    }

    private AuxiliaryTableRow retrieveOrCompute(String keyId) {
        Optional<KeyDeclaration> optionalKeyDeclaration = super.declaration.getKeyDeclarations().parallelStream()
                .filter(childKeyDeclaration -> keyId.equals(childKeyDeclaration.getId())).findAny();
        KeyDeclaration keyDeclaration = optionalKeyDeclaration.orElseGet(() -> new KeyDeclaration(ruleset, keyId));
        return new AuxiliaryTableRow(keyDeclaration, settings);
    }

    @Override
    public Optional<Domain> getDomain() {
        return declaration.getDomain();
    }
}
