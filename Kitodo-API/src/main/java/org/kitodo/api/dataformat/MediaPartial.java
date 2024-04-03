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
 * MediaPartial is part of a {@link PhysicalDivision} of type
 * {@link PhysicalDivision#TYPE_TRACK} and contains additional
 * information about the start and length.
 */
public class MediaPartial {

    private String begin;

    private String extent;

    /**
     * Constructs a media partial object.
     *
     * @param begin
     *         The begin as formatted time in form of
     *         {@link org.kitodo.production.helper.metadata.MediaPartialHelper#FORMATTED_TIME_PATTERN}
     */
    public MediaPartial(String begin) {
        this.begin = begin;
    }

    /**
     * Constructs a media partial object.
     *
     * @param begin
     *         The begin as formatted time in form of
     *         {@link org.kitodo.production.helper.metadata.MediaPartialHelper#FORMATTED_TIME_PATTERN}
     * @param extent
     *         The extent as formatted time in form of
     *         {@link org.kitodo.production.helper.metadata.MediaPartialHelper#FORMATTED_TIME_PATTERN}
     */
    public MediaPartial(String begin, String extent) {
        this(begin);
        this.extent = extent;
    }

    public void setBegin(String begin) {
        this.begin = begin;
    }

    public String getBegin() {
        return begin;
    }

    public String getExtent() {
        return extent;
    }

    public void setExtent(String extent) {
        this.extent = extent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!getClass().isInstance(o)) {
            return false;
        }

        MediaPartial mediaPartial = (MediaPartial) o;

        return Objects.equals(begin, mediaPartial.begin);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        return prime * super.hashCode() + Objects.hash(begin, extent);
    }

}
