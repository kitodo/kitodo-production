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

package org.kitodo.queryurlimport;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.kitodo.api.externaldatamanagement.DataImport;
import org.kitodo.api.externaldatamanagement.SearchInterfaceType;
import org.kitodo.api.externaldatamanagement.SearchResult;
import org.kitodo.api.externaldatamanagement.SingleHit;
import org.kitodo.exceptions.CatalogException;
import org.kitodo.exceptions.ConfigException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

class XmlResponseHandler {

    private static final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    private static final XMLOutputter xmlOutputter = new XMLOutputter();
    private static final XPath xPath = XPathFactory.newInstance().newXPath();

    static {
        documentBuilderFactory.setNamespaceAware(true);
        try {
            documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        } catch (ParserConfigurationException parserConfigurationException) {
            throw new UndeclaredThrowableException(parserConfigurationException);
        }
        xmlOutputter.setFormat(Format.getPrettyFormat());
    }

    /**
     * Create and return SearchResult for given HttpResponse.
     * @param response HttpResponse for which a SearchResult is created
     * @return SearchResult created from given HttpResponse
     */
    static SearchResult getSearchResult(HttpResponse response, DataImport dataImport) throws IOException {
        SearchInterfaceType interfaceType = dataImport.getSearchInterfaceType();
        SearchResult searchResult = new SearchResult();
        if (Objects.nonNull(response) && Objects.nonNull(response.getEntity())) {
            String content = IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());
            Document resultDocument = transformResponseToDocument(content);
            if (Objects.nonNull(resultDocument)) {
                searchResult.setHits(extractHits(resultDocument, dataImport));
                if (Objects.nonNull(interfaceType.getNumberOfRecordsString())) {
                    searchResult.setNumberOfHits(extractNumberOfRecords(resultDocument, interfaceType));
                } else {
                    searchResult.setNumberOfHits(searchResult.getHits().size());
                }
                if (searchResult.getNumberOfHits() < 1 && Objects.nonNull(interfaceType.getErrorMessageXpath())) {
                    String errorMessage = getTextContent(resultDocument.getDocumentElement(),
                            interfaceType.getErrorMessageXpath());
                    if (StringUtils.isNotBlank(errorMessage)) {
                        errorMessage = interfaceType.getTypeString().toUpperCase() + " error: '" + errorMessage + "'";
                        throw new CatalogException(errorMessage);
                    }
                }
            }
        }
        return searchResult;
    }

    /**
     * Transform given HttpResponse into Document and return it.
     * @param content String that is transformed into a Document
     * @return Document into which given HttpResponse has been transformed
     */
    private static Document transformResponseToDocument(String content) {
        if (Objects.nonNull(content)) {
            return parseXML(content);
        }
        throw new ConfigException("Query response is null");
    }

    private static Document parseXML(String xmlString) {
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            return documentBuilder.parse(new InputSource(new ByteArrayInputStream(xmlString
                    .getBytes(StandardCharsets.UTF_8))));
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new ConfigException(e.getMessage());
        }
    }

    private static LinkedList<SingleHit> extractHits(Document document, DataImport dataImport) {
        SearchInterfaceType type = dataImport.getSearchInterfaceType();
        LinkedList<SingleHit> hits = new LinkedList<>();
        NodeList records = document.getElementsByTagNameNS(type.getNamespace(), type.getRecordString());
        for (int i = 0; i < records.getLength(); i++) {
            Element recordElement = (Element) records.item(i);
            String recordTitle = getTextContent(recordElement, dataImport.getRecordTitleXPath());
            String recordId = getTextContent(recordElement, dataImport.getRecordIdXPath());
            hits.add(new SingleHit(recordTitle, recordId));
        }
        return hits;
    }

    static int extractNumberOfRecords(String content, SearchInterfaceType type) {
        return extractNumberOfRecords(transformResponseToDocument(content), type);
    }

    private static int extractNumberOfRecords(Document document, SearchInterfaceType type) {
        NodeList numberOfRecordNodes = document.getElementsByTagNameNS(type.getNamespace(), type.getNumberOfRecordsString());
        assert numberOfRecordNodes.getLength() == 1;
        Element numberOfRecordsElement = (Element) numberOfRecordNodes.item(0);
        if (Objects.nonNull(numberOfRecordsElement)) {
            return Integer.parseInt(numberOfRecordsElement.getTextContent().trim());
        }
        return 0;
    }

    static String getTextContent(Element element, String xpathString) {
        try {
            return xPath.evaluate(xpathString, element);
        } catch (XPathExpressionException e) {
            return "";
        }
    }
}
