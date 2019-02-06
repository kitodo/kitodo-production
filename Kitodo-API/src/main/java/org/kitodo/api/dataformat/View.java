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

package org.kitodo.api.dataformat;

/**
 * A view on a media unit. The individual levels of the {@link Structure} refer
 * to {@code View}s on {@link MediaUnit}s. At the moment, each {@code View}
 * refers to exactly one {@code MediaUnit} as a whole.
 */
public class View {
    /**
     * Media unit in view.
     */
    private MediaUnit mediaUnit;

    /**
     * Returns the media unit in the view.
     * 
     * @return the media unit
     */
    public MediaUnit getMediaUnit() {
        return mediaUnit;
    }

    /**
     * Inserts a media unit into the view.
     * 
     * @param mediaUnit
     *            media unit to insert
     */
    public void setMediaUnit(MediaUnit mediaUnit) {
        this.mediaUnit = mediaUnit;
    }
}
