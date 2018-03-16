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

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Class for generation and registration DBNURN identifier.
 */
public class URNGenerator implements UnifiedResourceNameGeneratorInterface {

    private static final String SCHEME = "urn:nbn:de";
    private static final int URN_NBN_DE_PART_CHECKSUM = 801;
    private static final HashMap<Character, Integer> CHAR_MAP = new HashMap<Character, Integer>() {
        {
            put('0', 1);
            put('1', 2);
            put('2', 3);
            put('3', 4);
            put('4', 5);
            put('5', 6);
            put('6', 7);
            put('7', 8);
            put('8', 9);
            put('9', 41);
            put('a', 18);
            put('b', 14);
            put('c', 19);
            put('d', 15);
            put('e', 16);
            put('f', 21);
            put('g', 22);
            put('h', 23);
            put('i', 24);
            put('j', 25);
            put('k', 42);
            put('l', 26);
            put('m', 27);
            put('n', 13);
            put('o', 28);
            put('p', 29);
            put('q', 31);
            put('r', 12);
            put('s', 32);
            put('t', 33);
            put('u', 11);
            put('v', 34);
            put('w', 35);
            put('x', 36);
            put('y', 37);
            put('z', 38);
            put('+', 49);
            put(':', 17);
            put('-', 39);
            put('/', 45);
            put('_', 43);
            put('.', 47);
        }
    };

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
        throw new NotImplementedException();
    }

    /**
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
