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
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.config.ConfigCore;
import org.kitodo.data.database.beans.Project;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ProcessService;
import org.kitodo.production.services.dataformat.MetsService;

public class MigrationTaskIT {

    private static final MetsService metsService = ServiceManager.getMetsService();
    private static final ProcessService processService = ServiceManager.getProcessService();
    private static Project project;

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();

        project = ServiceManager.getProjectService().getById(1);
        moveMetaFileAway(2, "meta_MigrationTaskIT.tmp");
        createTestMetafile(2);
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
        cleanUp();
    }

    @Test
    public void testMigrationTask() throws Exception {
        MigrationTask migrationTask = new MigrationTask(project);
        migrationTask.start();
        Assert.assertTrue(migrationTask.isAlive());
        migrationTask.join();
        Assert.assertFalse(migrationTask.isAlive());
        Assert.assertEquals(100, migrationTask.getProgress());
        Assert.assertNotNull("Process migration failed",
            metsService.loadWorkpiece(processService.getMetadataFileUri(processService.getById(2))));
    }

    private static void moveMetaFileAway(int recordNumber, String tempFileName) throws Exception {
        File processHome = new File(ConfigCore.getKitodoDataDirectory(), Integer.toString(recordNumber));
        new File(processHome, "meta.xml").renameTo(new File(processHome, tempFileName));
    }

    private static void createTestMetafile(int recordNumber) throws Exception {
        List<String> lines = Arrays.asList("<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
            "<mets:mets xsi:schemaLocation=\"http://www.loc.gov/mods/v3 "
                    + "http://www.loc.gov/standards/mods/mods.xsd http://www.loc.gov/METS/ http://www.loc.gov/standards/mets/mets.xsd "
                    + "info:lc/xmlns/premis-v2 http://www.loc.gov/standards/premis/v2/premis-v2-0.xsd http://www.loc.gov/mix/v10 "
                    + "http://www.loc.gov/standards/mix/mix10/mix10.xsd\" xmlns:mets=\"http://www.loc.gov/METS/\" "
                    + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                    + "<mets:metsHdr CREATEDATE=\"2018-09-14T07:45:17\">"
                    + "<mets:agent OTHERTYPE=\"SOFTWARE\" ROLE=\"CREATOR\" TYPE=\"OTHER\">"
                    + "<mets:name>Kitodo - kitodo-ugh-3.0-SNAPSHOT - 18-April-2018 13:20:13</mets:name>"
                    + "<mets:note>Kitodo</mets:note></mets:agent></mets:metsHdr>"
                    + "<mets:dmdSec ID=\"DMDLOG_0000\"><mets:mdWrap MDTYPE=\"MODS\"><mets:xmlData>"
                    + "<mods:mods xmlns:mods=\"http://www.loc.gov/mods/v3\"><mods:extension>"
                    + "<goobi:goobi xmlns:goobi=\"http://meta.goobi.org/v1.5.1/\">"
                    + "<goobi:metadata name=\"TitleDocMain\">Second process</goobi:metadata>"
                    + "<goobi:metadata name=\"TitleDocMainShort\">SecondMetaShort</goobi:metadata>"
                    + "<goobi:metadata name=\"TSL_ATS\">Proc</goobi:metadata>"
                    + "</goobi:goobi></mods:extension></mods:mods></mets:xmlData></mets:mdWrap></mets:dmdSec>"
                    + "<mets:dmdSec ID=\"DMDPHYS_0000\"><mets:mdWrap MDTYPE=\"MODS\"><mets:xmlData>"
                    + "<mods:mods xmlns:mods=\"http://www.loc.gov/mods/v3\">"
                    + "<mods:extension><goobi:goobi xmlns:goobi=\"http://meta.goobi.org/v1.5.1/\">"
                    + "<goobi:metadata name=\"pathimagefiles\">file:/2/images/Sec_Proc_tif</goobi:metadata>"
                    + "</goobi:goobi></mods:extension></mods:mods></mets:xmlData></mets:mdWrap></mets:dmdSec>"
                    + "<mets:structMap TYPE=\"LOGICAL\">"
                    + "<mets:div DMDID=\"DMDLOG_0000\" ID=\"LOG_0000\" TYPE=\"Monograph\"/>" + "</mets:structMap>"
                    + "<mets:structMap TYPE=\"PHYSICAL\">"
                    + "<mets:div DMDID=\"DMDPHYS_0000\" ID=\"PHYS_0000\" TYPE=\"physSequence\"/>" + "</mets:structMap>"
                    + "<mets:structLink>"
                    + "<mets:smLink xlink:to=\"PHYS_0000\" xlink:from=\"LOG_0000\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"/>"
                    + "</mets:structLink></mets:mets>");
        File processHome = new File(ConfigCore.getKitodoDataDirectory(), "2");
        FileUtils.writeLines(new File(processHome, "meta.xml"), "UTF-8", lines);
    }

    private static void cleanUp() throws Exception {
        File processHome = new File(ConfigCore.getKitodoDataDirectory(), "2");
        File originalMeta = new File(processHome, "meta_MigrationTaskIT.tmp");
        if (originalMeta.exists()) {
            File metaFile = new File(processHome, "meta.xml");
            metaFile.delete();
            originalMeta.renameTo(metaFile);
        }
    }
}
