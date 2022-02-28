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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale.LanguageRange;

import org.kitodo.api.Metadata;

/**
 * Class of the data row objects of the auxiliary table. You can imagine the
 * list of these objects in the metadata acquisition mask builder as a large
 * table in which all relevant information is entered, one row per metadata
 * type, in the order in which the metadata types must be displayed later.
 */
class AuxiliaryTableRow {
    /**
     * Returns the umpteenth set member as the only element of a list, if that
     * exists. Otherwise the empty list.
     *
     * @param unnumbered
     *            unnumbered collection
     * @param fictitiousNumber
     *            fictitious number of the element
     * @return element with the fictional number
     */
    private static <T> Collection<T> unorderedCollectionMemberByIndexAsList(Collection<T> unnumbered,
            int fictitiousNumber) {

        List<T> unorderedCollectionMemberAsList = new LinkedList<>();
        Iterator<T> currentMetaDataObject = unnumbered.iterator();
        for (int i = 0; i < fictitiousNumber; i++) {
            if (currentMetaDataObject.hasNext()) {
                currentMetaDataObject.next();
            }
        }
        if (currentMetaDataObject.hasNext()) {
            unorderedCollectionMemberAsList.add(currentMetaDataObject.next());
        }
        return unorderedCollectionMemberAsList;
    }

    /**
     * The key declaration provides information about the key.
     */
    private KeyDeclaration key;

    /**
     * The rule plays a role in terms of repeatability (e.g., single versus
     * multiple choice) and possible restrictions on list of values.
     */
    private Rule rule;

    /**
     * If there are edit settings for this metadata type.
     */
    private Settings settings;

    /**
     * The field is ticked if for this type a (possibly additional) white field
     * is to be created.
     */
    private boolean oneAdditionalField = false;

    /**
     * The metadata values for this type. Although the table is agnostic about
     * which class these objects have, it still accepts them and provides for
     * possibly necessary grouping.
     */
    private Collection<Metadata> metaDataObjects = new ArrayList<>();

    /**
     * Creates a new data row object and enters a key into the table.
     *
     * @param key
     *            key for this row
     */
    AuxiliaryTableRow(KeyDeclaration key, Settings settings) {
        this.key = key;
        this.settings = settings;
    }

    /**
     * Adds a metadata value object to this metadata type.
     *
     * @param metaDataObject
     *            valuable object added
     */
    void add(Metadata metaDataObject) {
        metaDataObjects.add(metaDataObject);

    }

    /**
     * Adds an additional (empty) input field to this metadata type.
     */
    void addOneAdditionalField() {
        oneAdditionalField = true;

    }

    /**
     * Returns the data object(s). If the metadata type is a multiple-choice,
     * all objects in the first (and only) element are returned, otherwise one
     * will be used, which in turn must be included in a list as a container
     * because of the method signature.
     *
     * @param i
     *            which object should be returned
     * @return the data object(s)
     */
    Collection<Metadata> getDataObjects(int i) {
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
        return key.getId();
    }

    /**
     * Returns the key handled in this row.
     *
     * @return the key
     */
    KeyDeclaration getKey() {
        return key;
    }

    /**
     * Returns a label in the named priority list.
     *
     * @param priorityList
     *            favorites list of human languages
     * @return the best fitting language label
     */
    String getLabel(List<LanguageRange> priorityList) {
        return key.getLabel(priorityList);
    }

    /**
     * Returns how many type views need to be generated in the result list.
     *
     * @return how many type views need to be generated in the result list
     */
    int getNumberOfTypeViewsToGenerate() {
        if (settings.isExcluded(key.getId())) {
            return 0;
        }
        int objects = metaDataObjects.size();
        int fields = Math.max(
            Math.max(settings.isAlwaysShowing(key.getId()) ? 1 : 0, rule.getMinOccurs()),
            objects + (oneAdditionalField && rule.getMaxOccurs() > objects ? 1 : 0));
        return isMultipleChoice() ? Math.min(fields, 1) : fields;
    }

    /**
     * Returns whether there are data values for a hidden metadata entry. These
     * must persist when the view is saved and must not be deleted simply
     * because they no longer appear in the display.
     *
     * @return whether there are hidden values
     */
    boolean isContainingExcludedData() {
        return settings.isExcluded(key.getId()) && !metaDataObjects.isEmpty();
    }

    /**
     * Returns whether the metadata type is a multiple choice.
     *
     * @return whether the metadata type is a multiple choice
     */
    private boolean isMultipleChoice() {
        return key.isWithOptions() && rule.isRepeatable();
    }

    /**
     * Returns whether you can add another field to this metadata type. For
     * excluded keys you cannot add a field, and for multiple selection keys
     * only if there is no metadata entry for this key. Otherwise it must be
     * looked out if one more is possible after the rule.
     *
     * @return whether you can add another field
     */
    boolean isPossibleToExpandAnotherField() {
        int previous = oneAdditionalField ? metaDataObjects.size() + 1 : metaDataObjects.size();
        if (settings.isExcluded(key.getId()) || rule.getMinOccurs() > previous
                || isMultipleChoice() && !metaDataObjects.isEmpty()) {
            return false;
        }
        return rule.getMaxOccurs() > previous;
    }

    /**
     * Returns whether a complex or simple key is to be generated for this key.
     *
     * @return whether a complex key is to be generated
     */
    boolean isComplexKey() {
        return key.isComplex();
    }

    /**
     * Set method for setting the rule if there is one.
     *
     * @param rule
     *            rule to be set
     */
    void setRule(Rule rule) {
        this.rule = rule;
    }
}
