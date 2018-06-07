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
import java.util.List;
import java.util.Objects;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.namespace.QName;

import org.kitodo.dataformat.metskitodo.DivType;
import org.kitodo.dataformat.metskitodo.KitodoType;
import org.kitodo.dataformat.metskitodo.MdSecType;
import org.kitodo.dataformat.metskitodo.MetadataType;
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
        return createStructMapTypeOfType("PHYSICAL");
    }

    /**
     * Creates a StructMap object of type "LOGICAL".
     *
     * @return The StructMap object.
     */
    public StructMapType createLogicalStructMapType() {
        return createStructMapTypeOfType("LOGICAL");
    }

    private StructMapType createStructMapTypeOfType(String type) {
        StructMapType structMapType = super.createStructMapType();
        structMapType.setTYPE(type);
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

    /**
     * Creates a DivType object for using as root div in mets physical sruct map.
     * 
     * @return The DivType object.
     */
    public DivType createRootDivTypeForPhysicalStructMap() {
        DivType divType = super.createDivType();
        divType.setID("PHYS_0000");
        divType.setTYPE("physSequence");
        return divType;
    }

    /**
     * Creates a DivType object for using as root div in mets logical sruct map.
     *
     * @return The DivType object.
     */
    public DivType createRootDivTypeForLogicalStructMap(String type, MdSecType dmdSecOfLogicalRootDiv) {
        DivType divType = super.createDivType();
        divType.setID("LOG_0000");
        divType.setTYPE(type);
        divType.getDMDID().add(dmdSecOfLogicalRootDiv);
        return divType;
    }


    /**
     * Creates a MdSecType object which wraps a list of MetadataTypes.
     *
     * @param metadata
     *            The list MetadataType objects.
     * @param id
     *            The id of this DmdSec element.
     * @return The MdSecType object.
     */
    public MdSecType createDmdSecByMetadata(List<MetadataType> metadata, String id) {
        JAXBElement<KitodoType> kitodoType = createKitodoTypeJAXBElement();
        if (Objects.nonNull(metadata)) {
            for (MetadataType metadataType : metadata) {
                kitodoType.getValue().getMetadata().add(metadataType);
            }
        }
        MdSecType mdSec = super.createMdSecType();
        mdSec.setMdWrap(wrapKitodoTypeJAXBIntoMdWrap(kitodoType));
        mdSec.setID(id);
        return mdSec;
    }

    /**
     * Created a MdWrap object for kitodo metadata by setting MDTYPE and
     * OTHERMDTYPE.
     *
     * @return The MdWrap object.
     */
    private MdSecType.MdWrap createKitodoMdSecTypeMdWrap() {
        MdSecType.MdWrap mdWrap = super.createMdSecTypeMdWrap();
        mdWrap.setMDTYPE("OTHER");
        mdWrap.setOTHERMDTYPE("KITODO");
        return mdWrap;
    }

    private JAXBElement<KitodoType> createKitodoTypeJAXBElement() {
        KitodoType kitodoType = super.createKitodoType();
        QName qName = new QName(MetsKitodoPrefixMapper.getKitodoUri(), MetsKitodoPrefixMapper.getKitodoPrefix());
        return new JAXBElement<>(qName, KitodoType.class, kitodoType);
    }

    private MdSecType.MdWrap wrapKitodoTypeJAXBIntoMdWrap(JAXBElement<KitodoType> kitodoTypeJAXBElement) {
        MdSecType.MdWrap mdWrap = createKitodoMdSecTypeMdWrap();
        MdSecType.MdWrap.XmlData xmlData = super.createMdSecTypeMdWrapXmlData();
        xmlData.getAny().add(kitodoTypeJAXBElement);
        mdWrap.setXmlData(xmlData);
        return mdWrap;
    }
}
