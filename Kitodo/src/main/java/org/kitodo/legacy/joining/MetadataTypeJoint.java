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
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewInterface;
import org.kitodo.api.ugh.MetadataTypeInterface;

public class MetadataTypeJoint implements MetadataTypeInterface {
    private static final Logger logger = LogManager.getLogger(MetadataTypeJoint.class);

    public static final MetadataTypeInterface SPECIAL_TYPE_ORDER = new MetadataTypeInterface() {
        @Override
        public Map<String, String> getAllLanguages() {
            throw new UnsupportedOperationException("Order type needs special treatment");
        }

        @Override
        public boolean isPerson() {
            throw new UnsupportedOperationException("Order type needs special treatment");
        }

        @Override
        public String getLanguage(String language) {
            throw new UnsupportedOperationException("Order type needs special treatment");
        }

        @Override
        public String getName() {
            return "physPageNumber";
        }

        @Override
        public String getNum() {
            throw new UnsupportedOperationException("Order type needs special treatment");
        }

        @Override
        public void setAllLanguages(Map<String, String> labels) {
            throw new UnsupportedOperationException("Order type needs special treatment");
        }

        @Override
        public void setIdentifier(boolean identifier) {
            throw new UnsupportedOperationException("Order type needs special treatment");
        }

        @Override
        public void setPerson(boolean person) {
            throw new UnsupportedOperationException("Order type needs special treatment");
        }

        @Override
        public void setName(String name) {
            throw new UnsupportedOperationException("Order type needs special treatment");
        }

        @Override
        public void setNum(String quantityRestriction) {
            throw new UnsupportedOperationException("Order type needs special treatment");
        }
    };

    public static final MetadataTypeInterface SPECIAL_TYPE_ORDERLABEL = new MetadataTypeInterface() {
        @Override
        public Map<String, String> getAllLanguages() {
            throw new UnsupportedOperationException("Orderlabel type needs special treatment");
        }

        @Override
        public boolean isPerson() {
            throw new UnsupportedOperationException("Orderlabel type needs special treatment");
        }

        @Override
        public String getLanguage(String language) {
            throw new UnsupportedOperationException("Orderlabel type needs special treatment");
        }

        @Override
        public String getName() {
            return "logicalPageNumber";
        }

        @Override
        public String getNum() {
            throw new UnsupportedOperationException("Orderlabel type needs special treatment");
        }

        @Override
        public void setAllLanguages(Map<String, String> labels) {
            throw new UnsupportedOperationException("Orderlabel type needs special treatment");
        }

        @Override
        public void setIdentifier(boolean identifier) {
            throw new UnsupportedOperationException("Orderlabel type needs special treatment");
        }

        @Override
        public void setPerson(boolean person) {
            throw new UnsupportedOperationException("Orderlabel type needs special treatment");
        }

        @Override
        public void setName(String name) {
            throw new UnsupportedOperationException("Orderlabel type needs special treatment");
        }

        @Override
        public void setNum(String quantityRestriction) {
            throw new UnsupportedOperationException("Orderlabel type needs special treatment");
        }
    };

    private MetadataViewInterface keyView;

    public MetadataTypeJoint(MetadataViewInterface keyView) {
        this.keyView = keyView;
    }

    @Override
    public Map<String, String> getAllLanguages() {
        logger.log(Level.TRACE, "getAllLanguages()");
        // TODO Auto-generated method stub
        return Collections.emptyMap();
    }

    @Override
    public boolean isPerson() {
        return false;
    }

    @Override
    public String getLanguage(String language) {
        return keyView.getLabel();
    }

    @Override
    public String getName() {
        return keyView.getId();
    }

    @Override
    public String getNum() {
        logger.log(Level.TRACE, "getNum()");
        // TODO Auto-generated method stub
        return "";
    }

    @Override
    public void setAllLanguages(Map<String, String> labels) {
        logger.log(Level.TRACE, "setAllLanguages(labels: {})", labels);
        // TODO Auto-generated method stub
    }

    @Override
    public void setIdentifier(boolean identifier) {
        logger.log(Level.TRACE, "setIdentifier(identifier: {})", identifier);
        // TODO Auto-generated method stub
    }

    @Override
    public void setPerson(boolean person) {
        logger.log(Level.TRACE, "setPerson(person: {})", person);
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
