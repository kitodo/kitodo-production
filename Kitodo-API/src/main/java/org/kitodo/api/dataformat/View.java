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

import java.util.Objects;

/**
 * A view on a media unit. The individual levels of the
 * {@link LogicalDivision} refer to {@code View}s on
 * {@link MediaUnit}s. At the moment, each {@code View} refers to exactly one
 * {@code MediaUnit} as a whole.
 */
public class View {

    /**
     * Creates a new view with a media unit.
     * 
     * @param mediaUnit
     *            media unit to set in the view
     * @return a new view with a media unit
     */
    public static View of(MediaUnit mediaUnit) {
        View view = new View();
        view.setMediaUnit(mediaUnit);
        return view;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        View view = (View) o;
        return Objects.equals(mediaUnit, view.mediaUnit);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int hashCode = 1;
        hashCode = prime * hashCode + ((mediaUnit == null) ? 0 : mediaUnit.hashCode());
        return hashCode;
    }
}
