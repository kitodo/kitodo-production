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

package org.kitodo.production.helper.metadata.pagination;

import java.util.Objects;

/**
 * A static piece of text as part of a pagination sequence. The text may either
 * appear on the front, back, or both sides of a sheet.
 */
public class StaticText implements Fragment {

    private HalfInteger increment;
    private Boolean page; // true: odd (left) page, false: even (right) page,
    // null: any page
    private String value;

    /**
     * Creates a static text that is used on odd or even pages only.
     *
     * @param value
     *            text string
     * @param odd
     *            if true, the text is printed for odd pages only, else for even
     *            pages only, null for both pages
     */
    StaticText(String value, Boolean odd) {
        this.value = value;
        this.page = odd;
    }

    @Override
    public String format(HalfInteger value) {
        if (page == null || page == value.isHalf()) {
            return this.value;
        } else {
            return "";
        }
    }

    @Override
    public HalfInteger getIncrement() {
        return increment;
    }

    @Override
    public Integer getInitialValue() {
        return null;
    }

    @Override
    public void setIncrement(HalfInteger increment) {
        this.increment = increment;

    }

    @Override
    public String toString() {
        return '"' + value + "\" (" + (Objects.nonNull(increment) ? increment : "default") + (page != null ? ", "
                + page : "") + ")";
    }

}
