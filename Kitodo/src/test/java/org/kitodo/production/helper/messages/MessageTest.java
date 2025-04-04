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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;

import org.junit.jupiter.api.Test;

public class MessageTest {

    private final Locale locale = new Locale("EN");
    private final String customBundle = "test_messages";
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

        assertTrue(containsKey, "Keys set doesn't contain searched key!");
    }

    @Test
    public void shouldGetStringFromDefaultBundle() {
        // in case custom bundle does not exist
        assertEquals("Ready", Message.getResourceBundle(defaultBundle, "non-existent-bundle", locale).getString("ready"));
    }

    @Test
    public void shouldGetStringFromCustomBundle() throws Exception {
        // in case custom bundle exists, and also contains definition for the requested key
        assertEquals("Test custom message", Message.getResourceBundle(defaultBundle, customBundle, locale).getString("ready"));
    }

    @Test
    public void shouldThrowMissingRessourceExceptionForNonExistentKey() {
        // in case custom bundle is loaded, but key does not exist in either resource bundles 
        assertThrows(
            MissingResourceException.class, 
            () -> Message.getResourceBundle(defaultBundle, customBundle, locale).getString("non-existent-key")
        );

        // in case custom bundle is missing, and key does also not exist in default bundle
        assertThrows(
            MissingResourceException.class, 
            () -> Message.getResourceBundle(defaultBundle, "non-existent-bundle", locale).getString("non-existent-key")
        );

        // in case custom bundle is loaded, but does not include the key, even if the key exists in the default bundle
        assertThrows(
            MissingResourceException.class, 
            () -> Message.getResourceBundle(defaultBundle, customBundle, locale).getString("login")
        );
    }

}
