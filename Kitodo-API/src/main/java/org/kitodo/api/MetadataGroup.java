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

import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Metadata grouped together to describe something more complex using subfields.
 */
public class MetadataGroup extends Metadata {
    /**
     * The contents of the metadata group.
     */
    private HashSet<Metadata> metadata = new HashSet<>();

    /**
     * Get the grouped metadata.
     *
     * @return The grouped metadata
     */
    public HashSet<Metadata> getMetadata() {
        return metadata;
    }

    /**
     * Set the grouped metadata.
     *
     * @param metadata
     *            the grouped metadata
     */
    public void setMetadata(HashSet<Metadata> metadata) {
        this.metadata = metadata;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (Objects.isNull(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MetadataGroup other = (MetadataGroup) obj;
        if (!Objects.equals(key, other.key)) {
            return false;
        }
        if ((Objects.nonNull(domain) ? domain : MdSec.DMD_SEC) != (Objects.nonNull(other.domain) ? other.domain
                : MdSec.DMD_SEC)) {
            return false;
        }
        return Objects.equals(metadata, other.metadata);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (Objects.nonNull(domain) ? domain : MdSec.DMD_SEC).hashCode();
        result = prime * result + ((metadata == null) ? 0 : metadata.hashCode());
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return (Objects.nonNull(domain) ? "(" + domain + ") " : "") + key + ": {"
                + metadata.stream().map(String::valueOf).collect(Collectors.joining(", ")) + '}';
    }
}
