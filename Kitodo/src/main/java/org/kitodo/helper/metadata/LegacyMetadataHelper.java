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

package org.kitodo.helper.metadata;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.ugh.DocStructInterface;
import org.kitodo.api.ugh.MetadataInterface;
import org.kitodo.api.ugh.MetadataTypeInterface;

public class LegacyMetadataHelper implements MetadataInterface {
    private static final Logger logger = LogManager.getLogger(LegacyMetadataHelper.class);
    private LegacyInnerPhysicalDocStructHelper legacyInnerPhysicalDocStructHelper;
    private String value;
    private MetadataTypeInterface type;

    // public MetadataJoint() {
    // type = new MetadataTypeJoint();
    // value = "";
    // }

    LegacyMetadataHelper(LegacyInnerPhysicalDocStructHelper legacyInnerPhysicalDocStructHelper, MetadataTypeInterface type, String value) {
        this.type = type;
        this.value = value;
        this.legacyInnerPhysicalDocStructHelper = legacyInnerPhysicalDocStructHelper;
    }

    public LegacyMetadataHelper(MetadataTypeInterface type) {
        this.type = type;
        this.value = "";
    }

    @Override
    public LegacyInnerPhysicalDocStructHelper getDocStruct() {
        return legacyInnerPhysicalDocStructHelper;
    }

    @Override
    public MetadataTypeInterface getMetadataType() {
        return type;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setDocStruct(DocStructInterface docStruct) {
        this.legacyInnerPhysicalDocStructHelper = (LegacyInnerPhysicalDocStructHelper) docStruct;
    }

    @Override
    public void setType(MetadataTypeInterface metadataType) {
        logger.log(Level.TRACE, "setType(metadataType: {})", metadataType);
        // TODO Auto-generated method stub
    }

    @Override
    public void setStringValue(String value) {
        this.value = value;
    }
}
