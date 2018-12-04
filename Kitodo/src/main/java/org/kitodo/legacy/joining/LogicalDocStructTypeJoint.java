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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.api.ugh.DocStructTypeInterface;
import org.kitodo.api.ugh.MetadataTypeInterface;

public class LogicalDocStructTypeJoint implements DocStructTypeInterface {
    private static final Logger logger = LogManager.getLogger(LogicalDocStructTypeJoint.class);

    private StructuralElementViewInterface divisionView;

    public LogicalDocStructTypeJoint(StructuralElementViewInterface divisionView) {
        this.divisionView = divisionView;
    }

    @Override
    public List<String> getAllAllowedDocStructTypes() {
        return new ArrayList<>(divisionView.getAllowedSubstructuralElements().keySet());
    }

    @Override
    public List<MetadataTypeInterface> getAllMetadataTypes() {
        logger.log(Level.TRACE, "getAllMetadataTypes()");
        // TODO Auto-generated method stub
        return Collections.emptyList();
    }

    @Override
    public String getAnchorClass() {
        return null; // muss null sein = keine
    }

    @Override
    public String getName() {
        return divisionView.getId();
    }

    @Override
    public String getNameByLanguage(String language) {
        return divisionView.getLabel();
    }

    @Override
    public String getNumberOfMetadataType(MetadataTypeInterface metadataType) {
        logger.log(Level.TRACE, "getNumberOfMetadataType(metadataType: {})", metadataType);
        // TODO Auto-generated method stub
        return "";
    }
}
