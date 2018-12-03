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
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.ugh.MetadataGroupTypeInterface;
import org.kitodo.api.ugh.MetadataTypeInterface;

public class MetadataGroupTypeJoint implements MetadataGroupTypeInterface {
    private static final Logger logger = LogManager.getLogger(MetadataGroupTypeJoint.class);

    @Override
    public void addMetadataType(MetadataTypeInterface metadataType) {
        logger.log(Level.TRACE, "addMetadataType(metadataType: {})", metadataType);
        // TODO Auto-generated method stub
    }

    @Override
    public Map<String, String> getAllLanguages() {
        logger.log(Level.TRACE, "getAllLanguages()");
        // TODO Auto-generated method stub
        return Collections.emptyMap();
    }

    @Override
    public String getLanguage(String language) {
        logger.log(Level.TRACE, "getLanguage(language: \"{}\")", language);
        // TODO Auto-generated method stub
        return "";
    }

    @Override
    public List<MetadataTypeInterface> getMetadataTypeList() {
        logger.log(Level.TRACE, "getMetadataTypeList()");
        // TODO Auto-generated method stub
        return Collections.emptyList();
    }

    @Override
    public String getName() {
        logger.log(Level.TRACE, "getName()");
        // TODO Auto-generated method stub
        return "";
    }

    @Override
    public void setAllLanguages(Map<String, String> allLanguages) {
        logger.log(Level.TRACE, "setAllLanguages(allLanguages: {})", allLanguages);
        // TODO Auto-generated method stub

    }

    @Override
    public void setName(String name) {
        logger.log(Level.TRACE, "setName(name: \"{}\")", name);
        // TODO Auto-generated method stub

    }

    @Override
    public void setNum(String quantityRestriction) {
        logger.log(Level.TRACE, "setNum(quantityRestriction: \"{}\")", quantityRestriction);
        // TODO Auto-generated method stub

    }

}
