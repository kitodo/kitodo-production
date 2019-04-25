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

package org.kitodo.longtermpreservationvalidation;

import edu.harvard.hul.ois.jhove.RepInfo;

/**
 * Enumeration of possible values in ternary logic.
 */
enum TernaryValue {
    /**
     * A constant with the truth value true. Corresponds to the boolean value
     * true.
     */
    TRUE {
        @Override
        String toModalAdverb() {
            return "";
        }
    },

    /**
     * A constant with the truth value maybe. The use of this constant has the
     * meaning of knowing that we know nothing, what is different from not
     * knowing if we do not know something. This value has no equivalent in
     * Boolean logic.
     */
    MAYBE {
        @Override
        String toModalAdverb() {
            return " unclear if";
        }
    },

    /**
     * A constant with the truth value false. Corresponds to the boolean value
     * false.
     */
    FALSE {
        @Override
        String toModalAdverb() {
            return " not";
        }
    };

    /**
     * A modal adverb is a word that modifies the quality, manner, quantity, or
     * intensity of an expression, such as "not" or "maybe." For the sake of
     * simpler string building, returning a word returns a space before the
     * word.
     * 
     * @return a modal adverb, can also be empty if the linguistic utterance
     *         attains its correctness through non-alteration
     */
    abstract String toModalAdverb();

    /**
     * JHove returns the state of the result as int. This is converted into a
     * Java value as optional of truth value.
     * 
     * @param repInfo
     *            State of result as RepInfo (int) value
     * @return an optional one of truth value
     */
    static TernaryValue valueOf(int repInfo) {
        switch (repInfo) {
            case RepInfo.UNDETERMINED:
                return TernaryValue.MAYBE;
            case RepInfo.FALSE:
                return TernaryValue.FALSE;
            case RepInfo.TRUE:
                return TernaryValue.TRUE;
            default:
                throw new IllegalArgumentException("The argument must be in the range " + RepInfo.UNDETERMINED + " to "
                        + RepInfo.TRUE + ", but was " + repInfo + '.');
        }
    }
}
