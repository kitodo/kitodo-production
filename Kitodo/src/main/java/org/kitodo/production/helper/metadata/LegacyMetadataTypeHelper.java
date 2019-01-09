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

package org.kitodo.production.helper.metadata;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewInterface;
import org.kitodo.api.ugh.MetadataTypeInterface;

/**
 * Connects a legacy meta-data type to a key view. This is a soldering class to
 * keep legacy code operational which is about to be removed. Do not use this
 * class.
 */
public class LegacyMetadataTypeHelper implements MetadataTypeInterface {
    private static final Logger logger = LogManager.getLogger(LegacyMetadataTypeHelper.class);

    /**
     * A representative for a special legacy meta-data type to read and write
     * the METS ORDER attribute.
     */
    public static final MetadataTypeInterface SPECIAL_TYPE_ORDER = new MetadataTypeInterface() {
        @Override
        public Map<String, String> getAllLanguages() {
            throw andLog(new UnsupportedOperationException("Not yet implemented"));
        }

        @Override
        public boolean isPerson() {
            throw andLog(new UnsupportedOperationException("Not yet implemented"));
        }

        @Override
        public String getLanguage(String language) {
            return "de".equals(language) ? "physische Seitenzahl (Imagenummer)" : "physical page number";
        }

        @Override
        public String getName() {
            return "physPageNumber";
        }

        @Override
        public String getNum() {
            throw andLog(new UnsupportedOperationException("Not yet implemented"));
        }

        @Override
        public void setAllLanguages(Map<String, String> labels) {
            throw andLog(new UnsupportedOperationException("Not yet implemented"));
        }

        @Override
        public void setIdentifier(boolean identifier) {
            throw andLog(new UnsupportedOperationException("Not yet implemented"));
        }

        @Override
        public void setPerson(boolean person) {
            throw andLog(new UnsupportedOperationException("Not yet implemented"));
        }

        @Override
        public void setName(String name) {
            throw andLog(new UnsupportedOperationException("Not yet implemented"));
        }

        @Override
        public void setNum(String quantityRestriction) {
            throw andLog(new UnsupportedOperationException("Not yet implemented"));
        }
    };

    /**
     * A representative for a special legacy meta-data type to read and write
     * the METS ORDERLABEL attribute.
     */
    public static final MetadataTypeInterface SPECIAL_TYPE_ORDERLABEL = new MetadataTypeInterface() {
        @Override
        public Map<String, String> getAllLanguages() {
            throw andLog(new UnsupportedOperationException("Not yet implemented"));
        }

        @Override
        public boolean isPerson() {
            throw andLog(new UnsupportedOperationException("Not yet implemented"));
        }

        @Override
        public String getLanguage(String language) {
            return "de".equals(language) ? "logische Seitenzahl (gedruckte Zeitenzahl)" : "logical page number";
        }

        @Override
        public String getName() {
            return "logicalPageNumber";
        }

        @Override
        public String getNum() {
            throw andLog(new UnsupportedOperationException("Not yet implemented"));
        }

        @Override
        public void setAllLanguages(Map<String, String> labels) {
            throw andLog(new UnsupportedOperationException("Not yet implemented"));
        }

        @Override
        public void setIdentifier(boolean identifier) {
            throw andLog(new UnsupportedOperationException("Not yet implemented"));
        }

        @Override
        public void setPerson(boolean person) {
            throw andLog(new UnsupportedOperationException("Not yet implemented"));
        }

        @Override
        public void setName(String name) {
            throw andLog(new UnsupportedOperationException("Not yet implemented"));
        }

        @Override
        public void setNum(String quantityRestriction) {
            throw andLog(new UnsupportedOperationException("Not yet implemented"));
        }
    };

    /**
     * The key view accessed via this soldering class.
     */
    private MetadataViewInterface keyView;

    public LegacyMetadataTypeHelper(MetadataViewInterface keyView) {
        this.keyView = keyView;
    }

    @Override
    public Map<String, String> getAllLanguages() {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
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
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public void setAllLanguages(Map<String, String> labels) {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public void setIdentifier(boolean identifier) {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public void setPerson(boolean person) {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public void setName(String name) {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public void setNum(String quantityRestriction) {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    /**
     * This method generates a comprehensible log message in case something was
     * overlooked and one of the unimplemented methods should ever be called in
     * operation. The name was chosen deliberately short in order to keep the
     * calling code clear. This method must be implemented in every class
     * because it uses the logger tailored to the class.
     * 
     * @param exception
     *            created {@code UnsupportedOperationException}
     * @return the exception
     */
    private static RuntimeException andLog(UnsupportedOperationException exception) {
        StackTraceElement[] stackTrace = exception.getStackTrace();
        StringBuilder buffer = new StringBuilder(255);
        buffer.append(stackTrace[1].getClassName());
        buffer.append('.');
        buffer.append(stackTrace[1].getMethodName());
        buffer.append("()");
        if (stackTrace[1].getLineNumber() > -1) {
            buffer.append(" line ");
            buffer.append(stackTrace[1].getLineNumber());
        }
        buffer.append(" unexpectedly called unimplemented ");
        buffer.append(stackTrace[0].getMethodName());
        buffer.append("()");
        if (exception.getMessage() != null) {
            buffer.append(": ");
            buffer.append(exception.getMessage());
        }
        logger.error(buffer.toString());
        return exception;
    }
}
