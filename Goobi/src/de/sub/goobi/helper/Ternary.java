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

/**
 * A ternary is a three-valued logic system in which there are three truth
 * values indicating true, false and some unknown third value.
 *
 * @see "https://en.wikipedia.org/wiki/Three-valued_logic"
 */
public enum Ternary {
    /**
     * The ternary value of false.
     */
    FALSE,

    /**
     * The ternary value of true.
     */
    TRUE,

    /**
     * The ternary value of unknown.
     */
    UNKNOWN;
}
