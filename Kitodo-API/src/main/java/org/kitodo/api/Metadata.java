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

import org.kitodo.api.dataformat.mets.MdSec;

/**
 * Abstract superclass for meta-data entries and groups.
 *
 * <p>
 * Meta-data is stored in Production as a collection of key-value pairs. This
 * super interface grants access to the key while providing access to the value
 * through two different sub-interfaces, the use of which depends on the nature
 * of the meta-data, as the value must be accessed in different ways
 * accordingly.
 */
public abstract class Metadata {
    /**
     * In which conceptual area in the METS file this meta-data entry is stored.
     */
    private MdSec domain;

    /**
     * The key of the meta-data. The key is used to describe the meta-data, i.e.
     * whether the value of the entry is about the title, the author or a
     * summary of an intellectual work.
     */
    private String key;

    /**
     * Specifies the location of the meta-data entry in the METS file. METS
     * allows the storage of meta-data in five different areas with different
     * semantics. See {@link MdSec} for details.
     *
     * @return the location of the meta-data entry
     */
    public MdSec getDomain() {
        return domain;
    }

    /**
     * Returns the key of the meta-data entry. The key is used to describe the
     * meta-data entry, i.e. whether the value of the entry is about the title,
     * the author or a summary of an intellectual work. Even though the key is
     * stored as simple text, it is worth using a controlled vocabulary here. In
     * Production, this controlled vocabulary is provided by the ruleset.
     *
     * @return the key of the meta-data entry
     */
    public String getKey() {
        return key;
    }

    /**
     * Specifies the location of the meta-data entry in the METS file. METS
     * allows the storage of meta-data in five different areas with different
     * semantics. See {@link MdSec} for details. Note that you cannot set the
     * location for a meta-data entry that is a member of a meta-data group
     * differently from the location of the meta-data group.
     *
     * @param domain
     *            location to set for the meta-data entry
     */
    public void setDomain(MdSec domain) {
        this.domain = domain;
    }

    /**
     * Sets the key of the meta-data entry. The key is used to describe the
     * meta-data entry, i.e. whether the value of the entry is about the title,
     * the author or a summary of an intellectual work. Even if the type is
     * stored as simple text, it is worth using a controlled vocabulary here. In
     * Production, this controlled vocabulary is provided by the ruleset.
     *
     * @param key
     *            the key of the meta-data entry
     */
    public void setKey(String key) {
        this.key = key;
    }
}
