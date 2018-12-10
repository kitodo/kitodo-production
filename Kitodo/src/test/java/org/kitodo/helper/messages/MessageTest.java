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

package org.kitodo.helper.messages;

import java.net.URI;
import java.util.Enumeration;
import java.util.Locale;

import org.junit.Test;
import org.kitodo.FileLoader;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.file.FileService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MessageTest {

    private static final FileService fileService = ServiceManager.getFileService();
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
        System.out.println(Message.getResourceBundle(defaultBundle, customBundle, locale).getString("ready"));
    }

    @Test
    public void shouldGetStringFromCustomBundle() throws Exception {
        fileService.createDirectory(URI.create(""), "custom");
        FileLoader.createCustomMessages();

        String value = Message.getResourceBundle(defaultBundle, customBundle, locale).getString("ready");
        assertEquals("Test custom message", value);

        FileLoader.deleteCustomMessages();
        fileService.delete(URI.create("custom"));
    }
}
