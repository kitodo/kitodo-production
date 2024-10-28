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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataGroup;
import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.Reimport;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.production.services.data.RulesetService;
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
    private final MetadataViewInterface metadataView;

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
    public MetadataComparison(String metadataKey, HashSet<Metadata> oldValues, HashSet<Metadata> newValues,
                              MetadataViewInterface viewInterface, Reimport selection) {
        this.metadataKey = metadataKey;
        this.oldValues = oldValues;
        this.newValues = newValues;
        this.metadataView = viewInterface;
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
     * Incorporate "minOccurs" and "maxOccurs" rules from ruleset to determine valid reimport modes.
     */
    public void updateComparison() {
        switch (selection) {
            case KEEP:
                if (canAdd()) {
                    selection = Reimport.ADD;
                }
                else if (canReplace()) {
                    selection = Reimport.REPLACE;
                }
                break;
            case ADD:
                if (canReplace()) {
                    selection = Reimport.REPLACE;
                } else if (canKeep()) {
                    selection = Reimport.KEEP;
                }
                break;
            case REPLACE:
                if (canKeep()) {
                    selection = Reimport.KEEP;
                } else if (canAdd()) {
                    selection = Reimport.ADD;
                }
                break;
            default:
                break;
        }
    }

    // "<-"
    private boolean canKeep() {
        return oldValues.size() >= metadataView.getMinOccurs()
                && oldValues.size() <= metadataView.getMaxOccurs();
    }

    // "<-->"
    private boolean canAdd() {
        return metadataView.getMinOccurs() <= oldValues.size() + newValues.size()
                && oldValues.size() + newValues.size() <= metadataView.getMaxOccurs()
                && !(oldValues.isEmpty() || newValues.isEmpty());
    }

    // "->"
    private boolean canReplace() {
        return newValues.size() >= metadataView.getMinOccurs()
                && newValues.size() <= metadataView.getMaxOccurs();
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
     * List of given metadata groups sorted by their 'groupDisplayLabel'.
     *
     * @return given metadata groups sorted by their 'groupDisplayLabel'.
     */
    public List<Metadata> getMetadataGroupsSorted(Ruleset ruleset, HashSet<Metadata> values) {
        if (isMetadataGroup) {
            try {
                return RulesetService.getGroupsSortedByGroupDisplayLabel(values, ruleset);
            } catch (IOException e) {
                Helper.setErrorMessage(e);
                return new ArrayList<>();
            }
        } else {
            return getValuesSorted(values);
        }
    }

    /**
     * Get metadataView.
     *
     * @return metadataView
     */
    public MetadataViewInterface getMetadataView() {
        return metadataView;
    }

    private List<Metadata> getValuesSorted(HashSet<Metadata> metadataSet) {
        return metadataSet.stream().sorted(Comparator.comparing(DataEditorService::metadataToString))
                .collect(Collectors.toList());
    }
}
