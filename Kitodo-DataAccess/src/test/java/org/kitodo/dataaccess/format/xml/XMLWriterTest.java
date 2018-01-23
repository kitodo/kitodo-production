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
package org.kitodo.dataaccess.format.xml;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.util.Collections;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.kitodo.dataaccess.Node;
import org.kitodo.dataaccess.RDF;
import org.kitodo.dataaccess.SerializationFormat;
import org.kitodo.dataaccess.storage.memory.MemoryLiteral;
import org.kitodo.dataaccess.storage.memory.MemoryNode;
import org.kitodo.dataaccess.storage.memory.MemoryNodeReference;
import org.kitodo.dataaccess.storage.memory.MemoryStorage;

public class XMLWriterTest {
    private static final MemoryNodeReference METS_AGENT = new MemoryNodeReference("http://www.loc.gov/METS/agent");
    private static final MemoryNodeReference METS_CREATEDATE = new MemoryNodeReference(
            "http://www.loc.gov/METS/CREATEDATE");
    private static final MemoryNodeReference METS_DIV = new MemoryNodeReference("http://www.loc.gov/METS/div");
    private static final MemoryNodeReference METS_DMD_SEC = new MemoryNodeReference("http://www.loc.gov/METS/dmdSec");
    private static final MemoryNodeReference METS_DMDID = new MemoryNodeReference("http://www.loc.gov/METS/DMDID");
    private static final MemoryNodeReference METS_F_LOCAT = new MemoryNodeReference("http://www.loc.gov/METS/FLocat");
    private static final MemoryNodeReference METS_FILE = new MemoryNodeReference("http://www.loc.gov/METS/file");
    private static final MemoryNodeReference METS_FILE_GRP = new MemoryNodeReference("http://www.loc.gov/METS/fileGrp");
    private static final MemoryNodeReference METS_FILE_SEC = new MemoryNodeReference("http://www.loc.gov/METS/fileSec");
    private static final MemoryNodeReference METS_FILEID = new MemoryNodeReference("http://www.loc.gov/METS/fileid");
    private static final MemoryNodeReference METS_FPTR = new MemoryNodeReference("http://www.loc.gov/METS/fptr");
    private static final MemoryNodeReference METS_ID = new MemoryNodeReference("http://www.loc.gov/METS/ID");
    private static final MemoryNodeReference METS_LABEL = new MemoryNodeReference("http://www.loc.gov/METS/LABEL");
    private static final MemoryNodeReference METS_LOCTYPE = new MemoryNodeReference("http://www.loc.gov/METS/LOCTYPE");
    private static final MemoryNodeReference METS_MD_WRAP = new MemoryNodeReference("http://www.loc.gov/METS/mdWrap");
    private static final MemoryNodeReference METS_MDTYPE = new MemoryNodeReference("http://www.loc.gov/METS/MDTYPE");
    private static final MemoryNodeReference METS_METS = new MemoryNodeReference("http://www.loc.gov/METS/mets");
    private static final MemoryNodeReference METS_METS_HDR = new MemoryNodeReference("http://www.loc.gov/METS/metsHdr");
    private static final MemoryNodeReference METS_MIMETYPE = new MemoryNodeReference(
            "http://www.loc.gov/METS/MIMETYPE");
    private static final MemoryNodeReference METS_NAME = new MemoryNodeReference("http://www.loc.gov/METS/name");
    private static final MemoryNodeReference METS_ORDER = new MemoryNodeReference("http://www.loc.gov/METS/ORDER");
    private static final MemoryNodeReference METS_ORDERLABEL = new MemoryNodeReference(
            "http://www.loc.gov/METS/ORDERLABEL");
    private static final MemoryNodeReference METS_OTHERTYPE = new MemoryNodeReference(
            "http://www.loc.gov/METS/OTHERTYPE");
    private static final MemoryNodeReference METS_ROLE = new MemoryNodeReference("http://www.loc.gov/METS/ROLE");
    private static final MemoryNodeReference METS_SM_LINK = new MemoryNodeReference("http://www.loc.gov/METS/smLink");
    private static final MemoryNodeReference METS_STRUCT_LINK = new MemoryNodeReference(
            "http://www.loc.gov/METS/structLink");
    private static final MemoryNodeReference METS_STRUCT_MAP = new MemoryNodeReference(
            "http://www.loc.gov/METS/structMap");
    private static final MemoryNodeReference METS_TYPE = new MemoryNodeReference("http://www.loc.gov/METS/TYPE");
    private static final MemoryNodeReference METS_USE = new MemoryNodeReference("http://www.loc.gov/METS/USE");
    private static final MemoryNodeReference METS_XML_DATA = new MemoryNodeReference("http://www.loc.gov/METS/xmlData");
    private static final MemoryNodeReference MODS_MODS = new MemoryNodeReference("http://www.loc.gov/mods/v3#mods");
    private static final MemoryNodeReference MODS_TITLE = new MemoryNodeReference("http://www.loc.gov/mods/v3#title");
    private static final MemoryNodeReference MODS_TITLE_INFO = new MemoryNodeReference(
            "http://www.loc.gov/mods/v3#titleInfo");

