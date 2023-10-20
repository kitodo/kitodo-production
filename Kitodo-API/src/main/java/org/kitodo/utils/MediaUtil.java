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

public class MediaUtil {

    public static final String MIME_TYPE_AUDIO_PREFIX = "audio";

    public static final String MIME_TYPE_VIDEO_PREFIX = "video";

    /**
     * Private constructor to hide the implicit public one.
     */
    private MediaUtil() {

    }

    public static boolean isAudioOrVideo(String mimeType) {
        return isAudio(mimeType) || isVideo(mimeType);
    }

    public static boolean isAudio(String mimeType) {
        return mimeType.startsWith(MIME_TYPE_AUDIO_PREFIX);
    }

    public static boolean isVideo(String mimeType) {
        return mimeType.startsWith(MIME_TYPE_VIDEO_PREFIX);
    }

}
