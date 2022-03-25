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

package org.kitodo.production.forms;

public class CsvCell {

    private String value;

    /**
     * Empty standard constructor.
     */
    public CsvCell() {
    }

    /**
     * Constructor setting value of this CsvCell.
     *
     * @param value value of CsvCell
     */
    public CsvCell(String value) {
        this.value = value;
    }

    /**
     * Get value.
     *
     * @return value of value
     */
    public String getValue() {
        return value;
    }

    /**
     * Set value.
     *
     * @param value as java.lang.String
     */
    public void setValue(String value) {
        this.value = value;
    }
}
