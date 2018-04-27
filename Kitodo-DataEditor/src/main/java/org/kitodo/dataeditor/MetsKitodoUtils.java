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
import java.io.StringReader;
import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.dataformat.metskitodo.KitodoType;
import org.kitodo.dataformat.metskitodo.MdSecType;
import org.kitodo.dataformat.metskitodo.Mets;
import org.xml.sax.InputSource;

/**
 * General utilities for handling of generated mets-kitodo class content.
 */
class MetsKitodoUtils {

    private static final Logger logger = LogManager.getLogger(MetsKitodoUtils.class);

    /**
     * Gets the first object of the specified type from a given object list of JAXB
     * elements.
     *
     * @param objects
     *            The list of objects.
     * @param type
     *            The type of object to return.
     * @return The first object that corresponds to the given type.
     */
    static <T> T getFirstGenericTypeFromJaxbObjectList(List<Object> objects, Class<T> type) {
        if (jaxbObjectListContainsType(objects, type)) {
            for (Object object : objects) {
                if (object instanceof JAXBElement) {
                    JAXBElement jaxbElement = (JAXBElement) object;
                    if (type.isInstance(jaxbElement.getValue())) {
                        return (type.cast(jaxbElement.getValue()));
                    }
                }
            }
        }
        throw new NoSuchElementException("No " + type.getName() + " objects found");
    }

    /**
     * Checks if a List of Jaxb-Object elements contain objects of given type.
     *
     * @param objects
     *            The list of objects.
     * @param type
     *            The type of object to check.
     * @return {@code true} if the list of Jaxb-Object elements contain objects of
     *         given type. {@code false} if not.
     */
    static <T> boolean jaxbObjectListContainsType(List<Object> objects, Class<T> type) {
        for (Object object : objects) {
            if (object instanceof JAXBElement) {
                JAXBElement jaxbElement = (JAXBElement) object;
                if (type.isInstance(jaxbElement.getValue())) {
                    return true;
                }
            }
        }
        return false;
    }

    static Optional<List<Object>> getXmlDataOfMdSec(MdSecType mdSecType) {
        // Wrapping null-checks at getter-chain into Optional<T>.class
        return Optional.ofNullable(mdSecType).map(MdSecType::getMdWrap).map(MdSecType.MdWrap::getXmlData)
                .map(MdSecType.MdWrap.XmlData::getAny);
    }

    static boolean metsContainsMetadataAtMdSecIndex(Mets mets, int index) {
        if (mets.getDmdSec().size() > index) {
            Optional<List<Object>> xmlData = getXmlDataOfMdSec(mets.getDmdSec().get(index));
            if (xmlData.isPresent()) {
                return true;
            }
        }
        return false;
    }

    static List<Object> getXmlDataOfMetsByMdSecIndex(Mets mets, int index) {
        if (mets.getDmdSec().size() > index) {
            Optional<List<Object>> xmlData = getXmlDataOfMdSec(mets.getDmdSec().get(index));
            if (xmlData.isPresent()) {
                return xmlData.get();
            }
            throw new NoSuchElementException("MdSec element with index: " + index + " does not have data");
        }
        throw new NoSuchElementException("MdSec element with index: " + index + " does not exist");
    }

    static boolean checkValidMetsKitodoFormat(Mets mets) {
        return jaxbObjectListContainsType(getXmlDataOfMetsByMdSecIndex(mets, 0), KitodoType.class);
    }

    static Mets readFromOldFormat(URI file) throws TransformerException, IOException, JAXBException {
        URI xslFile = URI.create("./src/main/resources/xslt/MetsModsGoobi_to_MetsKitodo.xsl");
        String convertedData = XmlUtils.transformXmlByXslt(file, xslFile);
        return readStringToMets(convertedData);
    }

    private static Mets readStringToMets(String xmlString) throws JAXBException {
        JAXBContext jaxbMetsContext = JAXBContext.newInstance(Mets.class);
        Unmarshaller jaxbUnmarshaller = jaxbMetsContext.createUnmarshaller();
        try (StringReader stringReader = new StringReader(xmlString)) {
            return (Mets) jaxbUnmarshaller.unmarshal(new InputSource(stringReader));
        }
    }

    private static Mets readUriToMets(URI xmlFile) throws JAXBException, XMLStreamException {
        JAXBContext jaxbMetsContext = JAXBContext.newInstance(Mets.class);
        Unmarshaller jaxbUnmarshaller = jaxbMetsContext.createUnmarshaller();

        // using a stream filter to prevent accepting white space and new line content
        // in an element that has mixed context
        XMLInputFactory xif = XMLInputFactory.newFactory();
        XMLStreamReader xmlStreamReader = null;

        try {
            xmlStreamReader = xif.createXMLStreamReader(new StreamSource(xmlFile.getPath()));
            xmlStreamReader = xif.createFilteredReader(xmlStreamReader, new XmlStreamFilter());

            return (Mets) jaxbUnmarshaller.unmarshal(xmlStreamReader);
        } finally {
            if (Objects.nonNull(xmlStreamReader)) {
                xmlStreamReader.close();
            }
        }
    }

    static Mets readAndValidateUriToMets(URI xmlFile)
            throws JAXBException, XMLStreamException, TransformerException, IOException {

        Mets mets = readUriToMets(xmlFile);

        if (metsContainsMetadataAtMdSecIndex(mets, 0)) {
            if (!checkValidMetsKitodoFormat(mets)) {
                logger.warn("Not supported format detected. Trying to convert from old goobi format now!");
                mets = readFromOldFormat(xmlFile);
            }
            if (!checkValidMetsKitodoFormat(mets)) {
                throw new IOException("Can not read data because of not supported format!");
            } else {
                logger.info("Successfully converted metadata to kitodo format!");
            }
        } else {
            logger.warn("Metadata file does not contain any metadata");
        }
        return mets;
    }
}
