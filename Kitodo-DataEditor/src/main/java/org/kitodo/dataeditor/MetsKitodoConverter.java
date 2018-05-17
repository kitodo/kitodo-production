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
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.dataformat.metskitodo.Mets;

public class MetsKitodoConverter {

    private static final Logger logger = LogManager.getLogger(MetsKitodoConverter.class);

    /**
     * Reads an old mets-mods-goobi formated xml file by transforming it to the
     * current used mets-kitodo format by xslt.
     *
     * @param xmlFile
     *            The xml file as URI object.
     * @param xsltFile
     *            The xslt file as URI object.
     * @return The Mets object in mets-kitodo format.
     */
    public static Mets convertToMetsKitodo(URI xmlFile, URI xsltFile) throws IOException, TransformerException, JAXBException {
        if (!Files.exists(Paths.get(xsltFile))) {
            logger.error("Path to xslt file for transformation of goobi format metadata files is not valid: "
                + xmlFile.getPath());
            throw new IOException("Xslt file [" + xsltFile.getPath()
                + "] for transformation of goobi format metadata files was not found. Please check your local config!");
        } else {
            Mets mets = convertUriToMetsFromOldFormat(xmlFile, xsltFile);
            if (MetsKitodoValidator.checkMetsKitodoFormatOfMets(mets)) {
                logger.info("Successfully converted metadata to kitodo format!");
                return mets;
            } else {
                throw new IOException("Can not read data because of not supported format!");
            }
        }
    }

    private static Mets convertUriToMetsFromOldFormat(URI xmlFile, URI xsltFile)
        throws TransformerException, IOException, JAXBException {
        String convertedData = XmlUtils.transformXmlByXslt(xmlFile, xsltFile);
        Mets mets = MetsKitodoReader.readStringToMets(convertedData);
        mets = MetsKitodoHandler
            .addNoteToMetsHeader("Converted by " + VersionFinder.findVersionInfo("Kitodo - Data Editor"), mets);
        return mets;
    }
}
