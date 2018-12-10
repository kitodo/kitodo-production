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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale.LanguageRange;
import java.util.Set;

/**
 * Class of the data row objects of the auxiliary table. You can imagine the
 * list of these objects in the meta-data acquisition mask builder as a large
 * table in which all relevant information is entered, one row per meta-data
 * type, in the order in which the meta-data types must be displayed later.
 *
 * @param <T>
 *            the type of meta-data objects
 */
class AuxiliaryTableRow<T> {
    /**
     * Returns the umpteenth set member as the only element of a list, if that
     * exists. Otherwise the empty list.
     *
     * @param <T>
     *            the type of the meta-data objects
     * @param unnumbered
     *            unnumbered collection
     * @param fictitiousNumber
     *            fictitious number of the element
     * @return element with the fictional number
     */
    private static <T> Collection<T> unorderedCollectionMemberByIndexAsList(Collection<T> unnumbered,
            int fictitiousNumber) {

        List<T> result = new LinkedList<>();
        Iterator<T> currentMetaDataObject = unnumbered.iterator();
        for (int i = 0; i < fictitiousNumber; i++) {
            if (currentMetaDataObject.hasNext()) {
                currentMetaDataObject.next();
            }
        }
        if (currentMetaDataObject.hasNext()) {
            result.add(currentMetaDataObject.next());
        }
        return result;
    }

    /**
     * The universal key provides information about the key.
     */
    private UniversalKey universalKey;

    /**
     * The rule plays a role in terms of repeatability (e.g., single versus
     * multiple choice) and possible restrictions on list of values. If there is
     * one.
     */
    private UniversalRule universalPermitRule;

    /**
     * If there are edit settings for this meta-data type.
     */
    private Settings settings;

    /**
     * The field is ticked if for this type a (possibly additional) white field
     * is to be created.
     */
    private boolean oneAdditionalField = false;

    /**
     * The meta-data values for this type. Although the table is agnostic about
     * which class these objects have, it still accepts them and provides for
     * possibly necessary grouping.
     */
    private Set<T> metaDataObjects = new HashSet<>();

    /**
     * Creates a new data row object and enters a universal key into the table.
     *
     * @param universalKey
     *            universal key for this row
     */
    AuxiliaryTableRow(UniversalKey universalKey, Settings settings) {
        this.universalKey = universalKey;
        this.settings = settings;
    }

    /**
     * Adds a meta-data value object to this meta-data type.
     *
     * @param metaDataObject
     *            valuable object added
     */
    void add(T metaDataObject) {
        metaDataObjects.add(metaDataObject);

    }

    /**
     * Adds an additional (empty) input field to this metadata type.
     */
    void addOneAdditionalField() {
        oneAdditionalField = true;

    }

    /**
     * Returns the data object(s). If the meta-data type is a multiple-choice,
     * all objects in the first (and only) element are returned, otherwise one
     * will be used, which in turn must be included in a list as a container
     * because of the method signature.
     *
     * @param i
     *            which object should be returned
     * @return the data object(s)
     */
    Collection<T> getDataObjects(int i) {
        if (isMultipleChoice() || isContainingExcludedData()) {
            return metaDataObjects;
        } else {
            return unorderedCollectionMemberByIndexAsList(metaDataObjects, i);
        }

    }

    /**
     * Returns the identification of the key. The key ID is of fundamental
     * importance because it links everything together.
     *
     * @return the ID
     */
    String getId() {
        return universalKey.getId();
    }

    /**
     * Returns the universal key for the key handled in this row.
     *
     * @return the universal key
     */
    UniversalKey getUniversalKey() {
        return universalKey;
    }

    /**
     * Returns a label in the named priority list.
     *
     * @param priorityList
     *            favorites list of human languages
     * @return the best fitting language label
     */
    String getLabel(List<LanguageRange> priorityList) {
        return universalKey.getLabel(priorityList);
    }

    /**
     * Returns how many type views need to be generated in the result list.
     *
     * @return how many type views need to be generated in the result list
     */
    int getNumberOfTypeViewsToGenerate() {
        if (settings.isExcluded(universalKey.getId())) {
            return 0;
        }
        int objects = metaDataObjects.size();
        int fields = Math.max(
            Math.max(settings.isAlwaysShowing(universalKey.getId()) ? 1 : 0, universalPermitRule.getMinOccurs()),
            objects + (oneAdditionalField && universalPermitRule.getMaxOccurs() > objects ? 1 : 0));
        return isMultipleChoice() ? Math.min(fields, 1) : fields;
    }

    /**
     * Returns whether there are data values for a hidden meta-data entry. These
     * must persist when the view is saved and must not be deleted simply
     * because they no longer appear in the display.
     *
     * @return whether there are hidden values
     */
    boolean isContainingExcludedData() {
        return settings.isExcluded(universalKey.getId()) && !metaDataObjects.isEmpty();
    }

    /**
     * Returns whether the meta-data type is a multiple choice.
     *
     * @return whether the meta-data type is a multiple choice
     */
    private boolean isMultipleChoice() {
        return universalKey.isHavingOptions() && universalPermitRule.isRepeatable();
    }

    /**
     * Returns whether you can add another field to this meta-data type. For
     * excluded keys you cannot add a field, and for multiple selection keys
     * only if there is no meta-data entry for this key. Otherwise it must be
     * looked out if one more is possible after the rule.
     *
     * @return whether you can add another field
     */
    boolean isPossibleToExpandAnotherField() {
        int previous = oneAdditionalField ? metaDataObjects.size() + 1 : metaDataObjects.size();
        if (settings.isExcluded(universalKey.getId()) || universalPermitRule.getMinOccurs() > previous
                || isMultipleChoice() && !metaDataObjects.isEmpty()) {
            return false;
        }
        return universalPermitRule.getMaxOccurs() > previous;
    }

    /**
     * Returns whether a complex or simple universal key is to be generated
     * for this universal key.
     *
     * @return whether a complex universal key is to be generated
     */
    boolean isRequiringAComplexUniversalKey() {
        return universalKey.isComplex();
    }

    /**
     * Set method for setting the universal permit rule if there is one.
     *
     * @param universalPermitRule
     *            universal permit rule to be set
     */
    void setUniversalPermitRule(UniversalRule universalPermitRule) {
        this.universalPermitRule = universalPermitRule;
    }
}
