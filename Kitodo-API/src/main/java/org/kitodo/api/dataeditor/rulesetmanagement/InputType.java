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

package org.kitodo.api.dataeditor.rulesetmanagement;

/**
 * Enumerates the possible instructions for displaying the input field.
 */
public enum InputType {
    /**
     * Instruction to represent the element as a boolean selection. The design,
     * such as a checkbox, two radio buttons, or drop-down with two values, is
     * left to the application.
     */
    BOOLEAN,

    /**
     * Instruction to represent the element as a date input. The design is left
     * to the application. In the simplest case, a single-line text field is
     * sufficient here, but a comfortable date selection can also be realized.
     * Importantly, the date entry must support dates prior to 1970.
     */
    DATE,

    /**
     * Instruction to represent the element as integer input. The design is left
     * to the application. In the simplest case, a single-line text field is
     * sufficient here, but rotary or slider controls can also be implemented.
     * It is important that you can always enter an exact number.
     */
    INTEGER,

    /**
     * Instruction to represent the element as a single-selection list. The
     * design, for example as a list box or as a sequence of radio buttons, is
     * left to the application.
     */
    MULTI_LINE_SINGLE_SELECTION,

    /**
     * Instruction to represent the element as a larger text input field for
     * multi-line texts.
     */
    MULTI_LINE_TEXT,

    /**
     * Instruction to represent the element as a list with multiple selections.
     * The design is left to the application. However, it is important to make
     * it obvious to the user that he has an easy way to select more than one
     * value here.
     */
    MULTIPLE_SELECTION,

    /**
     * Instruction to present the element as a one-line selection option. The
     * design, for example, as a drop-down list, is left to the application.
     */
    ONE_LINE_SINGLE_SELECTION,

    /**
     * Instruction to represent the element as a small, one-line text field.
     */
    ONE_LINE_TEXT
}
