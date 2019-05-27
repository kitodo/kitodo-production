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

package org.kitodo.production.forms.dataeditor;

/**
 * The possibilities of the gallery view.
 */
public enum GalleryViewMode {
    /**
     * Classic gallery view. Just as in a graphical shell, all media in a
     * workpiece is displayed in an overview with thumbnails (if enabled).
     */
    GRID,

    /**
     * Structured gallery view. For each outline structure, the media contained
     * therein are displayed in a strip.
     */
    LIST,

    /**
     * Large media view with scrollbar with all images.
     */
    PREVIEW
}
