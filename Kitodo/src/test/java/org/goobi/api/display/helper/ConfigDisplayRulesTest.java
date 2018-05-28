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

package org.goobi.api.display.helper;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.goobi.api.display.Item;
import org.goobi.api.display.enums.DisplayType;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.FileLoader;

public class ConfigDisplayRulesTest {

    private ConfigDisplayRules configDisplayRules = ConfigDisplayRules.getInstance();

    @BeforeClass
    public static void setUp() throws Exception {
        FileLoader.createMetadataDisplayRulesFile();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        FileLoader.deleteMetadataDisplayRulesFile();
    }

    @Test
    public void shouldGetElementTypeByName() {
        DisplayType displayType = configDisplayRules.getElementTypeByName("DigiNews", "edit", "NotePreImport");

        assertEquals("Process definition was not loaded!", DisplayType.SELECT1, displayType);
    }

    @Test
    public void shouldGetItemsByNameAndType() {
        List<Item> items = configDisplayRules.getItemsByNameAndType("DigiNews", "edit", "NotePreImport",
            DisplayType.SELECT1);

        assertEquals("Process definition was not loaded!", 2, items.size());
    }
}
