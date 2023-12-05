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
 * Metadata that have a function.
 */
public enum FunctionalMetadata {
    /**
     * The author’s last name. This is used in the application to generate the
     * author-title key.
     */
    AUTHOR_LAST_NAME("authorLastName"),

    /**
     * The number of the child, when creating a child process from a parent
     * process using the plus button. This will set the value on process
     * creation. The value is the current number of children of the parent
     * process plus one, i.e. it creates a one-based counting.
     */
    CHILD_COUND("childCount"),

    /**
     * The name of the data source from which the record was imported. This is
     * saved for later comparison of the data records.
     */
    DATA_SOURCE("dataSource"),

    /**
     * Display metadata as summary on Title Record Link tab when creating a new
     * process.
     */
    DISPLAY_SUMMARY("displaySummary"),

    /**
     * The key of a higher-level data record in a hierarchical data structure of
     * 1:n relationships, which are stored from bottom to top.
     */
    HIGHERLEVEL_IDENTIFIER("higherlevelIdentifier"),

    /**
     * Create a metadata entry with this key during process creation and write
     * the process title to it.
     */
    PROCESS_TITLE("processTitle"),

    /**
     * Key of the record in the source.
     */
    RECORD_IDENTIFIER("recordIdentifier"),

    /**
     * The title. It is used to form the author-title key or the title key.
     */
    TITLE("title"),

    /**
     * Document type metadata use for document classification during process import.
     */
    DOC_TYPE("docType"),

    /**
     * Used for predfined page label selection.
     */
    PAGE_LABEL("pageLabel");

    /**
     * With the logger, text can be written to a log file or to the console.
     */
    private static final Logger logger = LogManager.getLogger(FunctionalMetadata.class);

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
    private FunctionalMetadata(String mark) {
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
    private static boolean addEnumByMark(String mark, Set<FunctionalMetadata> to) {
        for (FunctionalMetadata candidate : FunctionalMetadata.values()) {
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
    public static Set<FunctionalMetadata> valuesOf(String marks) {
        Set<FunctionalMetadata> values = new TreeSet<>();
        for (String mark : marks.split("\\s+", 0)) {
            if (addEnumByMark(mark, values)) {
                continue;
            }
            logger.warn("Ruleset declares undefined division use '{}', must be one of: {}", mark,
                    Arrays.stream(values()).map(FunctionalMetadata::getMark).collect(Collectors.joining(", ")));
        }
        return values;
    }
}
