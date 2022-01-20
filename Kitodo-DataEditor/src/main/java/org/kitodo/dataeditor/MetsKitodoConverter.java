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
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Paths;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.TransformerException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.filemanagement.FileManagementInterface;
import org.kitodo.dataeditor.handlers.MetsKitodoHeaderHandler;
import org.kitodo.dataformat.metskitodo.Mets;
import org.kitodo.serviceloader.KitodoServiceLoader;
import org.kitodo.utils.JAXBContextCache;

/**
 * Provides methods to convert mets-mods-goobi xml files to the current used mets-kitodo format.
 */
public class MetsKitodoConverter {

    private static final Logger logger = LogManager.getLogger(MetsKitodoConverter.class);
    private static FileManagementInterface fileManagementModule = new KitodoServiceLoader<FileManagementInterface>(
            FileManagementInterface.class).loadModule();

    /**
     * Private constructor to hide the implicit public one.
     */
    private MetsKitodoConverter() {
    }

    /**
     * Reads an old mets-mods-goobi formatted xml file by transforming it to the
     * current used mets-kitodo format by xslt.
     *
     * @param xmlFile
     *            The xml file as URI object.
     * @param xsltFile
     *            The xslt file as URI object.
     * @return The Mets object in mets-kitodo format.
     */
    public static Mets convertToMetsKitodoByXslt(URI xmlFile, URI xsltFile) throws IOException, TransformerException, JAXBException {
        if (!Paths.get(xsltFile).toFile().exists()) {
            logger.error("Path to xslt file for transformation of goobi format metadata files is not valid: "
                + xmlFile.getPath());
            throw new IOException("Xslt file [" + xsltFile.getPath()
                + "] for transformation of goobi format metadata files was not found. Please check your local config!");
        } else {
            Mets mets = convertUriToMetsFromGoobiFormat(xmlFile, xsltFile);
            if (MetsKitodoValidator.checkMetsKitodoFormatOfMets(mets)) {
                logger.info("Successfully converted metadata to kitodo format!");
                return mets;
            } else {
                throw new IOException("Can not read data because of not supported format!");
            }
        }
    }

    private static Mets convertUriToMetsFromGoobiFormat(URI xmlFile, URI xsltFile)
        throws TransformerException, IOException, JAXBException {
        String convertedData = JaxbXmlUtils.transformXmlByXslt(xmlFile, xsltFile);
        Mets mets = MetsKitodoReader.readStringToMets(convertedData);
        mets = MetsKitodoHeaderHandler
            .addNoteToMetsHeader("Converted by " + VersionProvider.getModuleVersionInfo(), mets);
        saveToFile(mets,xmlFile);

        return mets;
    }

    private static void saveToFile(Mets mets, URI xmlFile) throws JAXBException, IOException {
        URI metsFileUri = fileManagementModule.getFile(xmlFile).toURI();
        try (OutputStream outputStream = fileManagementModule.write(metsFileUri)) {
            JAXBContext context = JAXBContextCache.getJAXBContext(Mets.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(mets, outputStream);
        }
    }
}
