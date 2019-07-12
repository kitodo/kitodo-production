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

package org.kitodo.production.helper.metadata.legacytypeimplementations;

/**
 * Connects a legacy reference to a media unit. This is a soldering class to
 * keep legacy code operational which is about to be removed. Do not use this
 * class.
 */
public class LegacyReferenceHelper {

    /**
     * The soldering class containing the media unit accessed via this soldering
     * class.
     */
    private LegacyInnerPhysicalDocStructHelper target;

    @Deprecated
    public LegacyReferenceHelper(LegacyInnerPhysicalDocStructHelper target) {
        this.target = target;
    }
}
