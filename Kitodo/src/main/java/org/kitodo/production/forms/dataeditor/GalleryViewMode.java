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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.kitodo.production.helper.Helper;

/**
 * The possibilities of the gallery view.
 */
public enum GalleryViewMode {
    /**
     * Structured gallery view. For each outline structure, the media contained
     * therein are displayed in a strip.
     */
    LIST("dataEditor.galleryStructuredView"),

    /**
     * Large media view with scrollbar with all images.
     */
    PREVIEW("dataEditor.galleryDetailView");

    private final String messageKey;

    /**
     * Constructor with message key.
     *
     * @param messageKey message key of gallery view mode
     */
    GalleryViewMode(String messageKey) {
        this.messageKey = messageKey;
    }

    /**
     * Get translation of this gallery view mode.
     *
     * @return translation of this gallery view mode
     */
    public String getTranslation() {
        return Helper.getTranslation(messageKey);
    }

    /**
     * Return GalleryViewMode by name.
     * @param name enum name of requested GalleryViewMode.
     * @return GalleryViewMode
     */
    public static GalleryViewMode getByName(String name) {
        for (GalleryViewMode viewMode : values()) {
            if (viewMode.name().equals(name)) {
                return viewMode;
            }
        }
        return LIST;
    }

    /**
     * Get list of GalleryViewModes as Strings.
     *
     * @return list of Strings
     */
    public static List<String> getGalleryViewModes() {
        return Arrays.stream(values()).map(GalleryViewMode::name).collect(Collectors.toList());
    }
}
