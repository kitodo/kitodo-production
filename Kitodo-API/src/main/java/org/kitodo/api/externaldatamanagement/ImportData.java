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

import java.net.URI;
import java.util.HashMap;

public class ImportData {

    /** The data imported from the source as a map. */
    private HashMap<String, String> data;

    /** The imported data as a file. */
    private URI resultFileUri;

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
     * Gets the resultFileUri.
     * 
     * @return The resultFileUri.
     */
    public URI getResultFileUri() {
        return resultFileUri;
    }

    /**
     * Sets the resultFileUri.
     * 
     * @param resultFileUri
     *            The resultFileUri.
     */
    public void setResultFileUri(URI resultFileUri) {
        this.resultFileUri = resultFileUri;
    }
}
