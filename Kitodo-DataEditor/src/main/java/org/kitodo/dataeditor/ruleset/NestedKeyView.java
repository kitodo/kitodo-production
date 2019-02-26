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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale.LanguageRange;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;

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
 * @param <U>
 *            Type of universal key. Normally a universal key, but since the
 *            divisional view is also a nested key view, it will then be a
 *            universal division.
 */
class NestedKeyView<U extends UniversalKey> extends AbstractKeyView<U> implements ComplexMetadataViewInterface {
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
     *            the type of meta-data values
     * @param additionallySelectedKeys
     *            key for which the user has requested an additional blank field
     * @param auxiliaryTable
     *            auxiliary table
     * @throws NullPointerException
     *             When the API caller tries to add a field that does not exist
     *             in the ruleset.
     */
    private static final <V> void addFieldsForAdditionallySelectedKeys(Collection<String> additionallySelectedKeys,
            LinkedHashMap<String, AuxiliaryTableRow<V>> auxiliaryTable) {

        additionallySelectedKeys.parallelStream().map(auxiliaryTable::get)
                .forEach(AuxiliaryTableRow::addOneAdditionalField);
    }

    /**
     * Appends the rows to the auxiliary table. Since the order of the rows is
     * relevant, you cannot parallelize here.
     *
     * @param <V>
     *            the type of meta-data values
     * @param rows
     *            rows to append
     * @param auxiliaryTable
     *            auxiliary table to append to
     */
    private static final <V> void appendRowsToAuxiliaryTable(Collection<AuxiliaryTableRow<V>> rows,
            LinkedHashMap<String, AuxiliaryTableRow<V>> auxiliaryTable) {

        for (AuxiliaryTableRow<V> auxiliaryTableRow : rows) {
            auxiliaryTable.put(auxiliaryTableRow.getId(), auxiliaryTableRow);
        }
    }

