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

package org.kitodo.api;

import java.util.ArrayList;
import java.util.List;

public class MetadataGroup extends Metadata {

    // The value of the metadata is a list of metadata objects.
    private List<Metadata> group = new ArrayList<>();

    /**
     * Get the grouped metadata.
     *
     * @return The grouped metadata.
     */
    public List<Metadata> getGroup() {
        return group;
    }

    /**
     * Set the grouped metadata.
     *
     * @param group
     *            the grouped metadata.
     */
    public void setGroup(List<Metadata> group) {
        this.group = group;
    }
}
