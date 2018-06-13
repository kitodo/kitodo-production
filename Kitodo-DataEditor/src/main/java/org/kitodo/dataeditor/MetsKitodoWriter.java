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
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.Objects;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;

import org.kitodo.dataformat.metskitodo.Mets;

/**
 * This class provides methods for writing Mets objects to xml files.
 */
public class MetsKitodoWriter {

    private MetsKitodoObjectFactory objectFactory = new MetsKitodoObjectFactory();
    private JAXBContext jaxbMetsContext;
    private Marshaller jaxbMetsMarshaller;

    /**
     * The Constructor which instanciates the JAXB context of MetsKitodo format.
     */
    public MetsKitodoWriter() throws JAXBException {
        jaxbMetsContext = JAXBContext.newInstance(Mets.class);
        jaxbMetsMarshaller = jaxbMetsContext.createMarshaller();
    }

    /**
     * Updating Mets header by inserting a new header if no one exists, updating
     * last modification date and writing the serialized Mets object to specified file path in
     * xml format.
     *
     * @param mets
     *            The Mets object.
     * @param filePath
     *            The file path to write the xml file.
     */
    public void writeSerializedToFile(Mets mets, URI filePath) throws JAXBException, DatatypeConfigurationException, IOException {
        insertMetsHeaderIfNotExist(mets);
        updateLastModDate(mets);
        writeMetsData(mets, filePath);
    }

    /**
     * Serializes a given Mets object to xml format and returns as String.
     *
     * @param mets
     *            The mets object.
     */
    public String writeSerializedToString(Mets mets) throws JAXBException {
        jaxbMetsMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        jaxbMetsMarshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new MetsKitodoPrefixMapper());
        StringWriter stringWriter = new StringWriter();
        jaxbMetsMarshaller.marshal(mets, stringWriter);
        return stringWriter.toString();
    }

    private void writeMetsData(Mets mets, URI file) throws JAXBException {
        jaxbMetsMarshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new MetsKitodoPrefixMapper());
        jaxbMetsMarshaller.marshal(mets, new File(file));
    }

    private void insertMetsHeaderIfNotExist(Mets mets) throws DatatypeConfigurationException, IOException {
        if (Objects.isNull(mets.getMetsHdr())) {
            mets.setMetsHdr(objectFactory.createKitodoMetsHeader());
        }
    }

    private void updateLastModDate(Mets mets) throws DatatypeConfigurationException {
        mets.getMetsHdr().setLASTMODDATE(JaxbXmlUtils.getXmlTime());
    }
}
