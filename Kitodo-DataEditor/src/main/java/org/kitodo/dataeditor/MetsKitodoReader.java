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
import java.util.Objects;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.dataformat.metskitodo.Mets;
import org.xml.sax.InputSource;

public class MetsKitodoReader {

    private static final Logger logger = LogManager.getLogger(MetsKitodoReader.class);

    /**
     * Reads an old mets-mods-goobi formated xml file by transforming it to the
     * current used mets-kitodo format.
     * 
     * @param xmlFile
     *            The file as URI object.
     * @return The Mets object in mets-kitodo format.
     */
    static Mets readUriToMetsFromOldFormat(URI xmlFile) throws TransformerException, IOException, JAXBException {
        URI xslFile = URI.create("./src/main/resources/xslt/MetsModsGoobi_to_MetsKitodo.xsl");
        String convertedData = XmlUtils.transformXmlByXslt(xmlFile, xslFile);
        return readStringToMets(convertedData);
    }

    /**
     * Reads a mets-kitodo formated xml String to a Mets object.
     * 
     * @param xmlString
     *            The mets-kitodo formated xml String.
     * 
     * @return The Mets object in mets-kitodo format.
     */
    private static Mets readStringToMets(String xmlString) throws JAXBException {
        JAXBContext jaxbMetsContext = JAXBContext.newInstance(Mets.class);
        Unmarshaller jaxbUnmarshaller = jaxbMetsContext.createUnmarshaller();
        try (StringReader stringReader = new StringReader(xmlString)) {
            return (Mets) jaxbUnmarshaller.unmarshal(new InputSource(stringReader));
        }
    }

    /**
     * Reads an URI mets-kitodo formated xml file to a Mets object.
     * 
     * @param xmlFile
     *            The file as URI object.
     * @return The Mets object in mets-kitodo format.
     */
    private static Mets readUriToMets(URI xmlFile) throws JAXBException {
        JAXBContext jaxbMetsContext = JAXBContext.newInstance(Mets.class);
        Unmarshaller jaxbUnmarshaller = jaxbMetsContext.createUnmarshaller();
        return (Mets) jaxbUnmarshaller.unmarshal(new StreamSource(xmlFile.getPath()));
    }

    /**
     * Reads an URI mets-kitodo formated xml file to a Mets object and checking if
     * the file contains metadata. If yes, it is also checked if the first metadata
     * is in kitodo format. If not, it is tried to convert to kitodo format,
     * assuming that the file contains old goobi format metadata.
     * 
     * @param xmlFile
     *            The file as URI object.
     * @return The Mets object in mets-kitodo format.
     */
    static Mets readAndValidateUriToMets(URI xmlFile)
            throws JAXBException, TransformerException, IOException {

        Mets mets = readUriToMets(xmlFile);

        if (MetsKitodoUtils.metsContainsMetadataAtMdSecIndex(mets, 0)) {
            if (!MetsKitodoValidator.checkValidMetsKitodoFormat(mets)) {
                logger.warn("Not supported format detected. Trying to convert from old goobi format now!");
                mets = readUriToMetsFromOldFormat(xmlFile);
            }
            if (!MetsKitodoValidator.checkValidMetsKitodoFormat(mets)) {
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
