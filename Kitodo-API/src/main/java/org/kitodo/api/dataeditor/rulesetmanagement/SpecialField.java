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
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Certain fields in the ruleset that have a special function.
 */
public enum SpecialField {
    /**
     * The author’s last name. This is used in the application to generate the
     * author-title key.
     */
    AUTHOR_LAST_NAME("authorLastName"),

    /**
     * The name of the data source from which the record was imported. This is
     * saved for later comparison of the data records.
     */
    DATA_SOURCE("dataSource"),

    /**
     * The key of a higher-level data record in a hierarchical data structure of
     * 1:n relationships, which are stored from bottom to top.
     */
    HIGHERLEVEL_IDENTIFIER("higherlevelIdentifier"),

    /**
     * Key of the record in the source.
     */
    RECORD_IDENTIFIER("recordIdentifier"),

    /**
     * The title. It is used to form the author-title key or the title key.
     */
    TITLE("title");

    /**
     * With the logger, text can be written to a log file or to the console.
     */
    private static final Logger logger = LogManager.getLogger(SpecialField.class);

    /**
     * This character string defines how the special field is to be marked in
     * the rule set.
     */
    private final String mark;

    /**
     * Since this is an enum, the constructor cannot be called, except from Java
     * when building the class.
     *
     * @param mark
     *            how the special field is to be marked
     */
    private SpecialField(String mark) {
        this.mark = mark;
    }

    /**
     * Returns a string which defines how the special field is to be marked in
     * the rule set.
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
    public static Set<SpecialField> valuesOf(String marks) {
        Set<SpecialField> values = new HashSet<>(7);
        for (String mark : marks.split("\\s+", 0)) {
            for (SpecialField candidate : SpecialField.values()) {
                if (mark.equals(candidate.mark)) {
                    values.add(candidate);
                    break;
                }
            }
            logger.warn("Ruleset declares undefined field use '{}', must be one of: {}", mark,
                Arrays.stream(values()).map(SpecialField::toString).collect(Collectors.joining(", ")));
        }
        return values;
    }
}
