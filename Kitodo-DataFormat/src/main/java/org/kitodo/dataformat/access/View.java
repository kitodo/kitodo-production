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

package org.kitodo.dataformat.access;

import org.kitodo.api.dataformat.mets.AreaXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.FileXmlElementAccessInterface;

/**
 * A view on a media unit. The individual levels of the {@link Structure} refer
 * to {@code View}s on {@link MediaUnit}s. At the moment, each {@code View}
 * refers to exactly one {@code MediaUnit} as a whole. This concept level has
 * been added here in order to be able to expand it in the future in order to be
 * able to refer to individual parts of a {@code MediaUnit}.
 */
public class View implements AreaXmlElementAccessInterface {
    /**
     * Media unit in view.
     */
    private MediaUnit mediaUnit;

    /**
     * Creates a new view. This constructor can be called via the service loader
     * to create a new view.
     */
    public View() {
    }

    /**
     * Creates a new view that points to a media device. This constructor is
     * called module-internally when loading a METS file.
     * 
     * @param mediaUnit
     *            media unit in view
     */
    View(MediaUnit mediaUnit) {
        this.mediaUnit = mediaUnit;
    }

    /**
     * Returns the media unit in the view.
     * 
     * @return the media unit
     */
    @Override
    public MediaUnit getFile() {
        return mediaUnit;
    }

    /**
     * Inserts a media unit into the view.
     * 
     * @param mediaUnit
     *            media unit to insert
     */
    @Override
    public void setFile(FileXmlElementAccessInterface mediaUnit) {
        this.mediaUnit = (MediaUnit) mediaUnit;
    }
}
