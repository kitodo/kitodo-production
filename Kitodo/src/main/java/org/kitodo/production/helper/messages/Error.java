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

import java.util.Enumeration;
import java.util.Objects;

public class Error extends CustomResourceBundle {

    @Override
    public Enumeration<String> getKeys() {
        return getBaseResources("messages.errors").getKeys();
    }

    @Override
    protected Object handleGetObject(String key) {
        // If there is an external value use that
        Object externalValue = getValueFromExternalResourceBundle(key, "errors");
        if (Objects.nonNull(externalValue)) {
            return externalValue;
        }
        // otherwise use the one defined in the property files
        return getBaseResources("messages.errors").getObject(key);
    }
}
