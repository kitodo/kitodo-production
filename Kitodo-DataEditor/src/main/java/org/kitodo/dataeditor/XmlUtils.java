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
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class XmlUtils {

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
}
