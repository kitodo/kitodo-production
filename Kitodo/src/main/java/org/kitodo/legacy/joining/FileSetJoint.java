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

import java.util.Collections;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.ugh.ContentFileInterface;
import org.kitodo.api.ugh.FileSetInterface;
import org.kitodo.api.ugh.VirtualFileGroupInterface;

public class FileSetJoint implements FileSetInterface {
    private static final Logger logger = LogManager.getLogger(FileSetJoint.class);

    @Override
    public void addFile(ContentFileInterface contentFile) {
        logger.log(Level.TRACE, "addFile(contentFile: {})", contentFile);
        // TODO Auto-generated method stub
    }

    @Override
    public void addVirtualFileGroup(VirtualFileGroupInterface virtualFileGroup) {
        logger.log(Level.TRACE, "addVirtualFileGroup(virtualFileGroup: virtualFileGroup)", virtualFileGroup);
        // TODO Auto-generated method stub
    }

    @Override
    public Iterable<ContentFileInterface> getAllFiles() {
        logger.log(Level.TRACE, "getAllFiles()");
        // TODO Auto-generated method stub
        return Collections.emptyList();
    }

    @Override
    public void removeFile(ContentFileInterface contentFile) {
        logger.log(Level.TRACE, "removeFile(contentFile: {})", contentFile);
        // TODO Auto-generated method stub
    }

}
