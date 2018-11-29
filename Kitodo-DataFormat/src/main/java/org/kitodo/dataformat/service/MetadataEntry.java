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

package org.kitodo.dataformat.service;

import org.kitodo.api.dataformat.mets.MdSec;
import org.kitodo.api.dataformat.mets.MetadataXmlElementAccessInterface;
import org.kitodo.dataformat.metskitodo.MetadataType;

public class MetadataEntry extends Metadata implements MetadataXmlElementAccessInterface {

    private String value;

    public MetadataEntry() {
    }

    MetadataEntry(MdSec domain, MetadataType metadataType) {
        super.domain = domain;
        super.type = metadataType.getName();
        this.value = metadataType.getValue();
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    MetadataType toMetadata() {
        MetadataType metadataType = new MetadataType();
        metadataType.setName(super.type);
        metadataType.setValue(this.value);
        return metadataType;
    }
}
