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

package org.kitodo.production.helper;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataGroup;
import org.kitodo.api.dataeditor.rulesetmanagement.Reimport;
import org.kitodo.production.services.dataeditor.DataEditorService;

/**
 * This class is used to represent a comparison of old and new metadata instances of the same type used during
 * the re-import of catalog metadata in the metadata editor.
 */
public class MetadataComparison {

    private final String metadataKey;
    private final HashSet<Metadata> oldValues;
    private final HashSet<Metadata> newValues;
    private Reimport selection;
    private boolean isMetadataGroup;

    /**
     * Constructor.
     *
     * @param metadataKey
     *          metadata key as string
     * @param oldValues
     *          old metadata entries as set of metadata
     * @param newValues
     *          new metadata entries as set of metadata
     * @param selection
     *          default selection between old and new metadata entries
     */
    public MetadataComparison(String metadataKey, HashSet<Metadata> oldValues, HashSet<Metadata> newValues, Reimport selection) {
        this.metadataKey = metadataKey;
        this.oldValues = oldValues;
        this.newValues = newValues;
        this.selection = selection;
        if (!oldValues.isEmpty()) {
            this.isMetadataGroup = oldValues.toArray()[0] instanceof MetadataGroup;
        } else if (!newValues.isEmpty()) {
            this.isMetadataGroup = newValues.toArray()[0] instanceof MetadataGroup;
        }
    }

    /**
     * Get metadata key.
     *
     * @return metadata key
     */
    public String getMetadataKey() {
        return Helper.getTranslation(metadataKey);
    }

    /**
     * Get set of old metadata entries.
     *
     * @return set of old metadata entries
     */
    public HashSet<Metadata> getOldValues() {
        return oldValues;
    }

    /**
     * Get set of new metadata entries.
     *
     * @return set of new metadata entries
     */
    public HashSet<Metadata> getNewValues() {
        return newValues;
    }

    /**
     * Get new metadata entries as list of strings. This is used to sort metadata entries alphabetically for display.
     *
     * @return new metadata entries as list of strings.
     */
    public List<String> getNewValuesAsStrings() {
        return getValuesAsStrings(newValues);
    }

    /**
     * Get old metadata entries as list of strings. This is used to sort metadata entries alphabetically for display.
     *
     * @return old metadata entries as list of strings.
     */
    public List<String> getOldValuesAsStrings() {
        return getValuesAsStrings(oldValues);
    }

    /**
     * Get current selection between old and new entries as 'Reimport' instance.
     *
     * @return current selection between old and new metadata entries
     */
    public Reimport getSelection() {
        return selection;
    }

    /**
     * Set current selection between old and new metadata entries.
     *
     * @param selection selection between old and new metadata entries as 'Reimport' class.
     */
    public void setSelection(Reimport selection) {
        this.selection = selection;
    }

    /**
     * Return whether metadata of this comparison is of type 'MetadataGroup' or not.
     *
     * @return whether metadata of this comparison is of type 'MetadataGroup' or not
     */
    public boolean isMetadataGroup() {
        return isMetadataGroup;
    }

    /**
     * Update current selection between old and new metadata entries. Circles between available values
     * in the following order: KEEP -> ADD -> REPLACE.
     */
    public void updateComparison() {
        switch (selection) {
            case KEEP:
                selection = Reimport.ADD;
                break;
            case ADD:
                selection = Reimport.REPLACE;
                break;
            case REPLACE:
                selection = Reimport.KEEP;
                break;
            default:
                break;
        }
    }

    /**
     * Convert given set of metadata to list of strings and returns it.
     *
     * @param metadataSet set of metadata to be converted to strings
     *
     * @return list of string representing given metadata
     */
    public List<String> getValuesAsStrings(HashSet<Metadata> metadataSet) {
        return metadataSet.stream()
                .map(DataEditorService::metadataToString)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Convert given metadata to string and return it.
     *
     * @param metadata metadata to be converted to string
     *
     * @return string conversion of given metadata
     */
    public String getValueAsString(Metadata metadata) {
        return DataEditorService.metadataToString(metadata);
    }

    /**
     * Return list of given metadata groups metadata, sorted alphabetically by their string representation.
     *
     * @param metadataGroup MetadataGroup whose metadata is sorted and returned
     *
     * @return list of metadata sorted by string representation
     */
    public List<Metadata> getMetadataSorted(MetadataGroup metadataGroup) {
        return metadataGroup.getMetadata().stream().sorted(Comparator.comparing(Metadata::getKey))
                .collect(Collectors.toList());
    }

    /**
     * List of old metadata entries sorted by their string representation.
     *
     * @return  old metadata entries sorted by their string representation.
     */
    public List<Metadata> getOldValuesSorted() {
        return getValuesSorted(oldValues);
    }

    /**
     * List of new metadata entries sorted by their string representation.
     *
     * @return new metadata entries sorted by their string representation.
     */
    public List<Metadata> getNewValuesSorted() {
        return getValuesSorted(newValues);
    }

    private List<Metadata> getValuesSorted(HashSet<Metadata> metadataSet) {
        return metadataSet.stream().sorted(Comparator.comparing(DataEditorService::metadataToString))
                .collect(Collectors.toList());
    }
}