    @Test
    public void testXMLWriter() throws Exception {
        Node data = new MemoryNode(METS_METS)
                .add(new MemoryNode(METS_METS_HDR)
                        .put(METS_CREATEDATE, new MemoryLiteral("2018-01-10T11:35:45", RDF.PLAIN_LITERAL))
                        .add(new MemoryNode(METS_AGENT).put(METS_ROLE, "CREATOR").put(METS_TYPE, "OTHER")
                                .put(METS_OTHERTYPE, "SOFTWARE")
                                .add(new MemoryNode(METS_NAME)
                                        .add(new MemoryLiteral("Kitodo Production Test Suite", RDF.PLAIN_LITERAL)))))
                .add(new MemoryNode(METS_DMD_SEC).put(METS_ID, "metadata").add(
                    new MemoryNode(METS_MD_WRAP).put(METS_MDTYPE, "MODS").add(new MemoryNode(METS_XML_DATA).add(
                        new MemoryNode(MODS_MODS).add(new MemoryNode(MODS_TITLE_INFO).add(
                            new MemoryNode(MODS_TITLE).add(new MemoryLiteral("Hello World!", RDF.PLAIN_LITERAL))))))))
                .add(new MemoryNode(METS_FILE_SEC).add(new MemoryNode(METS_FILE_GRP).put(METS_USE, "DEFAULT")
                        .add(new MemoryNode(METS_FILE).put(METS_ID, "image").put(METS_MIMETYPE, "image/jpeg")
                                .add(XLink
                                        .createSimpleLink(MemoryStorage.INSTANCE, Optional.empty(),
                                            "http://data.example.org/images/hello-world.jpg", Collections.emptySet(),
                                            Collections.emptySet(), Optional.empty(), Optional.empty())
                                        .put(RDF.TYPE, METS_F_LOCAT).put(METS_LOCTYPE, "URL")))))
                .add(new MemoryNode(METS_STRUCT_MAP).put(METS_TYPE, "LOGICAL")
                        .add(new MemoryNode(METS_DIV).put(METS_ID, "greeting").put(METS_DMDID, "metadata")
                                .put(METS_LABEL, "Hello World!")))
                .add(new MemoryNode(METS_STRUCT_MAP).put(METS_TYPE, "PHYSICAL")
                        .add(new MemoryNode(METS_DIV).put(METS_TYPE, "physSequence")
                                .add(new MemoryNode(METS_DIV).put(METS_ID, "page").put(METS_ORDER, "1")
                                        .put(METS_ORDERLABEL, " - ").put(METS_TYPE, "page")
                                        .add(new MemoryNode(METS_FPTR).put(METS_FILEID, "image")))))
                .add(new MemoryNode(METS_STRUCT_LINK).add(XLink
                        .createArcLink(MemoryStorage.INSTANCE, Optional.<String>of("greeting"), Optional.empty(),
                            Optional.of("page"), Collections.emptySet(), Optional.empty(), Optional.empty())
                        .put(RDF.TYPE, METS_SM_LINK)));
        Namespaces namespaces = new Namespaces() {
            private static final long serialVersionUID = 1L;
            {
                put("mets", "http://www.loc.gov/METS/");
                put("mods", "http://www.loc.gov/mods/v3#");
                put("xlink", "http://www.w3.org/1999/xlink#");
            }
        };
        File expectedResult = new File("src/test/resources/xmlWriterTest-expectedResult.xml");
        File testfile = File.createTempFile("xmlWriterTest-", ".xml");
        try {
            SerializationFormat.XML.write(data, namespaces, testfile);
            assertThat(FileUtils.contentEqualsIgnoreEOL(testfile, expectedResult, "UTF-8"), is(true));
        } finally {
            if (testfile.exists()) {
                testfile.delete();
            }
        }
    }
}
