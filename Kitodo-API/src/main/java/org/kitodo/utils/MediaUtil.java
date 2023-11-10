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

package org.kitodo.utils;

import java.util.Objects;

import org.kitodo.api.dataformat.PhysicalDivision;

public class MediaUtil {

    public static final String MIME_TYPE_AUDIO_PREFIX = "audio";
    public static final String MIME_TYPE_IMAGE_PREFIX = "image";
    public static final String MIME_TYPE_VIDEO_PREFIX = "video";

    /**
     * Private constructor to hide the implicit public one.
     */
    private MediaUtil() {

    }

    /**
     * Check if mime type starts with {@link org.kitodo.utils.MediaUtil#MIME_TYPE_AUDIO_PREFIX} or
     * {@link org.kitodo.utils.MediaUtil#MIME_TYPE_VIDEO_PREFIX}.
     *
     * @param mimeType
     *         The mime type to check
     * @return True if mime type starts with {@link org.kitodo.utils.MediaUtil#MIME_TYPE_AUDIO_PREFIX} or
     *         {@link org.kitodo.utils.MediaUtil#MIME_TYPE_VIDEO_PREFIX}.
     */
    public static boolean isAudioOrVideo(String mimeType) {
        return isAudio(mimeType) || isVideo(mimeType);
    }

    /**
     * Check if mime type starts with {@link org.kitodo.utils.MediaUtil#MIME_TYPE_AUDIO_PREFIX}.
     *
     * @param mimeType
     *         The mime type to check
     * @return True if mime type starts with {@link org.kitodo.utils.MediaUtil#MIME_TYPE_AUDIO_PREFIX}
     */
    public static boolean isAudio(String mimeType) {
        return Objects.nonNull(mimeType) && mimeType.startsWith(MIME_TYPE_AUDIO_PREFIX);
    }

    /**
     * Check if mime type starts with {@link org.kitodo.utils.MediaUtil#MIME_TYPE_IMAGE_PREFIX}.
     *
     * @param mimeType
     *         The mime type to check
     * @return True if mime type starts with {@link org.kitodo.utils.MediaUtil#MIME_TYPE_IMAGE_PREFIX}
     */
    public static boolean isImage(String mimeType) {
        return Objects.nonNull(mimeType) && mimeType.startsWith(MIME_TYPE_IMAGE_PREFIX);
    }

    /**
     * Check if mime type starts with {@link org.kitodo.utils.MediaUtil#MIME_TYPE_VIDEO_PREFIX}.
     *
     * @param mimeType
     *         The mime type to check
     * @return True if mime type starts with {@link org.kitodo.utils.MediaUtil#MIME_TYPE_VIDEO_PREFIX}
     */
    public static boolean isVideo(String mimeType) {
        return Objects.nonNull(mimeType) && mimeType.startsWith(MIME_TYPE_VIDEO_PREFIX);
    }

    /**
     * Get the type of {@link org.kitodo.api.dataformat.PhysicalDivision} by mime type.
     *
     * @param mimeType
     *         The mime type to get the physical division type for
     * @return The type of the {@link org.kitodo.api.dataformat.PhysicalDivision}
     */
    public static String getPhysicalDivisionTypeOfMimeType(String mimeType) {
        if (isImage(mimeType)) {
            return PhysicalDivision.TYPE_PAGE;
        }
        if (isAudioOrVideo(mimeType)) {
            return PhysicalDivision.TYPE_TRACK;
        }
        return PhysicalDivision.TYPE_OTHER;
    }

}
