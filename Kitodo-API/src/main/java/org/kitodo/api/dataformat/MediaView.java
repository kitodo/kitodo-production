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
 * A view on a physical division. The individual levels of the {@link LogicalDivision} refer to {@code View}s on
 * {@link PhysicalDivision}s. At the moment, each {@code View} refers to exactly one {@code PhysicalDivision} as a
 * whole.
 */
public class MediaView extends View {

    /**
     * Starting point of a view on time-based media (sound, video). {@code null} if not applicable.
     */
    private String begin;

    public MediaView(String begin) {
        this.begin = begin;
    }

    public String getBegin() {
        return begin;
    }

}
