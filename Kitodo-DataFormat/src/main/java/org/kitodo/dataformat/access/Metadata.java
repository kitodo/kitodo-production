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

package org.kitodo.dataformat.access;

import org.kitodo.api.dataformat.mets.MdSec;
import org.kitodo.api.dataformat.mets.MetadataAccessInterface;

/**
 * Contains common properties of {@code MetadataEntry} and
 * {@code MetadataEntriesGroup} elements.
 */
abstract class Metadata implements MetadataAccessInterface {
    /**
     * In which conceptual area in the METS file this meta-data entry is stored.
     */
    protected MdSec domain;

    /**
     * The type of the meta-data entry. The type is used to describe the
     * meta-data entry, i.e. whether the value of the entry is about the title,
     * the author or a summary of an intellectual work.
     */
    protected String type;

    @Override
    public MdSec getDomain() {
        return domain;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setDomain(MdSec domain) {
        this.domain = domain;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }
}