    /**
     * Sorts the table by copying it into a TreeMap with the translated label as
     * the key. The label will also be appended with the id if there are ever
     * two meta-data keys with the same label in the rule set. Since TreeMap is
     * not thread safe, it cannot be parallelized here.
     *
     * @param <V>
     *            the type of meta-data values
     * @param auxiliaryTableToBeSorted
     *            auxiliary table to be sorted
     * @param priorityList
     *            the wish list of the user’s preferred human language
     * @return sorted list of fields
     */
    private static final <V> Collection<AuxiliaryTableRow<V>> sort(
            HashMap<String, AuxiliaryTableRow<V>> auxiliaryTableToBeSorted, List<LanguageRange> priorityList) {

        TreeMap<String, AuxiliaryTableRow<V>> sorted = new TreeMap<>();
        for (AuxiliaryTableRow<V> auxiliaryTableRow : auxiliaryTableToBeSorted.values()) {
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
     * always a grouped meta-data.
     *
     * @param ruleset
     *            the ruleset
     * @param universalKey
     *            the universal key
     * @param universalRule
     *            the universal rule
     * @param settings
     *            the settings
     * @param priorityList
     *            the user’s wish list for the best possible translation
     */
    private NestedKeyView(Ruleset ruleset, U universalKey, UniversalRule universalRule,
            Settings settings, List<LanguageRange> priorityList) {

        super(universalKey, universalRule, priorityList);
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
     * @param universalDivision
     *            the universal division
     * @param universalRule
     *            the universal rule
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
    protected NestedKeyView(Ruleset ruleset, U universalDivision, UniversalRule universalRule,
            Settings settings, List<LanguageRange> priorityList, boolean division) {
        super(universalDivision, universalRule, priorityList);

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
    private final <V> void addAnyUniversalRules(LinkedHashMap<String, AuxiliaryTableRow<V>> auxiliaryTable) {

        auxiliaryTable.entrySet().parallelStream().forEach(entry -> entry.getValue()
                .setUniversalPermitRule(universalRule.getUniversalPermitRuleForKey(entry.getKey(), division)));
    }

    /**
     * Adds the keys selected by the user to be added to the list of keys to
     * add. This procedure is a part of the function to
     * {@link #cerateAuxiliaryTableWithKeysToBeSorted(LinkedHashMap, UniversalRule, Collection, Collection)}.
     * 
     * @param auxiliaryTable
     *            the target table. If the key is already here, it will not be
     *            added a second time.
     * @param universalRule
     *            the rule provides the key
     * @param additionalKeys
     *            list of the key marked as being added by the user
     * @param toBeSorted
     *            write access to the list of keys to be sorted
     */
    private final <V> void addAdditionalKeys(LinkedHashMap<String, AuxiliaryTableRow<V>> auxiliaryTable,
            UniversalRule universalRule, Collection<String> additionalKeys,
            HashMap<String, AuxiliaryTableRow<V>> toBeSorted) {
        for (String additionalKey : additionalKeys) {
            if (!auxiliaryTable.containsKey(additionalKey) && !toBeSorted.containsKey(additionalKey)) {
                Optional<Key> optionalKey = universalRule.isUnspecifiedUnrestricted() ? Optional.empty()
                        : ruleset.getKey(additionalKey);
                UniversalKey universalKey = optionalKey.isPresent() ? new UniversalKey(ruleset, optionalKey.get())
                        : new UniversalKey(ruleset, additionalKey);
                toBeSorted.put(additionalKey, new AuxiliaryTableRow<>(universalKey, settings));
            }
        }
    }

    /**
     * If the rule is unspecified unrestricted, the remaining keys are added to
     * the keys to be sorted. This procedure is a part of the function to
     * {@link #cerateAuxiliaryTableWithKeysToBeSorted(LinkedHashMap, UniversalRule, Collection, Collection)}.
     * 
     * @param auxiliaryTable
     *            the target table. If the key is already here, it will not be
     *            added a second time.
     * @param universalKeys
     *            the keys to be checked
     * @param toBeSorted
     *            write access to the list of keys to be sorted
     */
    private final <V> void addRemainingKeys(LinkedHashMap<String, AuxiliaryTableRow<V>> auxiliaryTable,
            Collection<UniversalKey> universalKeys, HashMap<String, AuxiliaryTableRow<V>> toBeSorted) {
        for (UniversalKey universalKey : universalKeys) {
            if (!auxiliaryTable.containsKey(universalKey.getId())) {
                toBeSorted.put(universalKey.getId(), new AuxiliaryTableRow<>(universalKey, settings));
            }
        }
    }

    /**
     * Generates an auxiliary table for additional keys. These will later be
     * sorted alphabetically and the key will be added below the given order.
     * This table contains values if there is no restriction rule or if the
     * restriction rule specifies unspecified as unrestricted. Otherwise the
     * table is empty for now. However, it can still receive elements in the
     * further course, namely when meta-data entries name a key that is not
     * specified in the rule set. Then in this table namely for undefined key
     * views are created.
     *
     * @param auxiliaryTable
     *            table with already pre-sorted keys. Keys that are in this
     *            table are not included in the table of keys yet to be sorted.
     * @param universalRule
     *            optionally a universal rule
     * @param universalKeys
     *            all universal keys
     * @param additionalKeys
     *            which keys the user has additionally selected
     * @return an auxiliary table for additional keys yet to be sorted
     */
    private final <V> HashMap<String, AuxiliaryTableRow<V>> cerateAuxiliaryTableWithKeysToBeSorted(
            LinkedHashMap<String, AuxiliaryTableRow<V>> auxiliaryTable, UniversalRule universalRule,
            Collection<UniversalKey> universalKeys, Collection<String> additionalKeys) {

        HashMap<String, AuxiliaryTableRow<V>> toBeSorted = new HashMap<>();

        if (universalRule.isUnspecifiedUnrestricted()) {
            addRemainingKeys(auxiliaryTable, universalKeys, toBeSorted);
        }
        addAdditionalKeys(auxiliaryTable, universalRule, additionalKeys, toBeSorted);
        return toBeSorted;
    }

    /**
     * This is actually a major part of the functionality of this class that
     * generates the helper table. You can see that here the individual
     * functions are called from the class.
     *
     * @param currentEntries
     *            which meta-data are present in the view
     * @param additionalKeys
     *            which keys the user has additionally selected
     * @return A helper table for all meta-data keys
     */
    private <V> Collection<AuxiliaryTableRow<V>> createAuxiliaryTable(Map<V, String> currentEntries,
            Collection<String> additionalKeys) {

        LinkedHashMap<String, AuxiliaryTableRow<V>> auxiliaryTable = createAuxiliaryTableWithPreSortedKeys(
            universalRule.getExplicitlyPermittedUniversalKeys(universal));
        HashMap<String, AuxiliaryTableRow<V>> auxiliaryTableToBeSorted = cerateAuxiliaryTableWithKeysToBeSorted(
            auxiliaryTable, universalRule, universal.getUniversalKeys(), additionalKeys);
        storeValues(currentEntries, auxiliaryTable, auxiliaryTableToBeSorted);
        appendRowsToAuxiliaryTable(sort(auxiliaryTableToBeSorted, priorityList), auxiliaryTable);
        addAnyUniversalRules(auxiliaryTable);
        addFieldsForAdditionallySelectedKeys(additionalKeys, auxiliaryTable);
        return auxiliaryTable.values();
    }

    /**
     * Generates the auxiliary table with the keys already specified by the
     * restriction rule in their order. Since the order of the keys is relevant,
     * you can not parallelize them here.
     *
     * @param explicitlyPermittedKeys
     *            keys given in their order by the restriction rule
     * @return the auxiliary table
     */
    private final <V> LinkedHashMap<String, AuxiliaryTableRow<V>> createAuxiliaryTableWithPreSortedKeys(
            List<UniversalKey> explicitlyPermittedKeys) {

        LinkedHashMap<String, AuxiliaryTableRow<V>> auxiliaryTable = new LinkedHashMap<>();
        for (UniversalKey universalKey : explicitlyPermittedKeys) {
            auxiliaryTable.put(universalKey.getId(), new AuxiliaryTableRow<V>(universalKey, settings));
        }
        return auxiliaryTable;
    }

    /**
     * Here the addable meta-data keys are fetched. To do this, the table must
     * first be made and then an output made.
     *
     * @param <V>
     *            the type of meta-data objects
     * @param currentEntries
     *            meta-data objects that have already been entered, along with
     *            their key
     * @param additionalKeys
     *            meta-data keys that the user has already selected
     */
    @Override
    public <V> Collection<MetadataViewInterface> getAddableMetadata(Map<V, String> currentEntries,
            Collection<String> additionalKeys) {

        Collection<MetadataViewInterface> result = new LinkedList<>();
        for (AuxiliaryTableRow<V> auxiliaryTableRow : createAuxiliaryTable(currentEntries, additionalKeys)) {
            if (auxiliaryTableRow.isPossibleToExpandAnotherField()) {
                MetadataViewInterface keyView = auxiliaryTableRow
                        .isRequiringAComplexUniversalKey()
                                ? getNestedKeyView(auxiliaryTableRow.getId())
                                : new KeyView(auxiliaryTableRow.getUniversalKey(),
                                        universalRule.getUniversalPermitRuleForKey(auxiliaryTableRow.getId(), division),
                                        settings, priorityList);
                result.add(keyView);
            }
        }
        return result;
    }

    /**
     * Creates a key view in a nest. This is the case when grouped keys, in the
     * rule set XML file when {@code <key>} element occurs within {@code <key>}
     * element.
     *
     * @param keyId
     *            identifier for key in the nest
     * @return a view on the child of the group
     */
    private NestedKeyView<UniversalKey> getNestedKeyView(String keyId) {
        UniversalRule universalRuleForKey = universalRule.getUniversalPermitRuleForKey(keyId, division);
        if (division) {
            universalRuleForKey.merge(ruleset.getUniversalRestrictionRuleForKey(keyId));
        }
        return new NestedKeyView<UniversalKey>(ruleset, universal.getUniversalKey(keyId), universalRuleForKey,
                settings.getSettingsForKey(keyId), priorityList);
    }

    /**
     * Sorts and visibly outputs the meta-data. That’s the main function of the
     * mask. The structure of meta-data mask is like this: If there is rule,
     * then the order of elements is by rule. But if the rule allows more
     * elements or if there are data elements that are not in the rule, then
     * they come in alphabetical order below them. The meta-data elements must
     * be sorted and therefore sometimes more fields are needed. Exceptions here
     * are multiple selections only once, and with multiple values.
     *
     * @param <V>
     *            the type of meta-data objects
     * @param currentEntries
     *            meta-data objects that have already been entered, along witht
     *            heir key
     * @param additionalKeys
     *            meta-data keys that the user has already selected
     * @return mask
     */
    @Override
    public <V> List<MetadataViewWithValuesInterface<V>> getSortedVisibleMetadata(Map<V, String> currentEntries,
            Collection<String> additionalKeys) {

        LinkedList<MetadataViewWithValuesInterface<V>> result = new LinkedList<>();
        Collection<V> excludedDataObjects = new HashSet<>();
        for (AuxiliaryTableRow<V> auxiliaryTableRow : createAuxiliaryTable(currentEntries, additionalKeys)) {
            if (auxiliaryTableRow.isContainingExcludedData()) {
                excludedDataObjects.addAll(auxiliaryTableRow.getDataObjects(0));
            } else {
                for (int i = 0; i < auxiliaryTableRow.getNumberOfTypeViewsToGenerate(); i++) {
                    MetadataViewInterface typeView = auxiliaryTableRow
                            .isRequiringAComplexUniversalKey()
                                    ? getNestedKeyView(auxiliaryTableRow.getId())
                                    : new KeyView(auxiliaryTableRow.getUniversalKey(),
                                            universalRule.getUniversalPermitRuleForKey(auxiliaryTableRow.getId(), division),
                                            settings, priorityList);
                    Optional<MetadataViewInterface> definedTypeView = Optional.of(typeView);
                    result.add(new FormRow<V>(definedTypeView, auxiliaryTableRow.getDataObjects(i)));
                }
            }
        }
        if (!excludedDataObjects.isEmpty()) {
            result.addFirst(new FormRow<V>(Optional.empty(), excludedDataObjects));
        }
        return result;
    }

    /**
     * Adds the already entered meta-data to the helper tables. For meta-data
     * that does not belong to any field, an undefined field is created. Since
     * LinkedHashMap is not thread safe, it cannot be parallelized here.
     *
     * @param enteredMetaData
     *            already entered meta-data to be added to the auxiliary tables
     * @param sortedAuxiliaryTable
     *            help table with rows with default sorting
     * @param auxiliaryTableToBeSorted
     *            help table with rows that still have to be sorted
     */
    private final <V> void storeValues(Map<V, String> enteredMetaData,
            LinkedHashMap<String, AuxiliaryTableRow<V>> sortedAuxiliaryTable,
            HashMap<String, AuxiliaryTableRow<V>> auxiliaryTableToBeSorted) {

        for (Entry<V, String> entry : enteredMetaData.entrySet()) {
            String keyId = entry.getValue();
            if (sortedAuxiliaryTable.containsKey(keyId)) {
                sortedAuxiliaryTable.get(keyId).add(entry.getKey());
            } else {
                auxiliaryTableToBeSorted.computeIfAbsent(keyId,
                    missing -> new AuxiliaryTableRow<V>(new UniversalKey(ruleset, keyId), settings));
                auxiliaryTableToBeSorted.get(keyId).add(entry.getKey());
            }
        }
    }

    @Override
    public Optional<Domain> getDomain() {
        return universal.getDomain();
    }
}
