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

package org.kitodo.helper;

import com.ibm.icu.text.Transliterator;

public class Transliteration {

    private Transliterator iso;

    /**
     * Use Transliterator for transliteration from cyrillic to latin.
     */
    public Transliteration() {
        iso = Transliterator.getInstance("Cyrillic-Latin");
    }

    /**
     * Transliterate cyrillic to latin.
     *
     * @param text
     *            to transliterate
     * @return transliterated text
     */
    public String transliterateISO(String text) {
        return iso.transliterate(text);
    }

    /**
     * Transliterate cyrillic to german DIN.
     *
     * @param text
     *            to transliterate
     * @return transliterated text
     */
    public String transliterateDIN(String text) {
        StringBuilder s = new StringBuilder();
        char[] arr = text.toCharArray();
        for (char sign : arr) {
            switch (sign) {
                case 0x425:
                    s.append("Ch");
                    break;
                case 0x445:
                    s.append("ch");
                    break;
                case 0x429:
                    s.append("\u0160" + "\u010D");
                    break;
                case 0x449:
                    s.append("\u0161" + "\u010D");
                    break;
                case 0x44A:
                    s.append("\u201D");
                    break;
                case 0x42D:
                    s.append("\u0116");
                    break;
                case 0x44D:
                    s.append("\u0117");
                    break;
                case 0x42E:
                    s.append("Ju");
                    break;
                case 0x44E:
                    s.append("ju");
                    break;
                case 0x42F:
                    s.append("Ja");
                    break;
                case 0x44F:
                    s.append("ja");
                    break;
                default:
                    s.append(sign);
            }
        }
        return iso.transliterate(s.toString());
    }
}
