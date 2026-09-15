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

package org.kitodo.export;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Objects;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Process;
import org.kitodo.utils.XMLSecurity;
import org.xml.sax.SAXException;

public class XsltHelper {

    private static final Logger logger = LogManager.getLogger(XsltHelper.class);

    private XsltHelper() {
        // private constructor to hide implicit one
    }

    /**
     * Transforms a xml file by xslt and returns the result as string. The input
     * document is parsed with a hardened SAX parser that rejects DOCTYPE
     * declarations and external entity resolution, so the Saxon transformer
     * never sees untrusted XML features.
     *
     * @param source
     *            The xml file to transform.
     * @param xslFile
     *            The xsl file.
     * @return The Result of the transformation as String object.
     * @throws IOException if the input cannot be read
     */
    static ByteArrayOutputStream transformXmlByXslt(StreamSource source, URI xslFile)
            throws TransformerException, IOException {

        String xsltPath = xslFile.getPath();
        StreamSource xsltSource = new StreamSource(xsltPath);
        TransformerFactory factory = XMLSecurity.newTransformerFactory();
        Transformer transformer = factory.newTransformer(xsltSource);
        if (Objects.isNull(transformer)) {
            throw new IllegalArgumentException("Could not create XSLT transformer. Check " + xsltPath + " for errors.");
        }
        SAXSource secureSource;
        InputStream streamToClose = null;
        try {
            if (Objects.nonNull(source.getReader())) {
                secureSource = XMLSecurity.newSecureSource(source.getReader());
            } else if (Objects.nonNull(source.getInputStream())) {
                streamToClose = source.getInputStream();
                secureSource = XMLSecurity.newSecureSource(streamToClose);
            } else if (Objects.nonNull(source.getSystemId())) {
                String systemId = source.getSystemId();
                streamToClose = new FileInputStream(systemId.startsWith("file:")
                        ? Paths.get(URI.create(systemId)).toFile()
                        : Paths.get(systemId).toFile());
                secureSource = XMLSecurity.newSecureSource(streamToClose);
            } else {
                throw new IllegalArgumentException("StreamSource has neither an input stream, a reader, nor a system ID");
            }
        } catch (ParserConfigurationException | SAXException e) {
            closeQuietly(streamToClose);
            throw new IllegalStateException("Unable to create hardened SAX source", e);
        }
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            StreamResult streamResult = new StreamResult(outputStream);
            transformer.transform(secureSource, streamResult);
            return outputStream;
        } finally {
            closeQuietly(streamToClose);
        }
    }

    private static void closeQuietly(InputStream stream) {
        if (Objects.nonNull(stream)) {
            try {
                stream.close();
            } catch (IOException e) {
                logger.debug("Ignoring error while closing XML input stream", e);
            }
        }
    }

    static URI getXsltFileFromConfig(Process process) {
        String path = ConfigCore.getParameter(ParameterCore.DIR_XSLT);
        String file = process.getRuleset().getFile().replaceFirst("\\.[Xx][Mm][Ll]$", ".xsl");
        return Paths.get(FilenameUtils.concat(path, file)).toUri();
    }

}
