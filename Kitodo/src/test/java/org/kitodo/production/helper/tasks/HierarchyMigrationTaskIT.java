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

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.config.ConfigCore;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.exceptions.CommandException;
import org.kitodo.exceptions.ProcessGenerationException;
import org.kitodo.production.services.ServiceManager;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class HierarchyMigrationTaskIT {

    private static Project project;

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();

        project = ServiceManager.getProjectService().getById(1);
        moveMetaFileAway(2, "meta_MigrationTaskIT.tmp");
        moveMetaFileAway(4, "meta_MigrationTaskIT_4.tmp");
        createTestMetafile();
        createTestMetaAnchorfile();
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
        cleanUp();
    }

    @Test
    public void testHierarchyMigration() throws DAOException, ProcessGenerationException, CommandException, DataException, IOException {
        HierarchyMigrationTask hierarchyMigrationTask = new HierarchyMigrationTask(Arrays.asList(project));
        hierarchyMigrationTask.migrate(ServiceManager.getProcessService().getById(2));
        Assert.assertTrue("Tasks should have been removed", ServiceManager.getProcessService().getById(4).getTasks().isEmpty());
        Assert.assertEquals("JahrdeDeG_404810993", ServiceManager.getProcessService().getById(4).getTitle());
    }

    private static void createTestMetaAnchorfile() throws Exception {
        List<String> lines = Arrays.asList("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<mets:mets xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:kitodo=\"http://meta.kitodo.org/v1/\" xmlns:mets=\"http://www.loc.gov/METS/\">\n" +
                "    <mets:metsHdr CREATEDATE=\"2019-09-11T05:02:04\">\n" +
                "        <mets:agent ROLE=\"CREATOR\" TYPE=\"OTHER\" OTHERTYPE=\"SOFTWARE\">\n" +
                "            <mets:name>Kitodo - kitodo-ugh-2.1.3-kitodo-ugh-2.1.1-11-g4b06eaa - 30−July−2019</mets:name>\n" +
                "            <mets:note>Kitodo</mets:note>\n" +
                "            <mets:note>Converted by Kitodo - Data Editor - 3.2.1-SNAPSHOT (2021-03-29T07:53:32Z)</mets:note>\n" +
                "        </mets:agent>\n" +
                "        <mets:metsDocumentID></mets:metsDocumentID>\n" +
                "    </mets:metsHdr>\n" +
                "    <mets:dmdSec ID=\"DMDLOG_0000\">\n" +
                "        <mets:mdWrap MDTYPE=\"OTHER\" OTHERMDTYPE=\"KITODO\">\n" +
                "            <mets:xmlData>\n" +
                "                <kitodo:kitodo version=\"1.0\">\n" +
                "                    <kitodo:metadata name=\"CatalogIDDigital\">404810993</kitodo:metadata>\n" +
                "                    <kitodo:metadata name=\"CatalogIDSource\">040091791</kitodo:metadata>\n" +
                "                    <kitodo:metadata name=\"PublicationRun\">1.1889/90 - 12.1900/01; mehr nicht digitalisiert</kitodo:metadata>\n" +
                "                    <kitodo:metadata name=\"singleDigCollection\">LDP: SLUB</kitodo:metadata>\n" +
                "                    <kitodo:metadata name=\"singleDigCollection\">Saxonica</kitodo:metadata>\n" +
                "                    <kitodo:metadata name=\"singleDigCollection\">Vergriffene Werke</kitodo:metadata>\n" +
                "                    <kitodo:metadata name=\"TSL_ATS\">JahrdeDeG</kitodo:metadata>\n" +
                "                    <kitodo:metadata name=\"CatalogIDPeriodicalDB\">2768676-0</kitodo:metadata>\n" +
                "                    <kitodo:metadata name=\"TitleDocMain\">Jahresbericht der Deutschen Gerberschule zu Freiberg in Sachsen</kitodo:metadata>\n" +
                "                    <kitodo:metadata name=\"TitleDocMainShort\">Jahresbericht der Deutschen Gerberschule zu Freiberg in Sachsen</kitodo:metadata>\n" +
                "                    <kitodo:metadata name=\"DocLanguage\">ger</kitodo:metadata>\n" +
                "                    <kitodo:metadata name=\"PlaceOfPublication\">Freiberg</kitodo:metadata>\n" +
                "                    <kitodo:metadata name=\"slub_Finance\">LDP Sachsen</kitodo:metadata>\n" +
                "                    <kitodo:metadata name=\"docType\">MultiVolumeWork</kitodo:metadata>\n" +
                "                </kitodo:kitodo>\n" +
                "            </mets:xmlData>\n" +
                "        </mets:mdWrap>\n" +
                "    </mets:dmdSec>\n" +
                "    <mets:structMap TYPE=\"LOGICAL\">\n" +
                "        <mets:div ID=\"LOG_0000\" DMDID=\"DMDLOG_0000\" TYPE=\"MultiVolumeWork\" ORDERLABEL=\"Jahresbericht der Deutschen Gerberschule zu Freiberg in Sachsen\" LABEL=\"Jahresbericht der Deutschen Gerberschule zu Freiberg in Sachsen\">\n" +
                "            <mets:div ID=\"LOG_0001\" TYPE=\"PeriodicalVolume\">\n" +
                "                <mets:mptr xlink:href=\"\" LOCTYPE=\"URL\"/>\n" +
                "            </mets:div>\n" +
                "        </mets:div>\n" +
                "    </mets:structMap>\n" +
                "</mets:mets>\n");
        File processHome = new File(ConfigCore.getKitodoDataDirectory(), "2");
        FileUtils.writeLines(new File(processHome, "meta_anchor.xml"), "UTF-8", lines);
    }

    private static void createTestMetafile() throws Exception {
        List<String> lines = Arrays.asList("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<mets:mets xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:kitodo=\"http://meta.kitodo.org/v1/\" xmlns:mets=\"http://www.loc.gov/METS/\">\n" +
                "    <mets:metsHdr CREATEDATE=\"2019-09-11T05:02:04\">\n" +
                "        <mets:agent ROLE=\"CREATOR\" TYPE=\"OTHER\" OTHERTYPE=\"SOFTWARE\">\n" +
                "            <mets:name>Kitodo - kitodo-ugh-2.1.3-kitodo-ugh-2.1.1-11-g4b06eaa - 30−July−2019</mets:name>\n" +
                "            <mets:note>Kitodo</mets:note>\n" +
                "            <mets:note>Converted by Kitodo - Data Editor - 3.2.1-SNAPSHOT (2021-03-29T07:53:32Z)</mets:note>\n" +
                "        </mets:agent>\n" +
                "        <mets:metsDocumentID></mets:metsDocumentID>\n" +
                "    </mets:metsHdr>\n" +
                "    <mets:dmdSec ID=\"DMDLOG_0001\">\n" +
                "        <mets:mdWrap MDTYPE=\"OTHER\" OTHERMDTYPE=\"KITODO\">\n" +
                "            <mets:xmlData>\n" +
                "                <kitodo:kitodo version=\"1.0\">\n" +
                "                    <kitodo:metadata name=\"CatalogIDDigital\">404810993-19130000</kitodo:metadata>\n" +
                "                    <kitodo:metadata name=\"CatalogIDDigitalAnchor\">404810993</kitodo:metadata>\n" +
                "                    <kitodo:metadata name=\"CurrentNo\">25.1913/14</kitodo:metadata>\n" +
                "                    <kitodo:metadata name=\"CurrentNoSorting\">19130000</kitodo:metadata>\n" +
                "                    <kitodo:metadata name=\"PublicationYear\">1914</kitodo:metadata>\n" +
                "                    <kitodo:metadata name=\"LegalNoteAndTermsOfUse\">VW1.0</kitodo:metadata>\n" +
                "                    <kitodo:metadata name=\"singleDigCollection\">LDP: SLUB</kitodo:metadata>\n" +
                "                    <kitodo:metadata name=\"singleDigCollection\">Saxonica</kitodo:metadata>\n" +
                "                    <kitodo:metadata name=\"singleDigCollection\">Vergriffene Werke</kitodo:metadata>\n" +
                "                    <kitodo:metadata name=\"slub_Finance\">LDP Sachsen</kitodo:metadata>\n" +
                "                    <kitodo:metadata name=\"shelfmarksource\">Hist.Sax.H.1076.s-25.1913/14</kitodo:metadata>\n" +
                "                    <kitodo:metadata name=\"slub_script\">Antiqua</kitodo:metadata>\n" +
                "                    <kitodo:metadata name=\"DocLanguage\">ger</kitodo:metadata>\n" +
                "                    <kitodo:metadata name=\"docType\">PeriodicalVolume</kitodo:metadata>\n" +
                "                </kitodo:kitodo>\n" +
                "            </mets:xmlData>\n" +
                "        </mets:mdWrap>\n" +
                "    </mets:dmdSec>\n" +
                "    <mets:dmdSec ID=\"DMDLOG_0002\">\n" +
                "        <mets:mdWrap MDTYPE=\"OTHER\" OTHERMDTYPE=\"KITODO\">\n" +
                "            <mets:xmlData>\n" +
                "                <kitodo:kitodo version=\"1.0\">\n" +
                "                    <kitodo:metadata name=\"TitleDocMain\">Schulnachrichten</kitodo:metadata>\n" +
                "                </kitodo:kitodo>\n" +
                "            </mets:xmlData>\n" +
                "        </mets:mdWrap>\n" +
                "    </mets:dmdSec>\n" +
                "    <mets:dmdSec ID=\"DMDLOG_0003\">\n" +
                "        <mets:mdWrap MDTYPE=\"OTHER\" OTHERMDTYPE=\"KITODO\">\n" +
                "            <mets:xmlData>\n" +
                "                <kitodo:kitodo version=\"1.0\">\n" +
                "                    <kitodo:metadata name=\"TitleDocMain\">A. Geschichtliches</kitodo:metadata>\n" +
                "                </kitodo:kitodo>\n" +
                "            </mets:xmlData>\n" +
                "        </mets:mdWrap>\n" +
                "    </mets:dmdSec>\n" +
                "    <mets:dmdSec ID=\"DMDLOG_0004\">\n" +
                "        <mets:mdWrap MDTYPE=\"OTHER\" OTHERMDTYPE=\"KITODO\">\n" +
                "            <mets:xmlData>\n" +
                "                <kitodo:kitodo version=\"1.0\">\n" +
                "                    <kitodo:metadata name=\"TitleDocMain\">B. Unterricht</kitodo:metadata>\n" +
                "                </kitodo:kitodo>\n" +
                "            </mets:xmlData>\n" +
                "        </mets:mdWrap>\n" +
                "    </mets:dmdSec>\n" +
                "    <mets:dmdSec ID=\"DMDLOG_0005\">\n" +
                "        <mets:mdWrap MDTYPE=\"OTHER\" OTHERMDTYPE=\"KITODO\">\n" +
                "            <mets:xmlData>\n" +
                "                <kitodo:kitodo version=\"1.0\">\n" +
                "                    <kitodo:metadata name=\"TitleDocMain\">C. Unterstützungen</kitodo:metadata>\n" +
                "                </kitodo:kitodo>\n" +
                "            </mets:xmlData>\n" +
                "        </mets:mdWrap>\n" +
                "    </mets:dmdSec>\n" +
                "    <mets:dmdSec ID=\"DMDLOG_0006\">\n" +
                "        <mets:mdWrap MDTYPE=\"OTHER\" OTHERMDTYPE=\"KITODO\">\n" +
                "            <mets:xmlData>\n" +
                "                <kitodo:kitodo version=\"1.0\">\n" +
                "                    <kitodo:metadata name=\"TitleDocMain\">D. Statistisches</kitodo:metadata>\n" +
                "                </kitodo:kitodo>\n" +
                "            </mets:xmlData>\n" +
                "        </mets:mdWrap>\n" +
                "    </mets:dmdSec>\n" +
                "    <mets:dmdSec ID=\"DMDLOG_0007\">\n" +
                "        <mets:mdWrap MDTYPE=\"OTHER\" OTHERMDTYPE=\"KITODO\">\n" +
                "            <mets:xmlData>\n" +
                "                <kitodo:kitodo version=\"1.0\">\n" +
                "                    <kitodo:metadata name=\"TitleDocMain\">Schülerverzeichnis des Schuljahres 1913-1914</kitodo:metadata>\n" +
                "                </kitodo:kitodo>\n" +
                "            </mets:xmlData>\n" +
                "        </mets:mdWrap>\n" +
                "    </mets:dmdSec>\n" +
                "    <mets:dmdSec ID=\"DMDLOG_0008\">\n" +
                "        <mets:mdWrap MDTYPE=\"OTHER\" OTHERMDTYPE=\"KITODO\">\n" +
                "            <mets:xmlData>\n" +
                "                <kitodo:kitodo version=\"1.0\">\n" +
                "                    <kitodo:metadata name=\"TitleDocMain\">Ergänzungen zu der anläßlich der Feier des 25jähr. Bestehens der Deutschen Gerberschule herausgegebenen Festschrift</kitodo:metadata>\n" +
                "                    <kitodo:metadata name=\"TitleDocMainShort\">Ergänzungen zu der anläßlich der Feier des 25jähr. Bestehens der Deutschen Gerberschule herausgegebenen Festschrift</kitodo:metadata>\n" +
                "                    <kitodo:metadataGroup name=\"Person\">\n" +
                "                        <kitodo:metadata name=\"FirstName\">F. H.</kitodo:metadata>\n" +
                "                        <kitodo:metadata name=\"LastName\">Haenlein</kitodo:metadata>\n" +
                "                        <kitodo:metadata name=\"DisplayForm\">Haenlein, F. H.</kitodo:metadata>\n" +
                "                        <kitodo:metadata name=\"RoleCode\">aut</kitodo:metadata>\n" +
                "                    </kitodo:metadataGroup>\n" +
                "                </kitodo:kitodo>\n" +
                "            </mets:xmlData>\n" +
                "        </mets:mdWrap>\n" +
                "    </mets:dmdSec>\n" +
                "    <mets:dmdSec ID=\"DMDPHYS_0000\">\n" +
                "        <mets:mdWrap MDTYPE=\"OTHER\" OTHERMDTYPE=\"KITODO\">\n" +
                "            <mets:xmlData>\n" +
                "                <kitodo:kitodo version=\"1.0\">\n" +
                "                    <kitodo:metadata name=\"pathimagefiles\">file:///home/goobi/work/daten/188237/images/JahrdeDeG_404810993-19130000_tif</kitodo:metadata>\n" +
                "                </kitodo:kitodo>\n" +
                "            </mets:xmlData>\n" +
                "        </mets:mdWrap>\n" +
                "    </mets:dmdSec>\n" +
                "    <mets:fileSec>\n" +
                "        <mets:fileGrp USE=\"LOCAL\">\n" +
                "            <mets:file ID=\"FILE_0001\" MIMETYPE=\"image/tiff\">\n" +
                "                <mets:FLocat xlink:href=\"file:///home/goobi/work/daten/188237/images/scans_tif/00000001.tif\" LOCTYPE=\"URL\"/>\n" +
                "            </mets:file>\n" +
                "        </mets:fileGrp>\n" +
                "    </mets:fileSec>\n" +
                "    <mets:structMap TYPE=\"LOGICAL\">\n" +
                "        <mets:div ID=\"LOG_0002\" TYPE=\"Periodical\">\n" +
                "            <mets:mptr xlink:href=\"\" LOCTYPE=\"URL\"/>\n" +
                "            <mets:div ID=\"LOG_0003\" DMDID=\"DMDLOG_0001\" TYPE=\"PeriodicalVolume\" ORDER=\"19130000\">\n" +
                "                <mets:div ID=\"LOG_0004\" TYPE=\"TitlePage\"/>\n" +
                "                <mets:div ID=\"LOG_0005\" DMDID=\"DMDLOG_0002\" TYPE=\"OtherDocStrct\">\n" +
                "                    <mets:div ID=\"LOG_0006\" DMDID=\"DMDLOG_0003\" TYPE=\"OtherDocStrct\"/>\n" +
                "                    <mets:div ID=\"LOG_0007\" DMDID=\"DMDLOG_0004\" TYPE=\"OtherDocStrct\"/>\n" +
                "                    <mets:div ID=\"LOG_0008\" DMDID=\"DMDLOG_0005\" TYPE=\"OtherDocStrct\"/>\n" +
                "                    <mets:div ID=\"LOG_0009\" DMDID=\"DMDLOG_0006\" TYPE=\"OtherDocStrct\"/>\n" +
                "                    <mets:div ID=\"LOG_0010\" DMDID=\"DMDLOG_0007\" TYPE=\"OtherDocStrct\"/>\n" +
                "                </mets:div>\n" +
                "                <mets:div ID=\"LOG_0011\" DMDID=\"DMDLOG_0008\" TYPE=\"Article\"/>\n" +
                "            </mets:div>\n" +
                "        </mets:div>\n" +
                "    </mets:structMap>\n" +
                "    <mets:structMap TYPE=\"PHYSICAL\">\n" +
                "        <mets:div ID=\"PHYS_0000\" DMDID=\"DMDPHYS_0000\" TYPE=\"BoundBook\">\n" +
                "            <mets:div ID=\"PHYS_0001\" TYPE=\"page\" ORDER=\"1\" ORDERLABEL=\"uncounted\">\n" +
                "                <mets:fptr FILEID=\"FILE_0001\"/>\n" +
                "            </mets:div>\n" +
                "        </mets:div>\n" +
                "    </mets:structMap>\n" +
                "    <mets:structLink>\n" +
                "        <mets:smLink xlink:to=\"PHYS_0001\" xlink:from=\"LOG_0003\"/>\n" +
                "    </mets:structLink>\n" +
                "</mets:mets>\n");
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
