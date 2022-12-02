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

package org.kitodo.config.enums;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitodo.FileLoader;
import org.kitodo.config.ConfigCore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class KitodoConfigFileTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @BeforeClass
    public static void setUp() throws Exception {
        FileLoader.createConfigProjectsFile();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        FileLoader.deleteConfigProjectsFile();
    }

    @Test
    public void shouldGetFileNameTest() {
        assertEquals("Config projects file name is incorrect!", "kitodo_projects.xml",
            String.valueOf(KitodoConfigFile.PROJECT_CONFIGURATION));
        assertEquals("Config projects file name is incorrect!", "kitodo_projects.xml",
            KitodoConfigFile.PROJECT_CONFIGURATION.getName());
    }

    @Test
    public void shouldGetAbsolutePathTest() {
        assertTrue("Config projects file absolute path is incorrect!",
            KitodoConfigFile.PROJECT_CONFIGURATION.getAbsolutePath().contains("Kitodo" + File.separator + "src"
                    + File.separator + "test" + File.separator + "resources" + File.separator + "kitodo_projects.xml"));
    }

    @Test
    public void shouldGetFileTest() {
        assertEquals("Config projects file absolute path is incorrect!",
            new File(ConfigCore.getKitodoConfigDirectory() + "kitodo_projects.xml"),
            KitodoConfigFile.PROJECT_CONFIGURATION.getFile());
    }

    @Test
    public void shouldGetByFileNameTest() throws FileNotFoundException {
        assertEquals("Config projects file doesn't exists for given!", KitodoConfigFile.PROJECT_CONFIGURATION,
            KitodoConfigFile.getByName("kitodo_projects.xml"));
    }

    @Test
    public void shouldNotGetByFileNameTest() throws FileNotFoundException {
        exception.expect(FileNotFoundException.class);
        exception.expectMessage("Configuration file 'kitodo_nonexistent.xml' doesn't exists!");
        KitodoConfigFile.getByName("kitodo_nonexistent.xml");
    }

    @Test
    public void configFileShouldExistTest() {
        assertTrue("Config projects file doesn't exist!", KitodoConfigFile.PROJECT_CONFIGURATION.exists());
    }

    @Test
    public void configFileShouldNotExistTest() {
        assertTrue("Config OPAC file exists!", KitodoConfigFile.OPAC_CONFIGURATION.exists());
    }
}
