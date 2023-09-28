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
import java.util.Objects;
import java.util.function.Supplier;

import org.apache.commons.lang3.ArrayUtils;
import org.kitodo.api.Metadata;
import org.kitodo.dataeditor.ruleset.xml.Reimport;

/**
 * Determines the result of re-importing metadata for a single metadata key.
 */
class ReimportMetadata implements Supplier<Collection<Metadata>> {

    // Metadata key for which the result of the reimport is determined.
    private final String key;

    // existing metadata for this key before reimport
    private final Collection<Metadata> currentEntries;

    // configured reimport behavior
    private Reimport reimport;

    // metadata fetched on reimport
    private final Collection<Metadata> updateEntries;

    // the maximum amount of metadata applicable here for this key
    private int maxOccurs;

    /**
     * <b>Constructor.</b><!-- --> Generates a data set for the re-import of
     * metadata for a specific key.
     * 
     * @param key
     *            metadata key for which the result of the reimport is
     *            determined
     */
    ReimportMetadata(String key) {
        this.currentEntries = new ArrayList<>();
        this.updateEntries = new ArrayList<>();
        this.key = key;
        this.maxOccurs = Integer.MAX_VALUE;
    }

    /**
     * Sets the repeated import behavior model for this metadata key. Must be a
     * value from {@link Reimport}.
     * 
     * @param reimport
     *            configured reimport behavior to set
     */
    void setReimport(Reimport reimport) {
        this.reimport = reimport;
    }

    /**
     * Sets the maximum number of metadata values allowed for this key. The
     * setting only affects newly added metadata.
     * 
     * @param maxOccurs
     *            maximum amount of metadata to set
     */
    void setMaxOccurs(int maxOccurs) {
        this.maxOccurs = maxOccurs;
    }

    /**
     * Returns the metadata key. The key is that of the contained metadata.
     *
     * @return the metadata key
     */
    String getKey() {
        return key;
    }

    /**
     * Adds a metadata entry to the current metadata entries.
     * 
     * @param metadata
     *            metadata to add
     */
    void addToCurrentEntries(Metadata metadata) {
        assert Objects.equals(metadata.getKey(), key) : "keys should match";
        currentEntries.add(metadata);
    }

    /**
     * Adds a metadata entry to the update metadata entries.
     * 
     * @param metadata
     *            metadata to add
     */
    void addToUpdateEntries(Metadata metadata) {
        assert Objects.equals(metadata.getKey(), key) : "keys should match";
        updateEntries.add(metadata);
    }

    /**
     * Merges the metadata of this key in the given behavior, respecting the
     * maximum number.
     * 
     * @return the metadata remaining after the repeated import
     */
    @Override
    public Collection<Metadata> get() {
        if (!ArrayUtils.contains(Reimport.values(), reimport)) {
            throw new IllegalStateException("Used not supported reimport case ".concat(Objects.toString(reimport)));
        }
        Collection<Metadata> result = reimport.equals(Reimport.REPLACE) && !updateEntries.isEmpty() ? new ArrayList<>()
                : new ArrayList<>(currentEntries);
        if (!reimport.equals(Reimport.KEEP) || result.isEmpty()) {
            for (Metadata metadata : updateEntries) {
                if (!result.contains(metadata) && result.size() < maxOccurs) {
                    result.add(metadata);
                }
            }
        }
        return result;
    }
}
