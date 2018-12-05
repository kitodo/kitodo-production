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

package org.kitodo.legacy.joining;

import java.io.File;

import org.kitodo.api.dataformat.mets.FLocatXmlElementAccessInterface;
import org.kitodo.api.ugh.ContentFileInterface;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.dataformat.MetsService;

/**
 * Connects a content file to a media file.
 */
public class ContentFileJoint implements ContentFileInterface {

    private final ServiceManager serviceLoader = new ServiceManager();
    private final MetsService metsService = serviceLoader.getMetsService();

    private FLocatXmlElementAccessInterface mediaFile;

    public ContentFileJoint() {
        mediaFile = metsService.createFLocat();
    }

    @Override
    public String getLocation() {
        return mediaFile.getUri().toString();
    }

    @Override
    public void setLocation(String fileName) {
        mediaFile.setUri(new File(fileName).toURI());
    }

    public FLocatXmlElementAccessInterface getMediaFile() {
        return mediaFile;
    }

}
