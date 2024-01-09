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

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Divisions that have a function.
 */
public enum FunctionalDivision {
    /**
     * Children are created by calendar form.
     */
    CREATE_CHILDREN_WITH_CALENDAR("createChildrenWithCalendar"),

    /**
     * A division whose children are created from this division directly.
     */
    CREATE_CHILDREN_FROM_PARENT("createChildrenFromParent"),

    /**
     * Indicate the division for media partial usage.
     */
    MEDIA_PARTIAL("mediaPartial");

    /**
     * With the logger, text can be written to a log file or to the console.
     */
    private static final Logger logger = LogManager.getLogger(FunctionalDivision.class);

    /**
     * This character string defines how the special field is to be marked in
     * the ruleset.
     */
    private final String mark;

    /**
     * Since this is an enum, the constructor cannot be called, except from Java
     * when building the class.
     *
     * @param mark
     *            how the special field is to be marked
     */
    private FunctionalDivision(String mark) {
        this.mark = mark;
    }

    /**
     * Iterates over the {@code enum} constants, and if the candidate value has
     * the searched mark, it is added to the list.
     *
     * @param mark
     *            a character string defining how the special field is to be
     *            marked in the ruleset
     * @param to
     *            object to add value, return value of {@link #valuesOf(String)}
     * @return whether the loop has to continue
     */
    private static boolean addEnumByMark(String mark, Set<FunctionalDivision> to) {
        for (FunctionalDivision candidate : FunctionalDivision.values()) {
            if (mark.equals(candidate.mark)) {
                to.add(candidate);
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a string which defines how the special field is to be marked in
     * the ruleset.
     *
     * @return how the special field is to be marked
     */
    public String getMark() {
        return mark;
    }

    /**
     * This function is like {@code valueOf(String)}, except that it allows
     * multiple values in the input string and can return multiple values in the
     * return value. Unknown strings (misspellings) are reported in logging.
     *
     * @param marks
     *            string to be processed
     * @return fields
     */
    public static Set<FunctionalDivision> valuesOf(String marks) {
        Set<FunctionalDivision> values = new TreeSet<>();
        for (String mark : marks.split("\\s+", 0)) {
            if (addEnumByMark(mark, values)) {
                continue;
            }
            logger.warn("Ruleset declares undefined division use '{}', must be one of: {}", mark,
                    Arrays.stream(values()).map(FunctionalDivision::getMark).collect(Collectors.joining(", ")));
        }
        return values;
    }
}
