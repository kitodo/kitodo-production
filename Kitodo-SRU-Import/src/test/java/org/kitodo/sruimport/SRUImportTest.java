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

package org.kitodo.sruimport;

import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.semantics.Action.contentType;
import static com.xebialabs.restito.semantics.Action.ok;
import static com.xebialabs.restito.semantics.Action.stringContent;
import static com.xebialabs.restito.semantics.Condition.get;
import static com.xebialabs.restito.semantics.Condition.parameter;

import static org.hamcrest.CoreMatchers.instanceOf;

import com.xebialabs.restito.server.StubServer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.api.schemaconverter.DataRecord;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SRUImportTest {

    private static StubServer server;
    private static final String TEST_FILE_PATH = "src/test/resources/sruTestRecord.xml";
    private static final String OPAC_NAME = "Kalliope";
    private static final String RECORD_ID = "1";
    private static final String RECORD_IDENTIFIER_VALUE = "12345";

    @BeforeClass
    public static void setup() throws IOException {
        server = new StubServer(80).run();
        try (InputStream inputStream = Files.newInputStream(Paths.get(TEST_FILE_PATH))) {
            setupServer(IOUtils.toString(inputStream, StandardCharsets.UTF_8));
        }
    }

    @Test
    public void shouldGetFullRecordById() throws IOException, SAXException, ParserConfigurationException {
        SRUImport sruImport = new SRUImport();
        DataRecord importRecord = sruImport.getFullRecordById(OPAC_NAME, RECORD_ID);
        Assert.assertNotNull(importRecord);
        Assert.assertThat("Original data of data record has wrong class!",
                importRecord.getOriginalData(), instanceOf(String.class));
        Document xmlDocument = parseInputStreamToDocument((String) importRecord.getOriginalData());
        NodeList recordIdentifierNodeList = xmlDocument.getElementsByTagName("recordIdentifier");
        Assert.assertEquals("Wrong number of record identifiers found!", 1,
                recordIdentifierNodeList.getLength());
        Element recordIdentifierElement = (Element) recordIdentifierNodeList.item(0);
        Assert.assertEquals("Wrong record identifier found!", RECORD_IDENTIFIER_VALUE,
                recordIdentifierElement.getTextContent());
    }

    private static void setupServer(String serverResponse) {
        // endpoint for importing record by id
        whenHttp(server)
                .match(get("/sru"),
                        parameter("version", "1.2"),
                        parameter("operation", "searchRetrieve"),
                        parameter("recordSchema", "mods"),
                        parameter("maximumRecords", "1"),
                        parameter("query", "ead.id=" + RECORD_ID))
                .then(ok(), contentType("text/xml"), stringContent(serverResponse));
    }

    private Document parseInputStreamToDocument(String inputString) throws ParserConfigurationException,
            IOException, SAXException {
        try (InputStream inputStream = new ByteArrayInputStream(inputString.getBytes())) {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            return documentBuilderFactory.newDocumentBuilder().parse(inputStream);
        }
    }

    @AfterClass
    public static void shutdown() {
        server.stop();
    }

}
