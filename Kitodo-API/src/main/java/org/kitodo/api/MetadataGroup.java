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
import java.util.Objects;
import java.util.stream.Collectors;

public class MetadataGroup extends Metadata {

    /**
     * The value of the metadata is a list of metadata objects.
     */
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MetadataGroup other = (MetadataGroup) obj;
        if (key == null) {
            if (other.key != null) {
                return false;
            }
        } else if (!key.equals(other.key)) {
            return false;
        }
        if ((Objects.nonNull(domain) ? domain : MdSec.DMD_SEC) != (Objects.nonNull(other.domain) ? other.domain
                : MdSec.DMD_SEC)) {
            return false;
        }
        if (group == null) {
            if (other.group != null) {
                return false;
            }
        } else if (!group.equals(other.group)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (Objects.nonNull(domain) ? domain : MdSec.DMD_SEC).hashCode();
        result = prime * result + ((group == null) ? 0 : group.hashCode());
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return (Objects.nonNull(domain) ? "(" + domain + ") " : "") + key + ": {"
                + group.stream().map(String::valueOf).collect(Collectors.joining(", ")) + '}';
    }
}
