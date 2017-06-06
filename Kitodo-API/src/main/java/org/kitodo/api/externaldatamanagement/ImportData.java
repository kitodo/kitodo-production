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

package org.kitodo.api.externaldatamanagement;

import java.util.Map;

/**
 * The imported data, transformed to a Map.
 */
public class ImportData {

    /** The data imported from the source as a map. */
    private Map<String, String> data;

    /**
     * Gets the data.
     * 
     * @return The data.
     */
    public Map<String, String> getData() {
        return data;
    }

    /**
     * Sets the data.
     * 
     * @param data
     *            The data.
     */
    public void setData(Map<String, String> data) {
        this.data = data;
    }
}
