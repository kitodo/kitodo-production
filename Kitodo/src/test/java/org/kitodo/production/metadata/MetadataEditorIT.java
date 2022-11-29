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

package org.kitodo.production.metadata;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.api.Metadata;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.User;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ProcessService;

public class MetadataEditorIT {
    private static final ProcessService processService = ServiceManager.getProcessService();

    private static final String firstProcess = "First process";

    /**
     * Is running before the class runs.
     */
    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        MockDatabase.insertProcessesForHierarchyTests();
        MockDatabase.setUpAwaitility();
        User userOne = ServiceManager.getUserService().getById(1);
        SecurityTestUtils.addUserDataToSecurityContext(userOne, 1);
        await().until(() -> {
            SecurityTestUtils.addUserDataToSecurityContext(userOne, 1);
            return !processService.findByTitle(firstProcess).isEmpty();
        });
    }

    /**
     * Is running after the class has run.
     */
    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldAddLink() throws Exception {
        File metaXmlFile = new File("src/test/resources/metadata/4/meta.xml");
        List<String> metaXmlContentBefore = FileUtils.readLines(metaXmlFile, StandardCharsets.UTF_8);

        MetadataEditor.addLink(ServiceManager.getProcessService().getById(4), "0", 7);

        assertTrue("The link was not added correctly!",
            isInternalMetsLink(FileUtils.readLines(metaXmlFile, StandardCharsets.UTF_8).get(37), 7));

        FileUtils.writeLines(metaXmlFile, StandardCharsets.UTF_8.toString(), metaXmlContentBefore);
        FileUtils.deleteQuietly(new File("src/test/resources/metadata/4/meta.xml.1"));
    }

    @Test
    public void shouldAddMultipleStructuresWithoutMetadata() throws Exception {
        File metaXmlFile = new File("src/test/resources/metadata/2/meta.xml");
        Workpiece workpiece = ServiceManager.getMetsService().loadWorkpiece(metaXmlFile.toURI());

        int oldNrLogicalDivisions = workpiece.getAllLogicalDivisions().size();
        int addedDivisions = 2;
        int newNrDivisions = oldNrLogicalDivisions + addedDivisions;

        MetadataEditor.addMultipleStructures(addedDivisions, "section", workpiece, workpiece.getLogicalStructure(),
            InsertionPosition.FIRST_CHILD_OF_CURRENT_ELEMENT, "", "");        

        List<LogicalDivision> logicalDivisions = workpiece.getAllLogicalDivisions();
        assertTrue("Metadata should be empty",
            logicalDivisions.get(newNrDivisions - 1).getMetadata().isEmpty());
    }

    private boolean isInternalMetsLink(String lineOfMets, int recordNumber) {
        // Order of <mptr> attributes varies
        boolean isInternalMetsLink = lineOfMets.contains("mptr ") && lineOfMets.contains("LOCTYPE=\"OTHER\"")
                && lineOfMets.contains("OTHERLOCTYPE=\"Kitodo.Production\"")
                && lineOfMets.contains("href=\"database://?process.id=" + recordNumber + "\"");
        return isInternalMetsLink;
    }
}
