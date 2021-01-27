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
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Objects;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.TransformerFactoryImpl;

import org.apache.commons.io.FilenameUtils;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Process;

public class XsltHelper {

    private XsltHelper() {
        // private constructor to hide implicit one
    }

    /**
     * Transforms a xml file by xslt and returns the result as string.
     *
     * @param source
     *            The xml file to transform.
     * @param xslFile
     *            The xsl file.
     * @return The Result of the transformation as String object.
     */
    static ByteArrayOutputStream transformXmlByXslt(StreamSource source, URI xslFile)
            throws TransformerException, IOException {

        String xsltPath = xslFile.getPath();
        StreamSource xsltSource = new StreamSource(xsltPath);
        TransformerFactory factory = new TransformerFactoryImpl();
        Transformer transformer = factory.newTransformer(xsltSource);
        if (Objects.isNull(transformer)) {
            throw new IllegalArgumentException("Could not create XSLT transformer. Check " + xsltPath + " for errors.");
        }
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            StreamResult streamResult = new StreamResult(outputStream);
            transformer.transform(source, streamResult);
            return outputStream;
        }
    }

    static URI getXsltFileFromConfig(Process process) {
        String path = ConfigCore.getParameter(ParameterCore.DIR_XSLT);
        String file = process.getRuleset().getFile().replaceFirst("\\.[Xx][Mm][Ll]$", ".xsl");
        return Paths.get(FilenameUtils.concat(path, file)).toUri();
    }

}
