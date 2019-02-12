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

package org.kitodo.production.helper.messages;

import java.io.File;
import java.util.Enumeration;
import java.util.Locale;

import org.junit.Test;
import org.kitodo.FileLoader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class MessageTest {

    private final Locale locale = new Locale("EN");
    private final String customBundle = "messages";
    private final String defaultBundle = "messages.messages";


    @Test
    public void shouldGetKeys() {
        Enumeration<String> keys = Message.getResourceBundle(defaultBundle, customBundle, locale).getKeys();

        boolean containsKey = false;
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            if (key.equals("ready")) {
                containsKey = true;
                break;
            }
        }

        assertTrue("Keys set doesn't contain searched key!", containsKey);
    }

    @Test
    public void shouldGetStringFromDefaultBundle() {
        assertEquals("Ready", Message.getResourceBundle(defaultBundle, customBundle, locale).getString("ready"));
    }

    @Test
    public void shouldGetStringFromCustomBundle() throws Exception {
        File messageDirectory = new File("src/test/resources/custom");

        if (messageDirectory.mkdir()) {
            FileLoader.createCustomMessages();

            String value = Message.getResourceBundle(defaultBundle, customBundle, locale).getString("ready");
            assertEquals("Test custom message", value);

            FileLoader.deleteCustomMessages();
            messageDirectory.delete();
        } else {
            fail("Directory for custom messages was not created!");
        }
    }
}
