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

package org.kitodo.production.config;

import java.io.IOException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.production.FileLoader;
import org.kitodo.production.config.ConfigProject;

import static org.junit.Assert.assertEquals;

public class ConfigProjectTest {

    private static ConfigProject configProject;

    @BeforeClass
    public static void setUp() throws Exception {
        FileLoader.createConfigProjectsFile();
        configProject = new ConfigProject("default");
    }

    @AfterClass
    public static void tearDown() throws IOException {
        FileLoader.deleteConfigProjectsFile();
    }

    @Test
    public void shouldGetConfigProjectsForItems() {
        List<String> items = configProject.getParamList("createNewProcess.itemlist.item");
        assertEquals("Incorrect amount of items!", 10, items.size());
    }

    @Test
    public void shouldGetConfigProjectsForSelectItems() {
        List<String> items = configProject.getParamList("createNewProcess.itemlist.item(1).select");
        assertEquals("Incorrect amount of select items for second element!", 3, items.size());
    }

    @Test
    public void shouldGetConfigProjectsForProcessTitles() {
        List<String> processTitles = configProject.getParamList("createNewProcess.itemlist.processtitle");
        assertEquals("Incorrect amount of process titles!", 5, processTitles.size());
    }

    @Test
    public void shouldGetDocType() {
        assertEquals("Document type is incorrect!", "monograph", configProject.getDocType());
    }

    @Test
    public void shouldGetTifDefinition() {
        assertEquals("Tif definition is incorrect!", "kitodo", configProject.getTifDefinition());
    }

    @Test
    public void shouldGetTitleDefinition() {
        String titleDefinition = configProject.getTitleDefinition();
        String expected = "ATS+TSL+'_'+PPN digital a-Satz";
        assertEquals("Title definition is incorrect!", expected, titleDefinition);
    }
}
