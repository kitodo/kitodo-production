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
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Process;
import org.kitodo.utils.XMLSecurity;
import org.xml.sax.SAXException;

public class XsltHelper {

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
        InputStream inputStream;
        try {
            inputStream = getInputStream(source);
            secureSource = XMLSecurity.newSecureSource(inputStream);
        } catch (ParserConfigurationException | SAXException e) {
            throw new IllegalStateException("Unable to create hardened SAX source", e);
        }
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); InputStream in = inputStream) {
            StreamResult streamResult = new StreamResult(outputStream);
            transformer.transform(secureSource, streamResult);
            return outputStream;
        }
    }

    private static InputStream getInputStream(StreamSource source) throws IOException {
        if (Objects.nonNull(source.getInputStream())) {
            return source.getInputStream();
        }
        return new FileInputStream(Paths.get(source.getSystemId()).toFile());
    }

    static URI getXsltFileFromConfig(Process process) {
        String path = ConfigCore.getParameter(ParameterCore.DIR_XSLT);
        String file = process.getRuleset().getFile().replaceFirst("\\.[Xx][Mm][Ll]$", ".xsl");
        return Paths.get(FilenameUtils.concat(path, file)).toUri();
    }

}
