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
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.ugh.DocStructTypeInterface;
import org.kitodo.api.ugh.MetadataTypeInterface;
import org.kitodo.api.ugh.PrefsInterface;
import org.kitodo.api.ugh.exceptions.PreferencesException;

public class PrefsJoint implements PrefsInterface {
    private static final Logger logger = LogManager.getLogger(PrefsJoint.class);

    @Override
    public List<DocStructTypeInterface> getAllDocStructTypes() {
        logger.log(Level.TRACE, "getAllDocStructTypes()");
        // TODO Auto-generated method stub
        return Collections.emptyList();
    }

    @Override
    public DocStructTypeInterface getDocStrctTypeByName(String identifier) {
        logger.log(Level.TRACE, "getDocStrctTypeByName(identifier: \"{}\")", identifier);
        // TODO Auto-generated method stub
        return new DocStructTypeJoint();
    }

    @Override
    public MetadataTypeInterface getMetadataTypeByName(String identifier) {
        logger.log(Level.TRACE, "getMetadataTypeByName(identifier: \"{}\")", identifier);
        // TODO Auto-generated method stub
        return new MetadataTypeJoint();
    }

    @Override
    public void loadPrefs(String fileName) throws PreferencesException {
        logger.log(Level.TRACE, "loadPrefs(fileName: {})", fileName);
        // TODO Auto-generated method stub
    }

}
