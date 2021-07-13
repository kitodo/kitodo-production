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

package org.kitodo.production.helper.tasks;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.ExecutionPermission;
import org.kitodo.MockDatabase;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.exceptions.CommandException;
import org.kitodo.exceptions.ProcessGenerationException;
import org.kitodo.production.services.ServiceManager;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class HierarchyMigrationTaskIT {

    private static Project project;
    private static final File script = new File(ConfigCore.getParameter(ParameterCore.SCRIPT_CREATE_DIR_META));

    /**
     * prepares database.
     */
    @BeforeClass
    public static void prepareDatabase() throws Exception {
        if (!SystemUtils.IS_OS_WINDOWS) {
            ExecutionPermission.setExecutePermission(script);
        }

        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();

        project = ServiceManager.getProjectService().getById(1);
        moveMetaFileAway(2, "meta_MigrationTaskIT.tmp");
        moveMetaFileAway(4, "meta_MigrationTaskIT_4.tmp");
        createTestMetafile();
        createTestMetaAnchorfile();
    }

    /**
     * cleans database.
     */
    @AfterClass
    public static void cleanDatabase() throws Exception {
        if (!SystemUtils.IS_OS_WINDOWS) {
            ExecutionPermission.setNoExecutePermission(script);
        }

        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
        cleanUp();
    }

    /**
     * Tests migration of hierarchies.
     */
    @Test
    public void testHierarchyMigration() throws DAOException, ProcessGenerationException, CommandException,
            IOException, SAXException, ParserConfigurationException {
        HierarchyMigrationTask hierarchyMigrationTask = new HierarchyMigrationTask(Collections.singletonList(project));
        hierarchyMigrationTask.migrate(ServiceManager.getProcessService().getById(2));
        Assert.assertTrue("Tasks should have been removed",
            ServiceManager.getProcessService().getById(4).getTasks().isEmpty());
        Assert.assertEquals("JahrdeDeG_404810993", ServiceManager.getProcessService().getById(4).getTitle());
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new File("src/test/resources/metadata/4/meta.xml"));
        String documentId = document.getElementsByTagName("mets:metsDocumentID").item(0).getTextContent();
        Assert.assertEquals("DocumentId not set", "4", documentId);
    }

    private static void createTestMetaAnchorfile() throws Exception {
        List<String> lines = Collections.singletonList("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                + "<mets:mets xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:kitodo=\"http://meta.kitodo.org/v1/\" xmlns:mets=\"http://www.loc.gov/METS/\">\n"
                + "    <mets:metsHdr CREATEDATE=\"2019-09-11T05:02:04\">\n"
                + "        <mets:agent ROLE=\"CREATOR\" TYPE=\"OTHER\" OTHERTYPE=\"SOFTWARE\">\n"
                + "            <mets:name>Kitodo - kitodo-ugh-2.1.3-kitodo-ugh-2.1.1-11-g4b06eaa - 30−July−2019</mets:name>\n"
                + "            <mets:note>Kitodo</mets:note>\n"
                + "            <mets:note>Converted by Kitodo - Data Editor - 3.2.1-SNAPSHOT (2021-03-29T07:53:32Z)</mets:note>\n"
                + "        </mets:agent>\n" + "        <mets:metsDocumentID></mets:metsDocumentID>\n"
                + "    </mets:metsHdr>\n" + "    <mets:dmdSec ID=\"DMDLOG_0000\">\n"
                + "        <mets:mdWrap MDTYPE=\"OTHER\" OTHERMDTYPE=\"KITODO\">\n" + "            <mets:xmlData>\n"
                + "                <kitodo:kitodo version=\"1.0\">\n"
                + "                    <kitodo:metadata name=\"CatalogIDDigital\">404810993</kitodo:metadata>\n"
                + "                    <kitodo:metadata name=\"TSL_ATS\">JahrdeDeG</kitodo:metadata>\n"
                + "                    <kitodo:metadata name=\"PlaceOfPublication\">Freiberg</kitodo:metadata>\n"
                + "                    <kitodo:metadata name=\"slub_Finance\">LDP Sachsen</kitodo:metadata>\n"
                + "                    <kitodo:metadata name=\"docType\">MultiVolumeWork</kitodo:metadata>\n"
                + "                </kitodo:kitodo>\n" + "            </mets:xmlData>\n" + "        </mets:mdWrap>\n"
                + "    </mets:dmdSec>\n" + "    <mets:structMap TYPE=\"LOGICAL\">\n"
                + "        <mets:div ID=\"LOG_0000\" DMDID=\"DMDLOG_0000\" TYPE=\"MultiVolumeWork\" ORDERLABEL=\"Jahresbericht der Deutschen Gerberschule zu Freiberg in Sachsen\" LABEL=\"Jahresbericht der Deutschen Gerberschule zu Freiberg in Sachsen\">\n"
                + "            <mets:div ID=\"LOG_0001\" TYPE=\"PeriodicalVolume\">\n"
                + "                <mets:mptr xlink:href=\"\" LOCTYPE=\"URL\"/>\n" + "            </mets:div>\n"
                + "        </mets:div>\n" + "    </mets:structMap>\n" + "</mets:mets>\n");
        File processHome = new File(ConfigCore.getKitodoDataDirectory(), "2");
        FileUtils.writeLines(new File(processHome, "meta_anchor.xml"), "UTF-8", lines);
    }

    private static void createTestMetafile() throws Exception {
        List<String> lines = Collections.singletonList("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                + "<mets:mets xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:kitodo=\"http://meta.kitodo.org/v1/\" xmlns:mets=\"http://www.loc.gov/METS/\">\n"
                + "    <mets:metsHdr CREATEDATE=\"2019-09-11T05:02:04\">\n"
                + "        <mets:agent ROLE=\"CREATOR\" TYPE=\"OTHER\" OTHERTYPE=\"SOFTWARE\">\n"
                + "            <mets:name>Kitodo - kitodo-ugh-2.1.3-kitodo-ugh-2.1.1-11-g4b06eaa - 30−July−2019</mets:name>\n"
                + "            <mets:note>Kitodo</mets:note>\n"
                + "            <mets:note>Converted by Kitodo - Data Editor - 3.2.1-SNAPSHOT (2021-03-29T07:53:32Z)</mets:note>\n"
                + "        </mets:agent>\n" + "        <mets:metsDocumentID></mets:metsDocumentID>\n"
                + "    </mets:metsHdr>\n" + "    <mets:dmdSec ID=\"DMDLOG_0001\">\n"
                + "        <mets:mdWrap MDTYPE=\"OTHER\" OTHERMDTYPE=\"KITODO\">\n" + "            <mets:xmlData>\n"
                + "                <kitodo:kitodo version=\"1.0\">\n"
                + "                    <kitodo:metadata name=\"CatalogIDDigital\">404810993-19130000</kitodo:metadata>\n"
                + "                    <kitodo:metadata name=\"CatalogIDDigitalAnchor\">404810993</kitodo:metadata>\n"
                + "                    <kitodo:metadata name=\"CurrentNo\">25.1913/14</kitodo:metadata>\n"
                + "                    <kitodo:metadata name=\"CurrentNoSorting\">19130000</kitodo:metadata>\n"
                + "                    <kitodo:metadata name=\"slub_script\">Antiqua</kitodo:metadata>\n"
                + "                    <kitodo:metadata name=\"DocLanguage\">ger</kitodo:metadata>\n"
                + "                    <kitodo:metadata name=\"docType\">PeriodicalVolume</kitodo:metadata>\n"
                + "                </kitodo:kitodo>\n" + "            </mets:xmlData>\n" + "        </mets:mdWrap>\n"
                + "    </mets:dmdSec>\n" + "    <mets:fileSec>\n" + "        <mets:fileGrp USE=\"LOCAL\">\n"
                + "            <mets:file ID=\"FILE_0001\" MIMETYPE=\"image/tiff\">\n"
                + "                <mets:FLocat xlink:href=\"file:///home/goobi/work/daten/188237/images/scans_tif/00000001.tif\" LOCTYPE=\"URL\"/>\n"
                + "            </mets:file>\n" + "        </mets:fileGrp>\n" + "    </mets:fileSec>\n"
                + "    <mets:structMap TYPE=\"LOGICAL\">\n" + "        <mets:div ID=\"LOG_0002\" TYPE=\"Periodical\">\n"
                + "            <mets:mptr xlink:href=\"\" LOCTYPE=\"URL\"/>\n"
                + "            <mets:div ID=\"LOG_0003\" DMDID=\"DMDLOG_0001\" TYPE=\"PeriodicalVolume\" ORDER=\"19130000\">\n"
                + "                <mets:div ID=\"LOG_0004\" TYPE=\"TitlePage\"/>\n"
                + "                <mets:div ID=\"LOG_0005\" DMDID=\"DMDLOG_0002\" TYPE=\"OtherDocStrct\">\n"
                + "                    <mets:div ID=\"LOG_0006\" DMDID=\"DMDLOG_0003\" TYPE=\"OtherDocStrct\"/>\n"
                + "                    <mets:div ID=\"LOG_0007\" DMDID=\"DMDLOG_0004\" TYPE=\"OtherDocStrct\"/>\n"
                + "                    <mets:div ID=\"LOG_0008\" DMDID=\"DMDLOG_0005\" TYPE=\"OtherDocStrct\"/>\n"
                + "                    <mets:div ID=\"LOG_0009\" DMDID=\"DMDLOG_0006\" TYPE=\"OtherDocStrct\"/>\n"
                + "                    <mets:div ID=\"LOG_0010\" DMDID=\"DMDLOG_0007\" TYPE=\"OtherDocStrct\"/>\n"
                + "                </mets:div>\n"
                + "                <mets:div ID=\"LOG_0011\" DMDID=\"DMDLOG_0008\" TYPE=\"Article\"/>\n"
                + "            </mets:div>\n" + "        </mets:div>\n" + "    </mets:structMap>\n" + "</mets:mets>\n");
        File processHome = new File(ConfigCore.getKitodoDataDirectory(), "2");
        FileUtils.writeLines(new File(processHome, "meta.xml"), "UTF-8", lines);
    }

    private static void cleanUp() {
        File processHome = new File(ConfigCore.getKitodoDataDirectory(), "2");
        File originalMeta = new File(processHome, "meta_MigrationTaskIT.tmp");
        if (originalMeta.exists()) {
            File metaFile = new File(processHome, "meta.xml");
            metaFile.delete();
            originalMeta.renameTo(metaFile);
        }

        processHome = new File(ConfigCore.getKitodoDataDirectory(), "4");
        originalMeta = new File(processHome, "meta_MigrationTaskIT_4.tmp");
        if (originalMeta.exists()) {
            File metaFile = new File(processHome, "meta.xml");
            metaFile.delete();
            originalMeta.renameTo(metaFile);
        }
        new File("src/test/resources/metadata/2/meta_anchor.migrated").delete();
    }

    private static void moveMetaFileAway(int recordNumber, String tempFileName) throws Exception {
        File processHome = new File(ConfigCore.getKitodoDataDirectory(), Integer.toString(recordNumber));
        new File(processHome, "meta.xml").renameTo(new File(processHome, tempFileName));
    }

}
