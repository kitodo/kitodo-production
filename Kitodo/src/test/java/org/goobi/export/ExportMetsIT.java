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

package org.goobi.export;

import de.sub.goobi.export.download.ExportMets;
import de.sub.goobi.helper.exceptions.ExportFileException;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.FileLoader;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.api.ugh.exceptions.MetadataTypeNotAllowedException;
import org.kitodo.api.ugh.exceptions.PreferencesException;
import org.kitodo.api.ugh.exceptions.ReadException;
import org.kitodo.api.ugh.exceptions.WriteException;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.file.FileService;

public class ExportMetsIT {

    private static FileService fileService = new FileService();
    private static ServiceManager serviceManager = new ServiceManager();
    ExportMets exportMets = new ExportMets();

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        fileService.createDirectory(URI.create(""), "1");
        fileService.copyFileToDirectory(URI.create("metadata/testmetaOldFormat.xml"),URI.create("1"));
        fileService.renameFile(URI.create("1/testmetaOldFormat.xml"),"meta.xml");
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        SecurityTestUtils.addUserDataToSecurityContext(serviceManager.getUserService().getById(1));
        FileLoader.createConfigProjectsFile();
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        SecurityTestUtils.cleanSecurityContext();
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
        fileService.delete(URI.create("1"));
        fileService.delete(URI.create("export"));
        FileLoader.deleteConfigProjectsFile();
    }

    @Test
    public void exportMetsTest() throws Exception {
        Process process = serviceManager.getProcessService().getById(1);
        process.getRuleset().setFile("rulesets/ruleset_test.xml");
        process.setTitle("exportedProcess");
        exportMets.startExport(process,Paths.get("src/test/resources/export/1").toUri());
        List<String> strings = Files.readAllLines(Paths.get("src/test/resources/export/exportedProcess_mets.xml"));
        Assert.assertTrue(strings.get(1).contains("<mods:publisher>Test Publisher</mods:publisher>"));
        Assert.assertTrue(strings.get(1).contains("<mods:title>Test Title</mods:title>"));
        Assert.assertTrue(strings.get(1).contains("<mods:namePart type=\"given\">FirstTestName</mods:namePart>"));
    }
}
