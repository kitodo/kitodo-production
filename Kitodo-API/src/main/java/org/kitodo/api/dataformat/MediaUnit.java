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

package org.kitodo.api.dataformat;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.kitodo.api.Metadata;
import org.kitodo.api.dataformat.mets.KitodoUUID;

public class MediaUnit implements Division<MediaUnit> {
    /**
     * The subordinate media units of this media unit, which form the media unit
     * tree. The order of the substructures is subordinate media units by the
     * order of the {@code <div>} elements in the
     * {@code <structMap TYPE="PHYSICAL">} in the METS file.
     */
    private List<MediaUnit> children = new LinkedList<>();

    /**
     * Each media unit can be available in different variants, for each of which
     * a media file is available. This is in this map.
     */
    private Map<MediaVariant, URI> mediaFiles = new HashMap<>();

    /**
     * The metadata for this media unit. This media unit can be described with
     * any metadata.
     */
    private Collection<Metadata> metadata = new HashSet<>();

    /**
     * Sequence number of the media unit. The playback order of the media units
     * when referenced from an included structural element is determined by this
     * attribute (not by the order of the references).
     */
    private int order;

    /**
     * A human readable label for the order of this media unit. This need not be
     * directly related to the order number. Examples of order labels could be
     * “I, II, III, IV, V,  - , 1, 2, 3”, meanwhile the order would be “1, 2, 3,
     * 4, 5, 6, 7, 8, 9”.
     */
    private String orderlabel;

    /**
     * The type of the media unit.
     */
    private String type;

    /**
     * Saves the METS identifier for the division.
     */
    private String metsDivReferrerId;

    /**
     * List of IncludedStructuralElements this view is assigned to.
     */
    private List<IncludedStructuralElement> includedStructuralElements;

    /**
     * Creates a new MediaUnit.
     */
    public MediaUnit() {
        includedStructuralElements = new LinkedList<>();
    }


    /**
     * Returns the subordinate media units associated with this media unit.
     *
     * @return the subordinate media units
     */
    @Override
    public List<MediaUnit> getChildren() {
        return children;
    }

    /**
     * Returns the map of available media variants with the corresponding media
     * file URIs.
     *
     * @return available media variants with corresponding media file URIs
     */
    public Map<MediaVariant, URI> getMediaFiles() {
        return mediaFiles;
    }

    /**
     * Returns the metadata on this structure.
     *
     * @return the metadata
     */
    public Collection<Metadata> getMetadata() {
        return metadata;
    }

    /**
     * Returns the order number for this media unit.
     *
     * @return the order number
     */
    public int getOrder() {
        return order;
    }

    /**
     * Sets the order number for this media unit.
     *
     * @param order
     *            order number to set
     */
    public void setOrder(int order) {
        this.order = order;
    }

    /**
     * Returns the order label for this media unit.
     *
     * @return the order label
     */
    public String getOrderlabel() {
        return orderlabel;
    }

    /**
     * Sets the order label for this media unit.
     *
     * @param orderlabel
     *            order label to set
     */
    public void setOrderlabel(String orderlabel) {
        this.orderlabel = orderlabel;
    }

    /**
     * Returns the type of this media unit.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of this media unit.
     *
     * @param type
     *            type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns the ID of div, or if unknown, creates a new one.
     *
     * @return the ID of div
     */
    public String getDivId() {
        if (Objects.isNull(metsDivReferrerId)) {
            metsDivReferrerId = KitodoUUID.randomUUID();
        }
        return metsDivReferrerId;
    }

    /**
     * Set the ID of div.
     *
     * @param divId
     *            ID of div to set
     */
    public void setDivId(String divId) {
        this.metsDivReferrerId = divId;
    }

    /**
     * Get includedStructuralElements.
     *
     * @return value of includedStructuralElements
     */
    public List<IncludedStructuralElement> getIncludedStructuralElements() {
        return includedStructuralElements;
    }

    @Override
    public String toString() {
        String fileName = "No file (";
        if (!mediaFiles.isEmpty()) {
            URI uri = mediaFiles.entrySet().iterator().next().getValue();
            fileName = FilenameUtils.getBaseName(uri.getPath()).concat(" (");
        }
        if (Objects.nonNull(type)) {
            fileName = type + ' ' + fileName;
        }
        return mediaFiles.keySet().stream().map(MediaVariant::getUse)
                .collect(Collectors.joining(", ", fileName, ")"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MediaUnit)) {
            return false;
        }
        MediaUnit mediaUnit = (MediaUnit) o;
        return order == mediaUnit.order
                && Objects.equals(children, mediaUnit.children)
                && Objects.equals(mediaFiles, mediaUnit.mediaFiles)
                && Objects.equals(metadata, mediaUnit.metadata)
                && Objects.equals(orderlabel, mediaUnit.orderlabel)
                && Objects.equals(type, mediaUnit.type);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int hashCode = 1;
        hashCode = prime * hashCode + ((mediaFiles == null) ? 0 : mediaFiles.hashCode());
        hashCode = prime * hashCode + order;
        hashCode = prime * hashCode + ((orderlabel == null) ? 0 : orderlabel.hashCode());
        hashCode = prime * hashCode + (Objects.isNull(type) ? 0 : type.hashCode());
        return hashCode;
    }

}
