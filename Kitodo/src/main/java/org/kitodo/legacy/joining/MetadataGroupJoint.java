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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.ugh.MetadataGroupInterface;
import org.kitodo.api.ugh.MetadataGroupTypeInterface;
import org.kitodo.api.ugh.MetadataInterface;
import org.kitodo.api.ugh.PersonInterface;

public class MetadataGroupJoint implements MetadataGroupInterface {
    private static final Logger logger = LogManager.getLogger(MetadataGroupJoint.class);

    @Override
    public void addMetadata(MetadataInterface metadata) {
        logger.log(Level.TRACE, "addMetadata(metadata: {})", metadata);
        // TODO Auto-generated method stub

    }

    @Override
    public void addPerson(PersonInterface person) {
        logger.log(Level.TRACE, "addPerson(person: {})", person);
        // TODO Auto-generated method stub

    }

    @Override
    public List<MetadataInterface> getMetadataByType(String type) {
        logger.log(Level.TRACE, "getMetadataByType(type: \"{}\")", type);
        // TODO Auto-generated method stub
        return Collections.emptyList();
    }

    @Override
    public List<MetadataInterface> getMetadataList() {
        logger.log(Level.TRACE, "getMetadataList()");
        // TODO Auto-generated method stub
        return Collections.emptyList();
    }

    @Override
    public Iterable<PersonInterface> getPersonByType(String type) {
        logger.log(Level.TRACE, "getPersonByType(type: \"{}\")", type);
        // TODO Auto-generated method stub
        return Collections.emptyList();
    }

    @Override
    public Collection<PersonInterface> getPersonList() {
        logger.log(Level.TRACE, "getPersonList()");
        // TODO Auto-generated method stub
        return Collections.emptyList();
    }

    @Override
    public MetadataGroupTypeInterface getMetadataGroupType() {
        logger.log(Level.TRACE, "getMetadataGroupType()");
        // TODO Auto-generated method stub
        return new MetadataGroupTypeJoint();
    }
}
