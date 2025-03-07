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

import java.util.Objects;

public class Metadata {
    /**
     * In which conceptual area in the METS file this metadata entry is stored.
     */
    protected MdSec domain;

    /**
     * The key of the metadata.
     */
    protected String key;

    /**
     * Returns the domain of the metadata.
     *
     * @return the location of the metadata entry
     */
    public MdSec getDomain() {
        return domain;
    }

    /**
     * Get the key o the metadata.
     *
     * @return The key of the metadata.
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the domain of the metadata.
     *
     * @param domain
     *            location to set for the metadata entry
     */
    public void setDomain(MdSec domain) {
        this.domain = domain;
    }

    /**
     * Set the key of the metadata.
     *
     * @param key
     *            The key value of the metadata.
     */
    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Metadata metadata = (Metadata) o;
        return domain == metadata.domain
                && Objects.equals(key, metadata.key);
    }

    /**
     * hashCode method of current class.
     *
     * @see java.lang.Object#hashCode()
     * @return int
     */
    @Override
    public int hashCode() {
        return Objects.hash(domain, key);
    }
}
