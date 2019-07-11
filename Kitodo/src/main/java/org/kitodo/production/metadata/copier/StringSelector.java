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

package org.kitodo.production.metadata.copier;

import org.apache.commons.configuration.ConfigurationException;

/**
 * A string selector will always return the string used to create it. It can be
 * used for static arguments.
 */
public class StringSelector extends DataSelector {

    /**
     * Static string value.
     */
    private final String text;

    /**
     * Creates a new string selector with a static string value.
     *
     * @param text
     *            String, in quotes
     * @throws ConfigurationException
     *             if the string isn’t enclosed in quotes
     */
    public StringSelector(String text) throws ConfigurationException {
        if (!text.endsWith("\"")) {
            throw new ConfigurationException("String must be enclosed in double quotes (\"\"), but is: " + text);
        }
        this.text = text.substring(1, text.length() - 1);
    }

    /**
     * Returns the value of the string used to create the selector.
     *
     * @see org.kitodo.production.metadata.copier.DataSelector#findIn(org.kitodo.production.metadata.copier.CopierData)
     */
    @Override
    public String findIn(CopierData data) {
        return text;
    }

}
