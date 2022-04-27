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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CsvRecord {

    private List<CsvCell> csvCells;

    /**
     * Constructor creating CsvRecord instance with the given number of empty CsvCells.
     *
     * @param numberOfCells number of CsvCells to add to new CsvRecord
     */
    public CsvRecord(int numberOfCells) {
        this.csvCells = Stream.generate(CsvCell::new).limit(numberOfCells).collect(Collectors.toList());
    }

    /**
     * Constructor creating CsvRecord instance and set list of CsvCells.
     *
     * @param cells list of CsvCells added to new CsvRecord
     */
    public CsvRecord(List<CsvCell> cells) {
        this.csvCells = cells;
    }

    /**
     * Get csvCells.
     *
     * @return value of csvCells
     */
    public List<CsvCell> getCsvCells() {
        return csvCells;
    }

    /**
     * Set csvCells.
     *
     * @param csvCells as List of CsvCell
     */
    public void setCsvCells(List<CsvCell> csvCells) {
        this.csvCells = csvCells;
    }

}
