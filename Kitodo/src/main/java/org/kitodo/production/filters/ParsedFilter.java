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

package org.kitodo.production.filters;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.kitodo.production.enums.FilterString;

public class ParsedFilter {

    private final String plainFilter;

    /**
     * Default constructor.
     */
    public ParsedFilter(String plainFilter) {
        this.plainFilter = (plainFilter.startsWith("\"") ? "" : "\"") + plainFilter + (plainFilter.endsWith("\"") ? "" : "\"");
    }

    /**
     * Get plainFilter.
     *
     * @return value of plainFilter
     */
    public String getPlainFilter() {
        return plainFilter;
    }

    /**
     * Get category part of the filter.
     *
     * @return the category without surrounding quotation marks or leading dash
     */
    public String getCategory() {
        Pattern pattern = Pattern.compile("[a-zA-Z]+:");
        Matcher matcher = pattern.matcher(plainFilter.replaceFirst("^\"?-?", ""));
        if (matcher.find()) {
            return matcher.group();
        }
        return FilterString.PROCESS.getFilterEnglish();
    }

    /**
     * Get value part of the filter.
     *
     * @return value without surrounding quotation marks
     */
    public String getValue() {
        Pattern pattern = Pattern.compile("(?<=:)[^\"]+");
        Matcher matcher = pattern.matcher(plainFilter);
        if (matcher.find()) {
            return matcher.group().replaceFirst("^ -?", "");
        }
        return plainFilter.replaceAll("^\"|\"$", "").replace(getCategory(), "");
    }

    /**
     * Get whether this filter is negated.
     *
     * @return whether filter is negated
     */
    public boolean isNot() {
        Pattern pattern = Pattern.compile("^\"?-");
        Matcher matcher = pattern.matcher(plainFilter);
        return matcher.find();
    }

    @Override
    public boolean equals(Object o) {
        if (Objects.isNull(o) || !Objects.equals(getClass(), o.getClass())) {
            return false;
        }
        return plainFilter.equals(((ParsedFilter) o).getPlainFilter());
    }

    /**
     * hashCode method of current class.
     *
     * @see java.lang.Object#hashCode()
     * @return int
     */
    @Override
    public int hashCode() {
        return Objects.hash(plainFilter);
    }
}
