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

package org.kitodo.production.helper.metadata.legacytypeimplementations;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.ugh.MetadataInterface;
import org.kitodo.api.ugh.MetadataTypeInterface;

/**
 * Represents a legacy metadata. This is a soldering class to keep legacy code
 * operational which is about to be removed. Do not use this class.
 */
public class LegacyMetadataHelper implements MetadataInterface {
    private static final Logger logger = LogManager.getLogger(LegacyMetadataHelper.class);

    /**
     * The legacy type of the legacy metadata.
     */
    private MetadataTypeInterface type;

    /**
     * The value of the legacy metadata.
     */
    private String value;

    /**
     * The legacy doc struct of the legacy metadata.
     */
    private LegacyInnerPhysicalDocStructHelper legacyInnerPhysicalDocStructHelper;

    LegacyMetadataHelper(LegacyInnerPhysicalDocStructHelper legacyInnerPhysicalDocStructHelper,
            MetadataTypeInterface type, String value) {
        this.type = type;
        this.value = value;
        this.legacyInnerPhysicalDocStructHelper = legacyInnerPhysicalDocStructHelper;
    }

    public LegacyMetadataHelper(MetadataTypeInterface type) {
        this.type = type;
        this.value = "";
    }

    @Override
    public LegacyInnerPhysicalDocStructHelper getDocStruct() {
        return legacyInnerPhysicalDocStructHelper;
    }

    @Override
    public MetadataTypeInterface getMetadataType() {
        return type;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setDocStruct(LegacyDocStructHelperInterface docStruct) {
        if (docStruct instanceof LegacyInnerPhysicalDocStructHelper) {
            this.legacyInnerPhysicalDocStructHelper = (LegacyInnerPhysicalDocStructHelper) docStruct;
        }
    }

    @Override
    public void setType(MetadataTypeInterface metadataType) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public void setStringValue(String value) {
        this.value = value;
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
