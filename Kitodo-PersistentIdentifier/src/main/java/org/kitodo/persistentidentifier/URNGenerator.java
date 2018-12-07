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

package org.kitodo.persistentidentifier;

import java.util.HashMap;

import org.kitodo.api.persistentidentifier.urn.UnifiedResourceNameGeneratorInterface;

/**
 * Class for generation and registration DBNURN identifier.
 */
public class URNGenerator implements UnifiedResourceNameGeneratorInterface {

    private static final String SCHEME = "urn:nbn:de";
    private static final int URN_NBN_DE_PART_CHECKSUM = 801;
    private static final HashMap<Character, Integer> CHAR_MAP = new HashMap<>();

    static {
        CHAR_MAP.put('0', 1);
        CHAR_MAP.put('1', 2);
        CHAR_MAP.put('2', 3);
        CHAR_MAP.put('3', 4);
        CHAR_MAP.put('4', 5);
        CHAR_MAP.put('5', 6);
        CHAR_MAP.put('6', 7);
        CHAR_MAP.put('7', 8);
        CHAR_MAP.put('8', 9);
        CHAR_MAP.put('9', 41);
        CHAR_MAP.put('a', 18);
        CHAR_MAP.put('b', 14);
        CHAR_MAP.put('c', 19);
        CHAR_MAP.put('d', 15);
        CHAR_MAP.put('e', 16);
        CHAR_MAP.put('f', 21);
        CHAR_MAP.put('g', 22);
        CHAR_MAP.put('h', 23);
        CHAR_MAP.put('i', 24);
        CHAR_MAP.put('j', 25);
        CHAR_MAP.put('k', 42);
        CHAR_MAP.put('l', 26);
        CHAR_MAP.put('m', 27);
        CHAR_MAP.put('n', 13);
        CHAR_MAP.put('o', 28);
        CHAR_MAP.put('p', 29);
        CHAR_MAP.put('q', 31);
        CHAR_MAP.put('r', 12);
        CHAR_MAP.put('s', 32);
        CHAR_MAP.put('t', 33);
        CHAR_MAP.put('u', 11);
        CHAR_MAP.put('v', 34);
        CHAR_MAP.put('w', 35);
        CHAR_MAP.put('x', 36);
        CHAR_MAP.put('y', 37);
        CHAR_MAP.put('z', 38);
        CHAR_MAP.put('+', 49);
        CHAR_MAP.put(':', 17);
        CHAR_MAP.put('-', 39);
        CHAR_MAP.put('/', 45);
        CHAR_MAP.put('_', 43);
        CHAR_MAP.put('.', 47);
    }

    /**
     * Generate DBNURN identifier. According to this what displays Kitodo
     * Presentation: URN: urn:nbn:de:bsz:14-db-id3787048428 Persistente URL:
     * http://digital.slub-dresden.de/id378704842 SLUB-Katalog (PPN): 378704842
     * SWB-Katalog (PPN): 378704842
     *
     * @param namespace
     *            the URN-namespace (usually unique within an organisation).
     * @param libraryIdentifier
     *            the library identifier.
     * @param subNamespace
     *            the sub namespace.
     * @param identifier
     *            the identifier of the specific object to which the URN points.
     * @return generated URN
     */
    @Override
    public String generate(String namespace, String libraryIdentifier, String subNamespace, String identifier) {
        String urn = namespace + ':' + libraryIdentifier + '-' + subNamespace + "-" + identifier;
        return SCHEME + ':' + urn + getCheckDigit(urn);
    }

    @Override
    public boolean register(String urn) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get check digit for URN.
     *
     * {@see http://www.persistent-identifier.de/?link=316}
     * {@see http://nbn-resolving.de/nbncheckdigit.php}
     *
     * @param urn
     *            a generated identifier
     * @return check digit
     */
    private String getCheckDigit(final String urn) {
        int sum = URN_NBN_DE_PART_CHECKSUM;
        int index = 22;
        int charCode = 0;
        for (Character c : urn.toCharArray()) {
            charCode = CHAR_MAP.get(c);
            if (charCode < 10) {
                sum += charCode * ++index;
            } else {
                sum += (charCode / 10 * ++index) + (charCode % 10 * ++index);
            }
        }
        int lastDigit = ((charCode < 10) ? (charCode) : (charCode % 10));
        if (lastDigit != 0) {
            int checkDigit = (sum / lastDigit) % 10;
            return String.valueOf(checkDigit);
        }
        return "";
    }
}
