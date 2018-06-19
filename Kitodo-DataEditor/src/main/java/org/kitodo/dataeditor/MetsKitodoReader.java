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
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.TransformerException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.dataformat.metskitodo.Mets;
import org.xml.sax.InputSource;

/**
 * This class provides methods for reading sources to Mets objects.
 */
class MetsKitodoReader {

    private static final Logger logger = LogManager.getLogger(MetsKitodoReader.class);

    /**
     * Reads a mets-kitodo formated xml String to a Mets object.
     *
     * @param xmlString
     *            The mets-kitodo formated xml String.
     * 
     * @return The Mets object in mets-kitodo format.
     */
    static Mets readStringToMets(String xmlString) throws JAXBException {
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
    static Mets readUriToMets(URI xmlFile) throws JAXBException, IOException {
        if (Files.exists(Paths.get(xmlFile))) {
            JAXBContext jaxbMetsContext = JAXBContext.newInstance(Mets.class);
            Unmarshaller jaxbUnmarshaller = jaxbMetsContext.createUnmarshaller();
            return (Mets) jaxbUnmarshaller.unmarshal(xmlFile.toURL());
        } else {
            throw new IOException("File was not found: " + xmlFile.getPath());
        }
    }

    /**
     * Reads an URI mets-kitodo formated xml file to a Mets object and checking if
     * the file contains metadata. If yes, it is also checked if the first metadata
     * is in kitodo format. If not, it is tried to convert to kitodo format,
     * assuming that the file contains old goobi format metadata.
     *
     * @param xmlFile
     *            The file as URI object.
     * @param xsltFile
     *            The URI to the xsl file for transformation of old format goobi
     *            metadata files
     * @return The Mets object in mets-kitodo format.
     */
    static Mets readAndValidateUriToMets(URI xmlFile, URI xsltFile)
            throws JAXBException, TransformerException, IOException {

        Mets mets = readUriToMets(xmlFile);

        if (MetsKitodoValidator.metsContainsMetadataAtDmdSecIndex(mets, 0)) {
            if (!MetsKitodoValidator.checkMetsKitodoFormatOfMets(mets)) {
                logger.warn("Not supported metadata format detected. Trying to convert from old goobi format now!");
                return MetsKitodoConverter.convertToMetsKitodo(xmlFile, xsltFile);
            }
        } else {
            logger.warn("Metadata file does not contain any metadata");
        }
        return mets;
    }
}
