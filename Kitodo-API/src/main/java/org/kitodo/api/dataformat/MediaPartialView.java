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

public class MediaPartialView extends View {

    private String begin;

    private String extent;

    public MediaPartialView(String begin) {
        this.begin = begin;
    }

    public MediaPartialView(String begin, String extent) {
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
        if (o == null || !getClass().isInstance(o)) {
            return false;
        }

        MediaPartialView mediaPartialView = (MediaPartialView) o;

        // prevent endless loop and not calling super.equals
        if (Objects.isNull(getPhysicalDivision()) && Objects.nonNull(
                mediaPartialView.getPhysicalDivision()) || Objects.nonNull(getPhysicalDivision()) && Objects.isNull(
                mediaPartialView.getPhysicalDivision()) || getPhysicalDivision().hashCode() != mediaPartialView.getPhysicalDivision()
                .hashCode()) {
            return false;
        }

        return (Objects.isNull(begin) && Objects.isNull(mediaPartialView.begin)) || begin.equals(
                mediaPartialView.getBegin());
    }


}
