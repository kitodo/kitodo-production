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

package org.kitodo.production.exporter.download;

import static org.kitodo.test.utils.ProcessTestUtils.METADATA_DIR;
import static org.kitodo.test.utils.ProcessTestUtils.META_XML;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.lang3.SystemUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.ExecutionPermission;
import org.kitodo.FileLoader;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.User;
import org.kitodo.export.ExportMets;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.file.FileService;

public class ExportMetsIT {

    private static final File scriptCreateDirUserHome = new File(
            ConfigCore.getParameter(ParameterCore.SCRIPT_CREATE_DIR_USER_HOME));
    private static final FileService fileService = ServiceManager.getFileService();
    private static String userDirectory;
    private static String metadataDirectory;
    private static Process process;

    private final ExportMets exportMets = new ExportMets();

    @BeforeClass
    public static void setUp() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();

        User user = ServiceManager.getUserService().getById(1);
        process = ServiceManager.getProcessService().getById(1);
        metadataDirectory = process.getId().toString();
        userDirectory = user.getLogin();

        fileService.createDirectory(URI.create(""), metadataDirectory);
        fileService.copyFile(URI.create(METADATA_DIR + "/testmetaNewFormat.xml"), URI.create(metadataDirectory + META_XML));
        SecurityTestUtils.addUserDataToSecurityContext(user, 1);
        FileLoader.createConfigProjectsFile();

        if (!SystemUtils.IS_OS_WINDOWS) {
            ExecutionPermission.setExecutePermission(scriptCreateDirUserHome);
        }

        File userdataDirectory = new File(ConfigCore.getParameter(ParameterCore.DIR_USERS));
        if (!userdataDirectory.exists() && !userdataDirectory.mkdir()) {
            throw new IOException("Could not create users directory");
        }
    }

    @AfterClass
    public static void tearDown() throws Exception {
        SecurityTestUtils.cleanSecurityContext();
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
        fileService.delete(URI.create(metadataDirectory));
        fileService.delete(ConfigCore.getUriParameter(ParameterCore.DIR_USERS));
        FileLoader.deleteConfigProjectsFile();

        if (!SystemUtils.IS_OS_WINDOWS) {
            ExecutionPermission.setNoExecutePermission(scriptCreateDirUserHome);
        }
    }

    @Test
    public void exportMetsTest() throws Exception {
        if (SystemUtils.IS_OS_WINDOWS) {
            // This is a workaround for the problem that the startExport method
            // is calling
            // an external shell script for creating directories. This code only
            // does the work of that script.
            // TODO Find a better way for changing script selection
            // corresponding to OS
            fileService.createDirectory(ConfigCore.getUriParameter(ParameterCore.DIR_USERS), userDirectory);
        }

        exportMets.startExport(process);
        List<String> strings = Files.readAllLines(Paths.get(ConfigCore.getParameter(ParameterCore.DIR_USERS) + userDirectory
                + "/" + Helper.getNormalizedTitle(process.getTitle()) + "_mets.xml"));
        Assert.assertTrue("Export of metadata 'singleDigCollection' was wrong",
            strings.toString().contains("<kitodo:metadata name=\"singleDigCollection\">test collection</kitodo:metadata>"));
        Assert.assertTrue("Export of metadata 'TitleDocMain' was wrong",
            strings.toString().contains("<kitodo:metadata name=\"TitleDocMain\">test title</kitodo:metadata>"));
        Assert.assertTrue("Export of metadata 'PublisherName' was wrong",
            strings.toString().contains("<kitodo:metadata name=\"PublisherName\">Publisher test name</kitodo:metadata>"));
    }
}
