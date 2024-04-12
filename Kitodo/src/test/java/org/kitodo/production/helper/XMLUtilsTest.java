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

package org.kitodo.production.helper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kitodo.production.model.bibliography.course.Course;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class XMLUtilsTest {

    private static final String VALID_XPATH = ".//this/is/a/valid/xpath";
    private static final String INVALID_XPATH = ".//[";
    private static final String TEST_ELEMENT = "testElement";
    private static final String TEST_CHILD_ELEMENT = "testChildElement";
    private static final String XML_STRING = "<document><element>Text</element></document>";
    private static final String EXPECTED_EXCEPTION_MESSAGE = "javax.xml.transform.TransformerException: "
            + "Nach dem Token \"/\" oder \"//\" wurde ein Verzeichnisschritt erwartet.";

    @Test
    public void shouldConvertDocumentToByteArray() throws IOException, TransformerException {
        Course course = new Course();
        Document document = course.toXML();
        byte[] byteArray = XMLUtils.documentToByteArray(document, 2);
        Assertions.assertNotNull(byteArray);
    }

    @Test
    public void shouldGetFirstChildWithTagName() throws IOException {
        Document document = XMLUtils.newDocument();
        Node parentNode = document.createElement(TEST_ELEMENT);
        parentNode.appendChild(document.createElement(TEST_CHILD_ELEMENT));
        Element childNode = XMLUtils.getFirstChildWithTagName(parentNode, TEST_CHILD_ELEMENT);
        Assertions.assertNotNull(childNode);
    }

    @Test
    public void shouldLoadInputStream() {
        InputStream inputStream = new ByteArrayInputStream(XML_STRING.getBytes());
        Assertions.assertDoesNotThrow(() -> XMLUtils.load(inputStream));
    }

    @Test
    public void shouldCreateNewDocument() {
        Assertions.assertDoesNotThrow(XMLUtils::newDocument);
    }

    @Test
    public void shouldParseXMLString() {
        Assertions.assertDoesNotThrow(() -> XMLUtils.parseXMLString(XML_STRING));
    }

    @Test
    public void shouldValidateXpathSyntax() {
        Assertions.assertDoesNotThrow(() -> XMLUtils.validateXPathSyntax(VALID_XPATH));
    }

    @Test
    public void shouldThrowExceptionWhenValidatingInvalidXPathSyntax() {
        XPathExpressionException exception = Assertions.assertThrows(XPathExpressionException.class,
                () -> XMLUtils.validateXPathSyntax(INVALID_XPATH));
        Assertions.assertEquals(EXPECTED_EXCEPTION_MESSAGE, exception.getMessage());
    }
}
