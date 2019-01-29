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

package org.kitodo.production.export;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Paths;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FilenameUtils;
import org.kitodo.data.database.beans.Process;
import org.kitodo.production.config.ConfigCore;
import org.kitodo.production.config.enums.ParameterCore;

public class XsltHelper {

    /**
     * Transforms a xml file by xslt and returns the result as string.
     *
     * @param source  The xml file to transform.
     * @param xslFile The xsl file.
     * @return The Result of the transformation as String object.
     */
    static ByteArrayOutputStream transformXmlByXslt(StreamSource source, URI xslFile) throws TransformerException, IOException {
        StreamSource xsltSource = new StreamSource(xslFile.getPath());
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer(xsltSource);
        try (OutputStream outputStream = new ByteArrayOutputStream()) {
            StreamResult result = new StreamResult(outputStream);
            transformer.transform(source, result);
            return (ByteArrayOutputStream) outputStream;
        }
    }

    static URI getXsltFileFromConfig(Process process) {
        String path = ConfigCore.getParameter(ParameterCore.DIR_XSLT);
        String file = process.getTemplate().getRuleset().getFile().replaceFirst("\\.[Xx][Mm][Ll]$", ".xsl");
        return Paths.get(FilenameUtils.concat(path, file)).toUri();
    }

}
