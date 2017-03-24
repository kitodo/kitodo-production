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

package org.kitodo.api.externaldatamanagement;

import java.io.File;
import java.util.HashMap;

public class ImportData {

    /** The data imported from the source as a map. */
    private HashMap<String, String> data;

    /** The imported data as a file. */
    private File resultFile;

    /**
     * Gets the data.
     * 
     * @return The data.
     */
    public HashMap<String, String> getData() {
        return data;
    }

    /**
     * Sets the data.
     * 
     * @param data
     *            The data.
     */
    public void setData(HashMap<String, String> data) {
        this.data = data;
    }

    /**
     * Gets the resultFile.
     * 
     * @return The resultFile.
     */
    public File getResultFile() {
        return resultFile;
    }

    /**
     * Sets the resultFile.
     * 
     * @param resultFile
     *            The resultFile.
     */
    public void setResultFile(File resultFile) {
        this.resultFile = resultFile;
    }
}
