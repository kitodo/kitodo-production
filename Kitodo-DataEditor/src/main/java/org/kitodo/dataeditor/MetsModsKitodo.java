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

import java.io.File;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;

import metsModsKitodo.ExtensionDefinition;
import metsModsKitodo.KitodoType;
import metsModsKitodo.MdSecType;
import metsModsKitodo.Mets;
import metsModsKitodo.MetsType;
import metsModsKitodo.ModsDefinition;
import metsModsKitodo.ObjectFactory;
import metsModsKitodo.StructLinkType;

public class MetsModsKitodo {
    private Mets mets;
    private ObjectFactory objectFactory = new ObjectFactory();

    /**
     * Gets mets.
     *
     * @return The mets.
     */
    public Mets getMets() {
        return mets;
    }

    /**
     * Sets mets.
     *
     * @param mets
     *            The mets.
     */
    public void setMets(Mets mets) {
        this.mets = mets;
    }

    /**
     * Constructor which creates Mets object with corresponding object factory.
     */
    public MetsModsKitodo() {
        this.mets = objectFactory.createMets();
    }

    /**
     * Constructor which creates Mets object by unmarshalling given xml file of
     * mets-mods-kitodo format.
     * 
     * @param xmlFile
     *            The xml file in mets-mods-kitodo format.
     */
    public MetsModsKitodo(File xmlFile) throws JAXBException, XMLStreamException {
        JAXBContext jaxbMetsContext = JAXBContext.newInstance(Mets.class);
        Unmarshaller jaxbUnmarshaller = jaxbMetsContext.createUnmarshaller();

        // using a stream filter to prevent accepting white space and new line content
        // in an element that has mixed context
        XMLInputFactory xif = XMLInputFactory.newFactory();
        XMLStreamReader xsr = xif.createXMLStreamReader(new StreamSource(xmlFile.getAbsolutePath()));
        xsr = xif.createFilteredReader(xsr, new StreamFilter() {

            @Override
            public boolean accept(XMLStreamReader reader) {
                if (reader.getEventType() == XMLStreamReader.CHARACTERS) {
                    return reader.getText().trim().length() > 0;
                }
                return true;
            }
        });

        this.mets = (Mets) jaxbUnmarshaller.unmarshal(xsr);
    }

    /**
     * Adds a smLink to the structLink section of mets file.
     * 
     * @param from
     *            The from value.
     * @param to
     *            The to value.
     */
    public void addSmLink(String from, String to) {
        if (Objects.isNull(this.mets.getStructLink())) {
            MetsType.StructLink structLinkType = objectFactory.createMetsTypeStructLink();
            this.mets.setStructLink(structLinkType);
        }
        StructLinkType.SmLink structLinkTypeSmLink = objectFactory.createStructLinkTypeSmLink();
        structLinkTypeSmLink.setFrom(from);
        structLinkTypeSmLink.setTo(to);
        mets.getStructLink().getSmLinkOrSmLinkGrp().add(structLinkTypeSmLink);
    }

    /**
     * Gets all dmdSec elements.
     * 
     * @return All dmdSec elements as list of MdSecType objects.
     */
    public List<MdSecType> getDmdSecs() {
        return this.mets.getDmdSec();
    }

    /**
     * Gets KitodoType object of specified MdSec index.
     * 
     * @param index
     *            The index as int.
     * @return The KitodoType object.
     */
    public KitodoType getKitodoTypeByMdSecIndex(int index) {
        JAXBElement element = (JAXBElement) this.mets.getDmdSec().get(index).getMdWrap().getXmlData().getAny().get(0);
        ModsDefinition modsType = (ModsDefinition) element.getValue();
        ExtensionDefinition extensionType = (ExtensionDefinition) modsType.getModsGroup().get(0);
        element = (JAXBElement) extensionType.getContent().get(0);
        return (KitodoType) element.getValue();
    }

    /**
     * Gets KitodoType object of specified MdSec id.
     *
     * @param id
     *            The id as String.
     * @return The KitodoType object.
     */
    public KitodoType getKitodoTypeByMdSecId(String id) {
        int index = 0;
        for (MdSecType mdSecType : getDmdSecs()) {
            if (mdSecType.getID().equals(id)) {
                return getKitodoTypeByMdSecIndex(index);
            }
            index++;
        }
        throw new NoSuchElementException("MdSec element with id: " + id + " was not found");
    }
}
