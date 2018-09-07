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
import java.io.StringWriter;
import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.joda.time.DateTime;
import org.kitodo.dataformat.metskitodo.KitodoType;
import org.kitodo.dataformat.metskitodo.MdSecType;

/**
 * Provides methods for handling jaxb generated java objects and xml files.
 */
public class JaxbXmlUtils {

    /**
     * Private constructor to hide the implicit public one.
     */
    private JaxbXmlUtils() {
    }

    /**
     * Transforms a xml file by xslt and returns the result as string.
     *
     * @param xmlFile
     *            The xml file to transform.
     * @param xslFile
     *            The xsl file.
     * @return The Result of the transformation as String object.
     */
    static String transformXmlByXslt(URI xmlFile, URI xslFile) throws TransformerException, IOException {
        StreamSource source = new StreamSource(xmlFile.getPath());
        StreamSource styleSource = new StreamSource(xslFile.getPath());

        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer(styleSource);

        try (StringWriter stringWriter = new StringWriter()) {
            StreamResult result = new StreamResult(stringWriter);
            transformer.transform(source, result);
            return stringWriter.toString();
        }
    }

    /**
     * Gets current time as XMLGregorianCalender.
     *
     * @return The current time as XMLGregorianCalender object.
     */
    static XMLGregorianCalendar getXmlTime() throws DatatypeConfigurationException {
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(new DateTime().toGregorianCalendar());
    }

    /**
     * Checks if a List of Jaxb Object elements contain objects of given type.
     *
     * @param objects
     *            The list of Jaxb objects.
     * @param type
     *            The type of object to check.
     * @return {@code true} if the list of Jaxb-Object elements contain objects of
     *         given type. {@code false} if not.
     */
    static <T> boolean objectListContainsType(List<Object> objects, Class<T> type) {
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
        if (JaxbXmlUtils.objectListContainsType(objects, type)) {
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
     * Returns the KitodoType object of an MdSecType object.
     *
     * @param dmdSecElement
     *            The DmdSecElement as MdSecType object.
     * @return The KitodoType object.
     */
    public static KitodoType getKitodoTypeOfDmdSecElement(MdSecType dmdSecElement) {
        Optional<List<Object>> xmlDataOfMdSec = getXmlDataOfMdSec(dmdSecElement);
        if (xmlDataOfMdSec.isPresent()) {
            return JaxbXmlUtils.getFirstGenericTypeFromJaxbObjectList(xmlDataOfMdSec.get(), KitodoType.class);
        }
        throw new NoSuchElementException("DmdSec element with id " + dmdSecElement.getID() + " does not have xml data");
    }

    /**
     * Gets an optional list of objects which holds the xml data of an mets object
     * mdSec element.
     *
     * @param mdSecType
     *            The mdSec element.
     * @return A list of objects wraped in Optional class.
     */
    static Optional<List<Object>> getXmlDataOfMdSec(MdSecType mdSecType) {
        // Wrapping null-checks at getter-chain into Optional<T>.class
        return Optional.ofNullable(mdSecType).map(MdSecType::getMdWrap).map(MdSecType.MdWrap::getXmlData)
            .map(MdSecType.MdWrap.XmlData::getAny);
    }
}
