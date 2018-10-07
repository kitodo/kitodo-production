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

package org.goobi.webapi.beans;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IdentifierPPN {

    private String ppn;

    /**
     * Constructor.
     * 
     * @param ppn
     *            as String
     */
    public IdentifierPPN(String ppn) {
        if (!isValid(ppn)) {
            throw new IllegalArgumentException("Given string is not a valid PPN identifier.");
        }
        this.ppn = ppn;
    }

    /**
     * Check if identifier is valid.
     * 
     * @param identifier
     *            to check
     * @return true or false
     */
    public static boolean isValid(String identifier) {
        boolean result;
        int flags = Pattern.CASE_INSENSITIVE;
        Pattern pattern;
        Matcher matcher;

        if ((identifier == null) || (identifier.length() == 0)) {
            result = false;
        } else {
            pattern = Pattern.compile("^[0-9]{8}[0-9LXYZ]{1}$", flags);
            matcher = pattern.matcher(identifier);
            result = matcher.matches();
        }

        return result;
    }

    @Override
    public String toString() {
        return ppn;
    }

}
