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

package org.kitodo.production.forms.dataeditor;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A character or string that is used to separate the two page numbers in
 * double-page pagination.
 */
class Separator {
    /**
     * Regular expression to remove quotes from the input string.
     */
    private static final Pattern UNQUOTE = Pattern.compile("[^,\"]+|\"([^\"]*)\"");

    /**
     * Creates a lot of separator objects from a String array.
     *
     * @param data
     *            elements to create Separators from
     * @return a list of separator objects
     */
    static List<Separator> factory(String data) {
        List<Separator> result = new LinkedList<>();
        Matcher m = UNQUOTE.matcher(data);
        while (m.find()) {
            if (m.group(1) != null) {
                result.add(new Separator(m.group(1)));
            } else {
                result.add(new Separator(m.group()));
            }
        }
        return result;
    }

    /**
     * The separator string.
     */
    private final String separator;

    /**
     * Creates a new separator.
     *
     * @param separator
     *            separator String
     */
    private Separator(String separator) {
        this.separator = separator;
    }

    /**
     * Returns the separator string.
     *
     * @return the separator string
     */
    String getSeparatorString() {
        return separator;
    }
}
