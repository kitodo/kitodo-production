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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataformat.mets.FileXmlElementAccessInterface;
import org.kitodo.helper.metadata.LegacyDocStructHelperInterface;

/**
 * Connects a legacy file set its corresponding doc struct to a media units
 * list. This is a soldering class to keep legacy code operational which is
 * about to be removed. Do not use this class.
 */

public class LegacyFileSetDocStructHelper implements LegacyDocStructHelperInterface {
    private static final Logger logger = LogManager.getLogger(LegacyFileSetDocStructHelper.class);

    /**
     * The media units list accessed via this soldering class.
     */
    private List<FileXmlElementAccessInterface> mediaUnits;

    @Deprecated
    public LegacyFileSetDocStructHelper(List<FileXmlElementAccessInterface> mediaUnits) {
        this.mediaUnits = mediaUnits;
    }

    @Deprecated
    public void addFile(LegacyContentFileHelper contentFile) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Deprecated
    public Iterable<LegacyContentFileHelper> getAllFiles() {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Deprecated
    public void removeFile(LegacyContentFileHelper contentFile) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    @Deprecated
    public void addChild(LegacyDocStructHelperInterface child) {
        mediaUnits.add(((LegacyInnerPhysicalDocStructHelper) child).getMediaUnit());
    }

    @Override
    @Deprecated
    public void addChild(Integer index, LegacyDocStructHelperInterface child) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    @Deprecated
    public void addContentFile(LegacyContentFileHelper contentFile) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    @Deprecated
    public void addMetadata(LegacyMetadataHelper metadata) {
        /*
         * Legacy code tries to add (empty) meta-data entries here. I guess this
         * is a bug.
         */
    }

    @Deprecated
    public LegacyDocStructHelperInterface addMetadata(String metadataType, String value) {
        // TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    @Deprecated
    public LegacyReferenceHelper addReferenceTo(LegacyDocStructHelperInterface docStruct, String type) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Deprecated
    public LegacyDocStructHelperInterface copy(boolean copyMetaData, Boolean recursive) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Deprecated
    public LegacyDocStructHelperInterface createChild(String docStructType,
            LegacyMetsModsDigitalDocumentHelper digitalDocument,
            LegacyPrefsHelper prefs) {

        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    @Deprecated
    public void deleteUnusedPersonsAndMetadata() {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    @Deprecated
    public List<LegacyMetadataTypeHelper> getAddableMetadataTypes() {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    @Deprecated
    public List<LegacyDocStructHelperInterface> getAllChildren() {
        List<LegacyDocStructHelperInterface> result = new ArrayList<>(mediaUnits.size());
        for (FileXmlElementAccessInterface mediaUnit : mediaUnits) {
            result.add(new LegacyInnerPhysicalDocStructHelper(mediaUnit));
        }
        return result;
    }

    @Override
    @Deprecated
    public List<LegacyDocStructHelperInterface> getAllChildrenByTypeAndMetadataType(String page, String asterisk) {
        List<LegacyDocStructHelperInterface> result = new ArrayList<>(mediaUnits.size());
        for (FileXmlElementAccessInterface mediaUnit : mediaUnits) {
            result.add(new LegacyInnerPhysicalDocStructHelper(mediaUnit));
        }
        return result;
    }

    @Deprecated
    public List<LegacyContentFileHelper> getAllContentFiles() {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Deprecated
    public List<LegacyReferenceHelper> getAllFromReferences() {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Deprecated
    public List<LegacyMetadataHelper> getAllIdentifierMetadata() {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    @Deprecated
    public List<LegacyMetadataHelper> getAllMetadata() {
        return Collections.emptyList();
    }

    @Override
    @Deprecated
    public List<? extends LegacyMetadataHelper> getAllMetadataByType(LegacyMetadataTypeHelper metadataType) {
        return Collections.emptyList();
    }

    @Override
    @Deprecated
    public List<LegacyReferenceHelper> getAllReferences(String direction) {
        /*
         * Although the method is called because the same loop is used for
         * logical and physical structure elements, it must come back empty.
         */
        return Collections.emptyList();
    }

    @Override
    @Deprecated
    public Collection<LegacyReferenceHelper> getAllToReferences() {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    @Deprecated
    public Collection<LegacyReferenceHelper> getAllToReferences(String type) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Deprecated
    public Object getAllVisibleMetadata() {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    @Deprecated
    public String getAnchorClass() {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Deprecated
    public LegacyDocStructHelperInterface getChild(String type, String identifierField, String identifier) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    @Deprecated
    public List<LegacyMetadataTypeHelper> getDisplayMetadataTypes() {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    @Deprecated
    public String getImageName() {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Deprecated
    public LegacyDocStructHelperInterface getNextChild(LegacyDocStructHelperInterface predecessor) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    @Deprecated
    public LegacyDocStructHelperInterface getParent() {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    @Deprecated
    public List<LegacyMetadataTypeHelper> getPossibleMetadataTypes() {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    @Deprecated
    public LegacyLogicalDocStructTypeHelper getDocStructType() {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    /**
     * This method is not part of the interface, but the JSF code digs in the
     * depths of the UGH and uses it on the guts.
     * 
     * @return Method delegated to {@link #getDocStructType()}
     */
    @Deprecated
    public LegacyLogicalDocStructTypeHelper getType() {
        StackTraceElement[] stackTrace = new RuntimeException().getStackTrace();
        logger.log(Level.WARN, "Method {}.{}() invokes {}.{}(), bypassing the interface!", stackTrace[1].getClassName(),
            stackTrace[1].getMethodName(), stackTrace[0].getClassName(), stackTrace[0].getMethodName());
        return getDocStructType();
    }

    @Override
    @Deprecated
    public boolean isDocStructTypeAllowedAsChild(LegacyLogicalDocStructTypeHelper type) {
        // TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    @Deprecated
    // TODO remove
    public void removeChild(LegacyDocStructHelperInterface docStruct) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Deprecated
    public void removeContentFile(LegacyContentFileHelper contentFile) {
        // TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    @Deprecated
    public void removeMetadata(LegacyMetadataHelper metaDatum) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    @Deprecated
    public void removeReferenceTo(LegacyDocStructHelperInterface target) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Deprecated
    public void setImageName(String imageName) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Deprecated
    public void setType(LegacyLogicalDocStructTypeHelper docStructType) {
        //TODO remove
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
