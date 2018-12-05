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

package org.kitodo.legacy.joining;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.ugh.DocStructInterface;
import org.kitodo.api.ugh.MetadataInterface;
import org.kitodo.api.ugh.MetadataTypeInterface;

public class MetadataJoint implements MetadataInterface {
    private static final Logger logger = LogManager.getLogger(MetadataJoint.class);
    private InnerPhysicalDocStructJoint innerPhysicalDocStructJoint;
    private String value;
    private MetadataTypeInterface type;

    // public MetadataJoint() {
    // type = new MetadataTypeJoint();
    // value = "";
    // }

    MetadataJoint(InnerPhysicalDocStructJoint innerPhysicalDocStructJoint, MetadataTypeInterface type, String value) {
        this.type = type;
        this.value = value;
        this.innerPhysicalDocStructJoint = innerPhysicalDocStructJoint;
    }

    public MetadataJoint(MetadataTypeInterface type) {
        this.type = type;
        this.value = "";
    }

    @Override
    public InnerPhysicalDocStructJoint getDocStruct() {
        return innerPhysicalDocStructJoint;
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
        this.innerPhysicalDocStructJoint = (InnerPhysicalDocStructJoint) docStruct;
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
