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
import org.kitodo.api.dataeditor.rulesetmanagement.Domain;
import org.kitodo.api.dataformat.mets.MetadataXmlElementAccessInterface;

/**
 * Represents a legacy metadata. This is a soldering class to keep legacy code
 * operational which is about to be removed. Do not use this class.
 */
public class LegacyMetadataHelper {
    private static final Logger logger = LogManager.getLogger(LegacyMetadataHelper.class);

    /**
     * The legacy type of the legacy metadata.
     */
    private LegacyMetadataTypeHelper type;

    /**
     * The value of the legacy metadata.
     */
    private String value;

    /**
     * The legacy doc struct of the legacy metadata.
     */
    private LegacyInnerPhysicalDocStructHelper legacyInnerPhysicalDocStructHelper;

    private BindingSaveInterface bindingSaveInterface;

    private MetadataXmlElementAccessInterface binding;

    private Domain domain;

    LegacyMetadataHelper(LegacyInnerPhysicalDocStructHelper legacyInnerPhysicalDocStructHelper,
            LegacyMetadataTypeHelper type, String value) {
        this.type = type;
        this.value = value;
        this.legacyInnerPhysicalDocStructHelper = legacyInnerPhysicalDocStructHelper;
    }

    public LegacyMetadataHelper(LegacyMetadataTypeHelper type) {
        this.type = type;
        this.value = "";
    }

    public MetadataXmlElementAccessInterface getBinding() {
        return binding;
    }

    public LegacyInnerPhysicalDocStructHelper getDocStruct() {
        return legacyInnerPhysicalDocStructHelper;
    }

    public Domain getDomain() {
        return domain;
    }

    public LegacyMetadataTypeHelper getMetadataType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    /**
     * This allows the meta-data to be saved.
     */
    public void saveToBinding() {
        bindingSaveInterface.saveMetadata(this);
    }

    /**
     * Sets the binding to a meta-data XML access interface through a binding
     * save interface. This is needed so that the meta-data in its container can
     * automatically save itself if its value is subsequently changed. In fact,
     * the value may be, aside from the value of a meta-data entry, the value of
     * a field of the container, which makes the matter a bit unwieldy.
     * 
     * @param bsi
     *            thee binding save interface via which the meta-data can
     *            automatically save itself afterwards
     * @param binding
     *            the meta-data entry where the value should be stored, if
     *            applicable
     * @param domain
     *            the domain where the meta-data entry is stored
     */
    public void setBinding(BindingSaveInterface bsi, MetadataXmlElementAccessInterface binding, Domain domain) {
        this.bindingSaveInterface = bsi;
        this.binding = binding;
        this.domain = domain;
    }

    /**
     * Sets the document structure entity to which this object belongs to.
     *
     * @param docStruct
     *            document structure entity to which this object belongs
     */
    public void setDocStruct(LegacyDocStructHelperInterface docStruct) {
        if (docStruct instanceof LegacyInnerPhysicalDocStructHelper) {
            this.legacyInnerPhysicalDocStructHelper = (LegacyInnerPhysicalDocStructHelper) docStruct;
        }
    }

    public void setType(LegacyMetadataTypeHelper metadataType) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    public void setStringValue(String value) {
        this.value = value;
        if (bindingSaveInterface != null) {
            saveToBinding();
        }
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
