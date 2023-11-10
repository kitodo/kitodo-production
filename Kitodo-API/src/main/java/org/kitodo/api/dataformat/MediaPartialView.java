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

import org.apache.commons.lang3.StringUtils;

public class MediaPartialView extends View {

    private String begin;

    private String extent;

    public MediaPartialView(String begin, String extent) {
        this.begin = begin;
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

    public boolean hasExtent() {
        return StringUtils.isNotEmpty(extent);
    }

    public void setExtent(String extent) {
        this.extent = extent;
    }
}
