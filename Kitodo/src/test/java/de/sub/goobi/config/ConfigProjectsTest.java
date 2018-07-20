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

package de.sub.goobi.config;

import java.io.IOException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.FileLoader;

import static org.junit.Assert.assertEquals;

public class ConfigProjectsTest {

    private static ConfigProjects configProjects;

    @BeforeClass
    public static void setUp() throws Exception {
        FileLoader.createConfigProjectsFile();
        configProjects = new ConfigProjects("default");
    }

    @AfterClass
    public static void tearDown() throws IOException {
        FileLoader.deleteConfigProjectsFile();
    }

    @Test
    public void shouldGetConfigProjectsForItems() {
        List<String> items = configProjects.getParamList("createNewProcess.itemlist.item");
        assertEquals("Incorrect amount of items!", 10, items.size());
    }

    @Test
    public void shouldGetConfigProjectsForSelectItems() {
        List<String> items = configProjects.getParamList("createNewProcess.itemlist.item(1).select");
        assertEquals("Incorrect amount of select items for second element!", 3, items.size());
    }

    @Test
    public void shouldGetConfigProjectsForProcessTitles() {
        List<String> processTitles = configProjects.getParamList("createNewProcess.itemlist.processtitle");
        assertEquals("Incorrect amount of process titles!", 5, processTitles.size());
    }
}
