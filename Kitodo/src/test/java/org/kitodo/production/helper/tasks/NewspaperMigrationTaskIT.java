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
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.TreeDeleter;
import org.kitodo.api.dataformat.IncludedStructuralElement;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.beans.User;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.BatchService;
import org.kitodo.production.services.data.ProcessService;

public class NewspaperMigrationTaskIT {

    private static final File METADATA_DIRECTORY = new File("src/test/resources/metadata");
    private static final File ISSUE_ONE_PROCESS_DIR = new File(METADATA_DIRECTORY, "1");
    private static final File ISSUE_TWO_PROCESS_DIR = new File(METADATA_DIRECTORY, "2");
    private static final File ORIGINAL_METADATA_TEMPORARY_LOCATION = new File("metadata.orig");

    private static final BatchService batchService = ServiceManager.getBatchService();
    private static final ProcessService processService = ServiceManager.getProcessService();

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        moveOriginMetadataDirectoryAside();
        createMetadataDirectoryForNewspaperMigrationTest();
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        addNewspaperDatabase();
        User user = ServiceManager.getUserService().getById(1);
        SecurityTestUtils.addUserDataToSecurityContext(user, 1);
    }

    private static void addNewspaperDatabase() throws Exception {
        Process processOne = processService.getById(1);
        Ruleset ruleset = processOne.getRuleset();
        ruleset.setFile("ruleset_newspaper-migration-test.xml");
        ServiceManager.getRulesetService().save(ruleset);
        Process processTwo = processService.getById(2);
        Batch newspaperBatch = new Batch();
        newspaperBatch.setTitle("Newspaper migration test");
        newspaperBatch.getProcesses().add(processOne);
        newspaperBatch.getProcesses().add(processTwo);
        ServiceManager.getBatchService().save(newspaperBatch);
        processOne.setTitle("NewsMiTe_18500312");
        processOne.getBatches().add(newspaperBatch);
        processService.save(processOne);
        processTwo.setTitle("NewsMiTe_18501105");
        processTwo.getBatches().add(newspaperBatch);
        processService.save(processTwo);
    }

    private static void moveOriginMetadataDirectoryAside() throws Exception {
        if (ORIGINAL_METADATA_TEMPORARY_LOCATION.exists()) {
            TreeDeleter.deltree(METADATA_DIRECTORY);
        } else {
            METADATA_DIRECTORY.renameTo(ORIGINAL_METADATA_TEMPORARY_LOCATION);
        }
    }

    private static void createMetadataDirectoryForNewspaperMigrationTest() throws Exception {
        ISSUE_ONE_PROCESS_DIR.mkdirs();
        createAnchorFile(ISSUE_ONE_PROCESS_DIR);
        createYearFile(ISSUE_ONE_PROCESS_DIR, "03", "03-12");
        createMetaFile(ISSUE_ONE_PROCESS_DIR, "03-12", "0312");

        ISSUE_TWO_PROCESS_DIR.mkdirs();
        createAnchorFile(ISSUE_TWO_PROCESS_DIR);
        createYearFile(ISSUE_TWO_PROCESS_DIR, "11", "11-05");
        createMetaFile(ISSUE_TWO_PROCESS_DIR, "11", "1105");
    }

    private static void createAnchorFile(File processHome) throws Exception {
        List<String> lines = Arrays.asList("<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
            "<mets:mets xsi:schemaLocation=\"http://www.loc.gov/mods/v3 "
                    + "http://www.loc.gov/standards/mods/mods.xsd http://www.loc.gov/METS/ http://www.loc.gov/standards/mets/mets.xsd "
                    + "info:lc/xmlns/premis-v2 http://www.loc.gov/standards/premis/v2/premis-v2-0.xsd http://www.loc.gov/mix/v10 "
                    + "http://www.loc.gov/standards/mix/mix10/mix10.xsd\" xmlns:mets=\"http://www.loc.gov/METS/\" "
                    + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                    + "<mets:metsHdr CREATEDATE=\"2019-11-25T11:20:06\">"
                    + "<mets:agent OTHERTYPE=\"SOFTWARE\" ROLE=\"CREATOR\" TYPE=\"OTHER\">"
                    + "<mets:name>Kitodo - kitodo-ugh-2.1.3-kitodo-ugh-2.1.1-11-g4b06eaa - 30−July−2019</mets:name>"
                    + "<mets:note>Kitodo</mets:note></mets:agent></mets:metsHdr>"
                    + "<mets:dmdSec ID=\"DMDLOG_0000\"><mets:mdWrap MDTYPE=\"MODS\"><mets:xmlData>"
                    + "<mods:mods xmlns:mods=\"http://www.loc.gov/mods/v3\"><mods:extension>"
                    + "<goobi:goobi xmlns:goobi=\"http://meta.goobi.org/v1.5.1/\">"
                    + "<goobi:metadata name=\"singleDigCollection\">Collection 2</goobi:metadata>"
                    + "<goobi:metadata name=\"TitleDocMain\">Newspaper migration test</goobi:metadata>"
                    + "<goobi:metadata name=\"TitleDocMainShort\">Newspaper migration test</goobi:metadata>"
                    + "<goobi:metadata name=\"TSL_ATS\">Newsmite</goobi:metadata></goobi:goobi></mods:extension></mods:mods>"
                    + "</mets:xmlData></mets:mdWrap></mets:dmdSec>"
                    + "<mets:dmdSec ID=\"DMDLOG_0001\"><mets:mdWrap MDTYPE=\"MODS\"><mets:xmlData>"
                    + "<mods:mods xmlns:mods=\"http://www.loc.gov/mods/v3\"><mods:extension>"
                    + "<goobi:goobi xmlns:goobi=\"http://meta.goobi.org/v1.5.1/\">"
                    + "<goobi:metadata name=\"TitleDocMain\">Newspaper migration test</goobi:metadata>"
                    + "<goobi:metadata name=\"TitleDocMainShort\">1850</goobi:metadata></goobi:goobi></mods:extension></mods:mods>"
                    + "</mets:xmlData></mets:mdWrap></mets:dmdSec>"
                    + "<mets:structMap TYPE=\"LOGICAL\"><mets:div DMDID=\"DMDLOG_0000\" ID=\"LOG_0000\" TYPE=\"Newspaper\">"
                    + "<mets:div DMDID=\"DMDLOG_0001\" ID=\"LOG_0001\" TYPE=\"NewspaperYear\">"
                    + "<mets:mptr LOCTYPE=\"URL\" xlink:href=\"\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"/>"
                    + "</mets:div></mets:div></mets:structMap></mets:mets>");
        FileUtils.writeLines(new File(processHome, "meta_anchor.xml"), "UTF-8", lines);
    }

    private static void createYearFile(File processHome, String... string) throws Exception {
        List<String> lines = Arrays.asList("<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
            "<mets:mets xsi:schemaLocation=\"http://www.loc.gov/mods/v3 http://www.loc.gov/standards/mods/mods.xsd "
                    + "http://www.loc.gov/METS/ http://www.loc.gov/standards/mets/mets.xsd info:lc/xmlns/premis-v2 "
                    + "http://www.loc.gov/standards/premis/v2/premis-v2-0.xsd http://www.loc.gov/mix/v10 "
                    + "http://www.loc.gov/standards/mix/mix10/mix10.xsd\" xmlns:mets=\"http://www.loc.gov/METS/\" "
                    + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                    + "<mets:metsHdr CREATEDATE=\"2019-11-25T11:20:06\">"
                    + "<mets:agent OTHERTYPE=\"SOFTWARE\" ROLE=\"CREATOR\" TYPE=\"OTHER\">"
                    + "<mets:name>Kitodo - kitodo-ugh-2.1.3-kitodo-ugh-2.1.1-11-g4b06eaa - 30−July−2019</mets:name>"
                    + "<mets:note>Kitodo</mets:note></mets:agent></mets:metsHdr>"
                    + "<mets:dmdSec ID=\"DMDLOG_0002\"><mets:mdWrap MDTYPE=\"MODS\"><mets:xmlData>"
                    + "<mods:mods xmlns:mods=\"http://www.loc.gov/mods/v3\"><mods:extension>"
                    + "<goobi:goobi xmlns:goobi=\"http://meta.goobi.org/v1.5.1/\">"
                    + "<goobi:metadata name=\"TitleDocMain\">Newspaper migration test</goobi:metadata>"
                    + "<goobi:metadata name=\"TitleDocMainShort\">Newspaper migration test</goobi:metadata>"
                    + "</goobi:goobi></mods:extension></mods:mods></mets:xmlData></mets:mdWrap></mets:dmdSec>"
                    + "<mets:dmdSec ID=\"DMDLOG_0003\"><mets:mdWrap MDTYPE=\"MODS\"><mets:xmlData>"
                    + "<mods:mods xmlns:mods=\"http://www.loc.gov/mods/v3\"><mods:extension>"
                    + "<goobi:goobi xmlns:goobi=\"http://meta.goobi.org/v1.5.1/\">"
                    + "<goobi:metadata name=\"singleDigCollection\">Collection 2</goobi:metadata>"
                    + "<goobi:metadata name=\"TitleDocMain\">Newspaper migration test</goobi:metadata>"
                    + "<goobi:metadata name=\"TitleDocMainShort\">1850</goobi:metadata></goobi:goobi>"
                    + "</mods:extension></mods:mods></mets:xmlData></mets:mdWrap></mets:dmdSec>"
                    + "<mets:dmdSec ID=\"DMDLOG_0004\"><mets:mdWrap MDTYPE=\"MODS\"><mets:xmlData>"
                    + "<mods:mods xmlns:mods=\"http://www.loc.gov/mods/v3\"><mods:extension>"
                    + "<goobi:goobi xmlns:goobi=\"http://meta.goobi.org/v1.5.1/\">"
                    + "<goobi:metadata name=\"TitleDocMainShort\">1850-" + string[0] + "</goobi:metadata>"
                    + "</goobi:goobi></mods:extension></mods:mods></mets:xmlData></mets:mdWrap></mets:dmdSec>"
                    + "<mets:dmdSec ID=\"DMDLOG_0005\"><mets:mdWrap MDTYPE=\"MODS\"><mets:xmlData>"
                    + "<mods:mods xmlns:mods=\"http://www.loc.gov/mods/v3\"><mods:extension>"
                    + "<goobi:goobi xmlns:goobi=\"http://meta.goobi.org/v1.5.1/\">"
                    + "<goobi:metadata name=\"TitleDocMainShort\">1850-" + string[1] + "</goobi:metadata>"
                    + "</goobi:goobi></mods:extension></mods:mods></mets:xmlData></mets:mdWrap></mets:dmdSec>"
                    + "<mets:structMap TYPE=\"LOGICAL\"><mets:div DMDID=\"DMDLOG_0002\" ID=\"LOG_0002\" TYPE=\"Newspaper\">"
                    + "<mets:mptr LOCTYPE=\"URL\" xlink:href=\"\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"/>"
                    + "<mets:div DMDID=\"DMDLOG_0003\" ID=\"LOG_0003\" TYPE=\"NewspaperYear\">"
                    + "<mets:div DMDID=\"DMDLOG_0004\" ID=\"LOG_0004\" TYPE=\"NewspaperMonth\">"
                    + "<mets:div DMDID=\"DMDLOG_0005\" ID=\"LOG_0005\" TYPE=\"NewspaperDay\">"
                    + "<mets:div ID=\"LOG_0006\" TYPE=\"PeriodicalIssue\">"
                    + "<mets:mptr LOCTYPE=\"URL\" xlink:href=\"\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"/>"
                    + "</mets:div></mets:div></mets:div></mets:div></mets:div></mets:structMap></mets:mets>");
        FileUtils.writeLines(new File(processHome, "meta_year.xml"), "UTF-8", lines);
    }

    private static void createMetaFile(File processHome, String... string) throws Exception {
        String file = SystemUtils.IS_OS_WINDOWS ? "file:/" : "file://";
        String prefix = file + processHome.getAbsolutePath() + File.separatorChar + "images" + File.separatorChar
                + "NewsMiTe_1850" + string[1];
        String media = prefix + "_media" + File.separatorChar;

        List<String> lines = Arrays.asList("<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
            "<mets:mets xsi:schemaLocation=\"http://www.loc.gov/mods/v3 http://www.loc.gov/standards/mods/mods.xsd "
                    + "http://www.loc.gov/METS/ http://www.loc.gov/standards/mets/mets.xsd info:lc/xmlns/premis-v2 "
                    + "http://www.loc.gov/standards/premis/v2/premis-v2-0.xsd http://www.loc.gov/mix/v10 "
                    + "http://www.loc.gov/standards/mix/mix10/mix10.xsd\" xmlns:mets=\"http://www.loc.gov/METS/\" "
                    + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                    + "<mets:metsHdr CREATEDATE=\"2019-11-25T11:20:06\">"
                    + "<mets:agent OTHERTYPE=\"SOFTWARE\" ROLE=\"CREATOR\" TYPE=\"OTHER\">"
                    + "<mets:name>Kitodo - kitodo-ugh-2.1.3-kitodo-ugh-2.1.1-11-g4b06eaa - 30−July−2019</mets:name>"
                    + "<!-- mets:note>Kitodo</mets:note --></mets:agent></mets:metsHdr>"
                    + "<mets:dmdSec ID=\"DMDLOG_0006\"><mets:mdWrap MDTYPE=\"MODS\"><mets:xmlData>"
                    + "<mods:mods xmlns:mods=\"http://www.loc.gov/mods/v3\"><mods:extension>"
                    + "<goobi:goobi xmlns:goobi=\"http://meta.goobi.org/v1.5.1/\">"
                    + "<goobi:metadata name=\"TitleDocMainShort\">1850-" + string[0] + "</goobi:metadata></goobi:goobi>"
                    + "</mods:extension></mods:mods></mets:xmlData></mets:mdWrap></mets:dmdSec>"
                    + "<mets:dmdSec ID=\"DMDPHYS_0000\"><mets:mdWrap MDTYPE=\"MODS\"><mets:xmlData>"
                    + "<mods:mods xmlns:mods=\"http://www.loc.gov/mods/v3\"><mods:extension>"
                    + "<goobi:goobi xmlns:goobi=\"http://meta.goobi.org/v1.5.1/\"><goobi:metadata name=\"shelfmarksource\">"
                    + "NewsMiTe</goobi:metadata><goobi:metadata name=\"pathimagefiles\">" + prefix
                    + "_tif</goobi:metadata>"
                    + "</goobi:goobi></mods:extension></mods:mods></mets:xmlData></mets:mdWrap></mets:dmdSec>"
                    + "<mets:fileSec><mets:fileGrp USE=\"LOCAL\">"
                    + "<mets:file ID=\"FILE_0001\" MIMETYPE=\"\"><mets:FLocat LOCTYPE=\"URL\" xlink:href=\"" + media
                    + "00000001.tif\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"/></mets:file>"
                    + "<mets:file ID=\"FILE_0002\" MIMETYPE=\"\"><mets:FLocat LOCTYPE=\"URL\" xlink:href=\"" + media
                    + "00000002.tif\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"/></mets:file>"
                    + "<mets:file ID=\"FILE_0003\" MIMETYPE=\"\"><mets:FLocat LOCTYPE=\"URL\" xlink:href=\"" + media
                    + "00000003.tif\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"/></mets:file>"
                    + "<mets:file ID=\"FILE_0004\" MIMETYPE=\"\"><mets:FLocat LOCTYPE=\"URL\" xlink:href=\"" + media
                    + "00000004.tif\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"/></mets:file>"
                    + "<mets:file ID=\"FILE_0005\" MIMETYPE=\"\"><mets:FLocat LOCTYPE=\"URL\" xlink:href=\"" + media
                    + "00000005.tif\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"/></mets:file>"
                    + "</mets:fileGrp></mets:fileSec>"
                    + "<mets:structMap TYPE=\"LOGICAL\"><mets:div ID=\"LOG_0007\" TYPE=\"Newspaper\">"
                    + "<mets:mptr LOCTYPE=\"URL\" xlink:href=\"\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"/>"
                    + "<mets:div ID=\"LOG_0008\" TYPE=\"NewspaperYear\">"
                    + "<mets:mptr LOCTYPE=\"URL\" xlink:href=\"\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"/>"
                    + "<mets:div ID=\"LOG_0009\" TYPE=\"NewspaperMonth\"><mets:div ID=\"LOG_0010\" TYPE=\"NewspaperDay\">"
                    + "<mets:div DMDID=\"DMDLOG_0006\" ID=\"LOG_0011\" TYPE=\"PeriodicalIssue\"/>"
                    + "</mets:div></mets:div></mets:div></mets:div></mets:structMap>"
                    + "<mets:structMap TYPE=\"PHYSICAL\"><mets:div DMDID=\"DMDPHYS_0000\" ID=\"PHYS_0000\" TYPE=\"BoundBook\">"
                    + "<mets:div ID=\"PHYS_0001\" ORDER=\"1\" ORDERLABEL=\"uncounted\" TYPE=\"page\">"
                    + "<mets:fptr FILEID=\"FILE_0001\"/></mets:div>"
                    + "<mets:div ID=\"PHYS_0002\" ORDER=\"2\" ORDERLABEL=\"uncounted\" TYPE=\"page\">"
                    + "<mets:fptr FILEID=\"FILE_0002\"/></mets:div>"
                    + "<mets:div ID=\"PHYS_0003\" ORDER=\"3\" ORDERLABEL=\"uncounted\" TYPE=\"page\">"
                    + "<mets:fptr FILEID=\"FILE_0003\"/></mets:div>"
                    + "<mets:div ID=\"PHYS_0004\" ORDER=\"4\" ORDERLABEL=\"uncounted\" TYPE=\"page\">"
                    + "<mets:fptr FILEID=\"FILE_0004\"/></mets:div>"
                    + "<mets:div ID=\"PHYS_0005\" ORDER=\"5\" ORDERLABEL=\"uncounted\" TYPE=\"page\">"
                    + "<mets:fptr FILEID=\"FILE_0005\"/></mets:div></mets:div></mets:structMap><mets:structLink>"
                    + "<mets:smLink xlink:to=\"PHYS_0001\" xlink:from=\"LOG_0011\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"/>"
                    + "<mets:smLink xlink:to=\"PHYS_0002\" xlink:from=\"LOG_0011\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"/>"
                    + "<mets:smLink xlink:to=\"PHYS_0003\" xlink:from=\"LOG_0011\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"/>"
                    + "<mets:smLink xlink:to=\"PHYS_0004\" xlink:from=\"LOG_0011\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"/>"
                    + "<mets:smLink xlink:to=\"PHYS_0005\" xlink:from=\"LOG_0011\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"/>"
                    + "</mets:structLink></mets:mets>");
        FileUtils.writeLines(new File(processHome, "meta.xml"), "UTF-8", lines);
    }

    @Test
    public void testNewspaperMigrationTask() throws Exception {
        NewspaperMigrationTask underTest = new NewspaperMigrationTask(batchService.findById(5));

        underTest.start();
        Assert.assertTrue("should be running", underTest.isAlive());
        underTest.join();
        Assert.assertFalse("should have finished", underTest.isAlive());

        Assert.assertEquals("should have completed", 100, underTest.getProgress());

        Process issueOne = processService.getById(1);
        Workpiece workpiece = ServiceManager.getMetsService()
                .loadWorkpiece(processService.getMetadataFileUri(issueOne));
        IncludedStructuralElement rootElement = workpiece.getRootElement();
        Assert.assertEquals("should have modified METS file", "NewspaperMonth", rootElement.getType());

        Assert.assertEquals("should have added date for month", "1850-03", rootElement.getOrderlabel());
        Assert.assertEquals("should have added date for day", "1850-03-12",
            rootElement.getChildren().get(0).getOrderlabel());

        Assert.assertEquals("should have created year process", 1, processService.findByTitle("NewsMiTe_1850").size());

        Assert.assertEquals("should have created overall process", 1, processService.findByTitle("NewsMiTe").size());

        Process newspaperProcess = processService.getById(4);
        Process yearProcess = processService.getById(5);
        Assert.assertTrue("should have added link from newspaper process to year process",
            newspaperProcess.getChildren().contains(yearProcess));

        List<Process> linksInYear = yearProcess.getChildren();
        Assert.assertTrue("should have added links from year process to issues",
            linksInYear.contains(issueOne) && linksInYear.contains(processService.getById(2)));
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
        restoreMetadataDirectoryContents();
        SecurityTestUtils.cleanSecurityContext();
    }

    private static void restoreMetadataDirectoryContents() throws Exception {
        if (ORIGINAL_METADATA_TEMPORARY_LOCATION.exists()) {
            if (METADATA_DIRECTORY.exists()) {
                TreeDeleter.deltree(METADATA_DIRECTORY);
            }
            ORIGINAL_METADATA_TEMPORARY_LOCATION.renameTo(METADATA_DIRECTORY);
        }
    }
}
