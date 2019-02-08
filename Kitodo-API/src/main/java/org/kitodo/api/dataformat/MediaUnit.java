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
import java.util.HashMap;
import java.util.Map;

public class MediaUnit {
    /**
     * Each media unit can be available in different variants, for each of which
     * a media file is available. This is in this map.
     */
    private Map<MediaVariant, URI> mediaFiles = new HashMap<>();

    /**
     * Sequence number of the media unit. The playback order of the media units
     * when referenced from a structure is determined by this attribute (not by
     * the order of the references).
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
     * Returns the map of available media variants with the corresponding media
     * file URIs.
     * 
     * @return available media variants with corresponding media file URIs
     */
    public Map<MediaVariant, URI> getMediaFiles() {
        return mediaFiles;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mediaFiles == null) ? 0 : mediaFiles.hashCode());
        result = prime * result + order;
        result = prime * result + ((orderlabel == null) ? 0 : orderlabel.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof MediaUnit)) {
            return false;
        }
        MediaUnit other = (MediaUnit) obj;
        if (mediaFiles == null) {
            if (other.mediaFiles != null) {
                return false;
            }
        } else if (!mediaFiles.equals(other.mediaFiles)) {
            return false;
        }
        if (order != other.order) {
            return false;
        }
        if (orderlabel == null) {
            if (other.orderlabel != null) {
                return false;
            }
        } else if (!orderlabel.equals(other.orderlabel)) {
            return false;
        }
        return true;
    }
}
