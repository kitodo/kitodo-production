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

import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;

import org.kitodo.dataformat.metskitodo.KitodoType;
import org.kitodo.dataformat.metskitodo.MdSecType;
import org.kitodo.dataformat.metskitodo.Mets;
import org.kitodo.dataformat.metskitodo.ObjectFactory;
import org.kitodo.dataformat.metskitodo.StructLinkType;

/**
 * This is a wrapper class for holding and manipulating the content of a
 * serialized mets-kitodo format xml file.
 */
public class MetsKitodoWrap {
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
     * Constructor which creates Mets object with corresponding object factory.
     */
    public MetsKitodoWrap() {
        this.mets = createBasicMetsElements(objectFactory.createMets());
    }

    private Mets createBasicMetsElements(Mets mets) {
        if (Objects.isNull(mets.getFileSec())) {
            mets.setFileSec(objectFactory.createMetsTypeFileSec());
        }
        if (Objects.isNull(mets.getStructLink())) {
            mets.setStructLink(objectFactory.createMetsTypeStructLink());
        }
        if (Objects.isNull(mets.getMetsHdr())) {
            mets.setMetsHdr(objectFactory.createMetsTypeMetsHdr());
        }
        return mets;
    }

    /**
     * Constructor which creates Mets object by unmarshalling given xml file of
     * mets-kitodo format.
     * 
     * @param xmlFile
     *            The xml file in mets-kitodo format as URI.
     * @throws JAXBException
     *             Thrown if an error was encountered while creating the
     *             <tt>Unmarshaller</tt> object.
     * @throws XMLStreamException
     *             Thrown if an error was encountered while creating the
     *             <tt>XMLStreamReader</tt> object.
     */
    public MetsKitodoWrap(URI xmlFile) throws JAXBException, XMLStreamException {
        JAXBContext jaxbMetsContext = JAXBContext.newInstance(Mets.class);
        Unmarshaller jaxbUnmarshaller = jaxbMetsContext.createUnmarshaller();

        // using a stream filter to prevent accepting white space and new line content
        // in an element that has mixed context
        XMLInputFactory xif = XMLInputFactory.newFactory();

        XMLStreamReader xsr = xif.createXMLStreamReader(new StreamSource(xmlFile.getPath()));
        xsr = xif.createFilteredReader(xsr, new StreamFilter() {

            @Override
            public boolean accept(XMLStreamReader reader) {
                if (reader.getEventType() == XMLStreamReader.CHARACTERS) {
                    return reader.getText().trim().length() > 0;
                }
                return true;
            }
        });
        this.mets = createBasicMetsElements((Mets) jaxbUnmarshaller.unmarshal(xsr));
        xsr.close();
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
        if (this.getDmdSecs().size() > index) {
            // Wrapping null-checks at getter-chain into Optional<T>.class
            Optional<List<Object>> xmlData = Optional.ofNullable(getDmdSecs().get(index)).map(MdSecType::getMdWrap)
                    .map(MdSecType.MdWrap::getXmlData).map(MdSecType.MdWrap.XmlData::getAny);

            if (xmlData.isPresent()) {
                try {
                    return MetsKitodoUtils.getFirstGenericTypeFromObjectList(xmlData.get(), KitodoType.class);
                } catch (NoSuchElementException e) {
                    throw new NoSuchElementException("MdSec element with index: " + index + " does not have kitodo metadata");
                }
            }
            throw new NoSuchElementException("MdSec element with index: " + index + " does not have data");
        }
        throw new NoSuchElementException("MdSec element with index: " + index + " does not exist");
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
