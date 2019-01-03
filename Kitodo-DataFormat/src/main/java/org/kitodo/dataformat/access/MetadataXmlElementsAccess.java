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
 * Abstract superclass of {@code MetadataEntry} and
 * {@code MetadataGroupXmlElementAccess}, to be able to store them together in a
 * list.
 */
abstract class MetadataXmlElementsAccess implements MetadataAccessInterface {

    @Override
    public MdSec getDomain() {
        throw new UnsupportedOperationException("discontinued interface method pending removal");
    }

    @Override
    public String getType() {
        throw new UnsupportedOperationException("discontinued interface method pending removal");
    }

    @Override
    public void setDomain(MdSec domain) {
        throw new UnsupportedOperationException("discontinued interface method pending removal");
    }

    @Override
    public void setType(String type) {
        throw new UnsupportedOperationException("discontinued interface method pending removal");
    }
}
