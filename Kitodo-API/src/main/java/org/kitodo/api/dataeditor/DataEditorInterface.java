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

package org.kitodo.api.dataeditor;

import java.nio.file.Path;

public interface DataEditorInterface {

    /**
     *
     * @param xmlFilePath The Path to the xml file to read.
     */
    public void readData(Path xmlFilePath);

    /**
     *
     * @param xmlFilePath The Path to the xml file to edit.
     * @param rulesetFilePath The Path to the rulesetFilePath.
     * @return true, if editing was succesfull, false otherwise.
     */
    public boolean editData(Path xmlFilePath, Path rulesetFilePath);

}
