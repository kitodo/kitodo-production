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

public class DocStructTypeJoint implements DocStructTypeInterface {
    private static final Logger logger = LogManager.getLogger(DocStructTypeJoint.class);

    @Override
    public List<String> getAllAllowedDocStructTypes() {
        logger.log(Level.TRACE, "getAllAllowedDocStructTypes()");
        // TODO Auto-generated method stub
        return Collections.emptyList();
    }

    @Override
    public List<MetadataTypeInterface> getAllMetadataTypes() {
        logger.log(Level.TRACE, "getAllMetadataTypes()");
        // TODO Auto-generated method stub
        return Collections.emptyList();
    }

    @Override
    public String getAnchorClass() {
        logger.log(Level.TRACE, "getAnchorClass()");
        // TODO Auto-generated method stub
        return null; // muss null sein = keine
    }

    @Override
    public String getName() {
        logger.log(Level.TRACE, "getName()");
        // TODO Auto-generated method stub
        return "";
    }

    @Override
    public String getNameByLanguage(String language) {
        logger.log(Level.TRACE, "getNameByLanguage(language: \"{}\")", language);
        // TODO Auto-generated method stub
        return "";
    }

    @Override
    public String getNumberOfMetadataType(MetadataTypeInterface metadataType) {
        logger.log(Level.TRACE, "getNumberOfMetadataType(metadataType: {})", metadataType);
        // TODO Auto-generated method stub
        return "";
    }
}
