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

package org.kitodo.dataformat.service;

import org.kitodo.api.dataformat.mets.AreaXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.FileXmlElementAccessInterface;

public class View implements AreaXmlElementAccessInterface {

    private MediaUnit mediaUnit;

    public View() {
    }

    public View(MediaUnit mediaUnit) {
        this.mediaUnit = mediaUnit;
    }

    @Override
    public MediaUnit getFile() {
        return mediaUnit;
    }

    @Override
    public void setFile(FileXmlElementAccessInterface file) {
        this.mediaUnit = (MediaUnit) file;
    }
}
