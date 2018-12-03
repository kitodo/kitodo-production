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
import org.kitodo.api.ugh.DigitalDocumentInterface;
import org.kitodo.api.ugh.MetsModsInterface;
import org.kitodo.api.ugh.exceptions.PreferencesException;
import org.kitodo.api.ugh.exceptions.ReadException;
import org.kitodo.api.ugh.exceptions.WriteException;

public class MetsModsJoint implements MetsModsInterface {
    private static final Logger logger = LogManager.getLogger(MetsModsJoint.class);

    @Override
    public DigitalDocumentInterface getDigitalDocument() throws PreferencesException {
        logger.log(Level.TRACE, "getDigitalDocument()");
        // TODO Auto-generated method stub
        return new DigitalDocumentJoint();
    }

    @Override
    public void read(String path) throws ReadException {
        logger.log(Level.TRACE, "read(path: \"{}\")", path);
        // TODO Auto-generated method stub
    }

    @Override
    public void setDigitalDocument(DigitalDocumentInterface digitalDocument) {
        logger.log(Level.TRACE, "setDigitalDocument(digitalDocument: {})", digitalDocument);
        // TODO Auto-generated method stub
    }

    @Override
    public void write(String filename) throws PreferencesException, WriteException {
        logger.log(Level.TRACE, "write(filename: {})");
        // TODO Auto-generated method stub
    }
}
