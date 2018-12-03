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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.ugh.ContentFileInterface;

public class ContentFileJoint implements ContentFileInterface {
    private static final Logger logger = LogManager.getLogger(ContentFileJoint.class);

    @Override
    public String getLocation() {
        logger.log(Level.TRACE, "getLocation()");
        // TODO Auto-generated method stub
        return "";
    }

    @Override
    public void setLocation(String fileName) {
        logger.log(Level.TRACE, "setLocation(fileName: \"{}\")", fileName);
        // TODO Auto-generated method stub
    }

}
