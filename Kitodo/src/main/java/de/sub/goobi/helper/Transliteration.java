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

package de.sub.goobi.helper;

public class Transliteration {

    /**
     * Transliterate.
     *
     * @param inString
     *            String
     * @return String
     */
    public String transliterateISO(String inString) {
        StringBuilder s = new StringBuilder();
        char[] arr = inString.toCharArray();
        for (char sign : arr) {
            switch (sign) {
                case 0x410:
                    s.append("A");
                    break;
                case 0x430:
                    s.append("a");
                    break;
                case 0x411:
                    s.append("B");
                    break;
                case 0x431:
                    s.append("b");
                    break;
                case 0x412:
                    s.append("V");
                    break;
                case 0x432:
                    s.append("v");
                    break;
                case 0x413:
                    s.append("G");
                    break;
                case 0x433:
                    s.append("g");
                    break;
                case 0x414:
                    s.append("D");
                    break;
                case 0x434:
                    s.append("d");
                    break;
                case 0x415:
                    s.append("E");
                    break;
                case 0x435:
                    s.append("e");
                    break;
                case 0x0CB:
                    s.append("\u00CB");
                    break;
                case 0x451:
                    s.append("\u00EB");
                    break;
                case 0x416:
                    s.append("\u017D");
                    break;
                case 0x436:
                    s.append("\u017E");
                    break;
                case 0x417:
                    s.append("Z");
                    break;
                case 0x437:
                    s.append("z");
                    break;
                case 0x418:
                    s.append("I");
                    break;
                case 0x438:
                    s.append("i");
                    break;
                case 0x419:
                    s.append("J");
                    break;
                case 0x439:
                    s.append("j");
                    break;
                case 0x41A:
                    s.append("K");
                    break;
                case 0x43A:
                    s.append("k");
                    break;
                case 0x41B:
                    s.append("L");
                    break;
                case 0x43B:
                    s.append("l");
                    break;
                case 0x41C:
                    s.append("M");
                    break;
                case 0x43C:
                    s.append("m");
                    break;
                case 0x41D:
                    s.append("N");
                    break;
                case 0x43D:
                    s.append("n");
                    break;
                case 0x41E:
                    s.append("O");
                    break;
                case 0x43E:
                    s.append("o");
                    break;
                case 0x41F:
                    s.append("P");
                    break;
                case 0x43F:
                    s.append("p");
                    break;
                case 0x420:
                    s.append("R");
                    break;
                case 0x440:
                    s.append("r");
                    break;
                case 0x421:
                    s.append("S");
                    break;
                case 0x441:
                    s.append("s");
                    break;
                case 0x422:
                    s.append("T");
                    break;
                case 0x442:
                    s.append("t");
                    break;
                case 0x423:
                    s.append("U");
                    break;
                case 0x443:
                    s.append("u");
                    break;
                case 0x424:
                    s.append("F");
                    break;
                case 0x444:
                    s.append("f");
                    break;
                case 0x425:
                    s.append("H");
                    break;
                case 0x445:
                    s.append("h");
                    break;
                case 0x426:
                    s.append("C");
                    break;
                case 0x446:
                    s.append("c");
                    break;
                case 0x427:
                    s.append("\u010C");
                    break;
                case 0x447:
                    s.append("\u010D");
                    break;
                case 0x428:
                    s.append("\u0160");
                    break;
                case 0x448:
                    s.append("\u0161");
                    break;
                case 0x429:
                    s.append("\u015C");
                    break;
                case 0x449:
                    s.append("\u015D");
                    break;
                case 0x44A:
                    s.append("\u201D");
                    break;
                case 0x44B:
                    s.append("y");
                    break;
                case 0x44C:
                    s.append("'");
                    break;
                case 0x42D:
                    s.append("\u00C8");
                    break;
                case 0x44D:
                    s.append("\u00E8");
                    break;
                case 0x42E:
                    s.append("\u01D3");
                    break;
                case 0x44E:
                    s.append("\u01D4");
                    break;
                case 0x42F:
                    s.append("\u01CD");
                    break;
                case 0x44F:
                    s.append("\u01CE");
                    break;
                default:
                    s.append(sign);
            }
        }
        return s.toString();
    }

    /**
     * Transliteration.
     *
     * @param inString
     *            String
     * @return String
     */
    public String transliterateDIN(String inString) {
        StringBuilder s = new StringBuilder();
        char[] arr = inString.toCharArray();
        for (char sign : arr) {
            switch (sign) {
                case 0x410:
                    s.append("A");
                    break;
                case 0x430:
                    s.append("a");
                    break;
                case 0x411:
                    s.append("B");
                    break;
                case 0x431:
                    s.append("b");
                    break;
                case 0x412:
                    s.append("V");
                    break;
                case 0x432:
                    s.append("v");
                    break;
                case 0x413:
                    s.append("G");
                    break;
                case 0x433:
                    s.append("g");
                    break;
                case 0x414:
                    s.append("D");
                    break;
                case 0x434:
                    s.append("d");
                    break;
                case 0x415:
                    s.append("E");
                    break;
                case 0x435:
                    s.append("e");
                    break;
                case 0x0CB:
                    s.append("\u00CB");
                    break;
                case 0x451:
                    s.append("\u00EB");
                    break;
                case 0x416:
                    s.append("\u017D");
                    break;
                case 0x436:
                    s.append("\u017E");
                    break;
                case 0x417:
                    s.append("Z");
                    break;
                case 0x437:
                    s.append("z");
                    break;
                case 0x418:
                    s.append("I");
                    break;
                case 0x438:
                    s.append("i");
                    break;
                case 0x419:
                    s.append("J");
                    break;
                case 0x439:
                    s.append("j");
                    break;
                case 0x41A:
                    s.append("K");
                    break;
                case 0x43A:
                    s.append("k");
                    break;
                case 0x41B:
                    s.append("L");
                    break;
                case 0x43B:
                    s.append("l");
                    break;
                case 0x41C:
                    s.append("M");
                    break;
                case 0x43C:
                    s.append("m");
                    break;
                case 0x41D:
                    s.append("N");
                    break;
                case 0x43D:
                    s.append("n");
                    break;
                case 0x41E:
                    s.append("O");
                    break;
                case 0x43E:
                    s.append("o");
                    break;
                case 0x41F:
                    s.append("P");
                    break;
                case 0x43F:
                    s.append("p");
                    break;
                case 0x420:
                    s.append("R");
                    break;
                case 0x440:
                    s.append("r");
                    break;
                case 0x421:
                    s.append("S");
                    break;
                case 0x441:
                    s.append("s");
                    break;
                case 0x422:
                    s.append("T");
                    break;
                case 0x442:
                    s.append("t");
                    break;
                case 0x423:
                    s.append("U");
                    break;
                case 0x443:
                    s.append("u");
                    break;
                case 0x424:
                    s.append("F");
                    break;
                case 0x444:
                    s.append("f");
                    break;
                case 0x425:
                    s.append("Ch");
                    break;
                case 0x445:
                    s.append("ch");
                    break;
                case 0x426:
                    s.append("C");
                    break;
                case 0x446:
                    s.append("c");
                    break;
                case 0x427:
                    s.append("\u010C");
                    break;
                case 0x447:
                    s.append("\u010D");
                    break;
                case 0x428:
                    s.append("\u0160");
                    break;
                case 0x448:
                    s.append("\u0161");
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
                case 0x44B:
                    s.append("y");
                    break;
                case 0x44C:
                    s.append("'");
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
        return s.toString();
    }
}
