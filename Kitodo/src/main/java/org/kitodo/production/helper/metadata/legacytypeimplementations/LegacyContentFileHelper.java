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

import java.io.File;
import java.net.URI;

import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.dataformat.MetsService;

/**
 * Connects a legacy content file to a media file. This is a soldering class to
 * keep legacy code operational which is about to be removed. Do not use this
 * class.
 */
public class LegacyContentFileHelper {

    private static final MetsService metsService = ServiceManager.getMetsService();

    /**
     * The media file accessed via this soldering class.
     */
    private URI mediaFile;

    @Deprecated
    public LegacyContentFileHelper() {
    }

    @Deprecated
    public String getLocation() {
        return String.valueOf(mediaFile);
    }

    @Deprecated
    public void setLocation(String fileName) {
        mediaFile = new File(fileName).toURI();
    }

    public URI getMediaFile() {
        return mediaFile;
    }
}
