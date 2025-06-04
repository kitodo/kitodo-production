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


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.FileLoader;
import org.kitodo.config.ConfigCore;

public class KitodoConfigFileTest {

    @BeforeAll
    public static void setUp() throws Exception {
        FileLoader.createConfigProjectsFile();
    }

    @AfterAll
    public static void tearDown() throws Exception {
        FileLoader.deleteConfigProjectsFile();
    }

    @Test
    public void shouldGetFileNameTest() {
        assertEquals("kitodo_projects.xml", String.valueOf(KitodoConfigFile.PROJECT_CONFIGURATION), "Config projects file name is incorrect!");
        assertEquals("kitodo_projects.xml", KitodoConfigFile.PROJECT_CONFIGURATION.getName(), "Config projects file name is incorrect!");
    }

    @Test
    public void shouldGetAbsolutePathTest() {
        assertTrue(KitodoConfigFile.PROJECT_CONFIGURATION.getAbsolutePath().contains("Kitodo" + File.separator + "src"
                + File.separator + "test" + File.separator + "resources" + File.separator + "kitodo_projects.xml"), "Config projects file absolute path is incorrect!");
    }

    @Test
    public void shouldGetFileTest() {
        assertEquals(new File(ConfigCore.getKitodoConfigDirectory() + "kitodo_projects.xml"), KitodoConfigFile.PROJECT_CONFIGURATION.getFile(), "Config projects file absolute path is incorrect!");
    }

    @Test
    public void shouldGetByFileNameTest() throws FileNotFoundException {
        assertEquals(KitodoConfigFile.PROJECT_CONFIGURATION, KitodoConfigFile.getByName("kitodo_projects.xml"), "Config projects file doesn't exists for given!");
    }

    @Test
    public void shouldNotGetByFileNameTest() {
        Exception exception = assertThrows(FileNotFoundException.class,
            () -> KitodoConfigFile.getByName("kitodo_nonexistent.xml"));

        assertEquals("Configuration file 'kitodo_nonexistent.xml' doesn't exists!", exception.getMessage());
    }

    @Test
    public void configFileShouldExistTest() {
        assertTrue(KitodoConfigFile.PROJECT_CONFIGURATION.exists(), "Config projects file doesn't exist!");
    }

    @Test
    public void configFileShouldNotExistTest() {
        assertTrue(KitodoConfigFile.OPAC_CONFIGURATION.exists(), "Config OPAC file exists!");
    }
}
