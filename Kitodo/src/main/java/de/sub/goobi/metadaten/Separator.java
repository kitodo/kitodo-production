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

package de.sub.goobi.metadaten;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Object representing a separator.
 *
 * @author Matthias Ronge
 */
public class Separator implements Selectable {

    /**
     * Characters not allowed here.
     */
    private static final int[] ILLEGAL_CHARACTERS = {0, 34 };

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
    public static List<Separator> factory(String data) {
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
     * Creates a new separator
     *
     * @param separator
     *            separator String
     */
    public Separator(String separator) {
        for (int i = 0; i < separator.length(); i++) {
            int codePoint = separator.codePointAt(i);
            for (int illegal : ILLEGAL_CHARACTERS) {
                if (codePoint == illegal) {
                    throw new IllegalArgumentException(
                            String.format("Illegal character %c (U+%04X) at index %d.", illegal, illegal, i));
                }
            }
        }
        this.separator = separator;
    }

    /**
     * Returns a readable ID for the separator.
     */
    @Override
    public String getId() {
        return new BigInteger(separator.getBytes(StandardCharsets.UTF_8)).toString(Character.MAX_RADIX);
    }

    /**
     * Returns a visible label for the separator. White spaces are replaced by
     * open boxes (␣) to be visible.
     */
    @Override
    public String getLabel() {
        return separator.replaceAll(" ", "\u2423");
    }

    /**
     * Return the separator string.
     *
     * @return the separator string
     */
    public String getSeparatorString() {
        return separator;
    }
}
