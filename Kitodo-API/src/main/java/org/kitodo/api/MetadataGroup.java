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

/**
 * A group of meta-data entries. A group of meta-data entries is like a table
 * with different meta-data entries, which can be groups again. This allows any
 * nesting depths to be achieved.
 */
public class MetadataGroup extends Metadata {
    /**
     * The meta-data in this group. The value of the meta-data is a list of
     * meta-data objects.
     */
    private List<Metadata> group = new ArrayList<>();

    /**
     * Returns the group of this meta-data group.
     * 
     * @return the group
     */
    public List<Metadata> getGroup() {
        return group;
    }

    /**
     * Returns the group of this meta-data group.
     *
     * @param group
     *            group to set
     */
    public void setGroup(List<Metadata> group) {
        this.group = group;
    }
}
