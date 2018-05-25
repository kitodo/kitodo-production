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

package org.kitodo.dataeditor;

import java.io.IOException;

import javax.xml.datatype.DatatypeConfigurationException;

import org.kitodo.dataformat.metskitodo.KitodoType;
import org.kitodo.dataformat.metskitodo.MetsType;
import org.kitodo.dataformat.metskitodo.ObjectFactory;
import org.kitodo.dataformat.metskitodo.StructMapType;

public class MetsKitodoObjectFactory extends ObjectFactory {

    /**
     * Creates KitodoType object which version indication of used kitodo format.
     * 
     * @return The KitodoType object.
     */
    public KitodoType createKitodoType() {
        KitodoType kitodoType = new KitodoType();
        // TODO this version value should come from data format module. Think about how
        // implement this.
        kitodoType.setVersion("1.0");
        return kitodoType;
    }

    /**
     * Creates a kitodo data editor specific MetsHdr.Agent object.
     * 
     * @return The MetsHdr.Agent object.
     */
    public MetsType.MetsHdr.Agent createKitodoMetsAgent() throws IOException {
        MetsType.MetsHdr.Agent metsAgent = super.createMetsTypeMetsHdrAgent();
        metsAgent.setOTHERTYPE("SOFTWARE");
        metsAgent.setROLE("CREATOR");
        metsAgent.setTYPE("OTHER");
        metsAgent.setName(VersionFinder.findVersionInfo("Kitodo - Data Editor"));
        return metsAgent;
    }

    /**
     * Creates a kitodo data editor specific MetsHdr object, which sets CREATEDATE
     * and agent.
     * 
     * @return The MetsHdr object.
     */
    public MetsType.MetsHdr createKitodoMetsHeader() throws DatatypeConfigurationException, IOException {
        MetsType.MetsHdr metsTypeMetsHdr = super.createMetsTypeMetsHdr();
        metsTypeMetsHdr.setCREATEDATE(JaxbXmlUtils.getXmlTime());
        MetsType.MetsHdr.Agent metsAgent = createKitodoMetsAgent();
        metsTypeMetsHdr.getAgent().add(metsAgent);
        return metsTypeMetsHdr;
    }

    /**
     * Creates a StructMap object of type "PHYSICAL".
     * 
     * @return The StructMap object.
     */
    public StructMapType createPhysicalStructMapType() {
        StructMapType structMapType = super.createStructMapType();
        structMapType.setTYPE("PHYSICAL");
        return structMapType;
    }

    /**
     * Creates a StructMap object of type "PHYSICAL".
     *
     * @return The StructMap object.
     */
    public StructMapType createLogicalStructMapType() {
        StructMapType structMapType = super.createStructMapType();
        structMapType.setTYPE("LOGICAL");
        return structMapType;
    }

    /**
     * Creates a Mets FileGrp object where the attribute USE is set to LOCAL.
     * 
     * @return The FileGrp object.
     */
    public MetsType.FileSec.FileGrp createMetsTypeFileSecFileGrpLocal() {
        MetsType.FileSec.FileGrp metsTypeFileSecFileGrp = super.createMetsTypeFileSecFileGrp();
        metsTypeFileSecFileGrp.setUSE("LOCAL");
        return metsTypeFileSecFileGrp;
    }
}
