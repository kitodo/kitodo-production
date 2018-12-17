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

package org.kitodo.production.editor;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.KitodoConfigFile;

/**
 * Test class for class 'XMLEditorTest'.
 */
public class XMLEditorTest {

    private static final String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    private static final String LOAD_XML = XML_DECLARATION + "<loadRoot>LoadRootValue</loadRoot>";
    private static final String SAVE_XML = XML_DECLARATION + "<saveRoot>SaveRootValue</saveRoot>";
    private static String absolutePath = ConfigCore.getKitodoConfigDirectory() + KitodoConfigFile.PROJECT_CONFIGURATION;
    private static XMLEditor xmlEditor = null;

    @BeforeClass
    public static void setUp() throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(LOAD_XML);
        Files.write(Paths.get(absolutePath), lines);
        xmlEditor = new XMLEditor();
        xmlEditor.loadInitialConfiguration();
    }

    @AfterClass
    public static void tearDown() throws IOException {
        Files.deleteIfExists(Paths.get(absolutePath));
    }

    @Test
    public void shouldLoadXMLConfiguration() {
        xmlEditor.loadXMLConfiguration(KitodoConfigFile.PROJECT_CONFIGURATION.getName());
        Assert.assertEquals(LOAD_XML, xmlEditor.getXMLConfiguration().replace("\n", "").replace("\r", ""));
    }

    @Test
    public void shouldSaveXMLConfiguration() throws IOException {
        xmlEditor.loadXMLConfiguration(KitodoConfigFile.PROJECT_CONFIGURATION.getName());
        xmlEditor.setXMLConfiguration(SAVE_XML);
        xmlEditor.saveXMLConfiguration();
        String savedString = FileUtils.readFileToString(KitodoConfigFile.PROJECT_CONFIGURATION.getFile(), "utf-8");
        Assert.assertEquals(SAVE_XML, savedString);
    }
}
