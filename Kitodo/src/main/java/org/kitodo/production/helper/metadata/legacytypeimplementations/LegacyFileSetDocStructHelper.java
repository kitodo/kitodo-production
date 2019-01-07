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
import org.kitodo.api.ugh.DigitalDocumentInterface;
import org.kitodo.api.ugh.MetadataGroupInterface;
import org.kitodo.api.ugh.MetadataGroupTypeInterface;
import org.kitodo.api.ugh.PersonInterface;
import org.kitodo.api.ugh.PrefsInterface;
import org.kitodo.api.ugh.ReferenceInterface;
import org.kitodo.api.ugh.VirtualFileGroupInterface;
import org.kitodo.api.ugh.exceptions.ContentFileNotLinkedException;
import org.kitodo.api.ugh.exceptions.MetadataTypeNotAllowedException;
import org.kitodo.api.ugh.exceptions.TypeNotAllowedAsChildException;
import org.kitodo.api.ugh.exceptions.TypeNotAllowedForParentException;
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

    public LegacyFileSetDocStructHelper(List<FileXmlElementAccessInterface> mediaUnits) {
        this.mediaUnits = mediaUnits;
    }

    public void addFile(LegacyContentFileHelper contentFile) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    public void addVirtualFileGroup(VirtualFileGroupInterface virtualFileGroup) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    public Iterable<LegacyContentFileHelper> getAllFiles() {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    public void removeFile(LegacyContentFileHelper contentFile) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public void addChild(LegacyDocStructHelperInterface child) throws TypeNotAllowedAsChildException {
        mediaUnits.add(((LegacyInnerPhysicalDocStructHelper) child).getMediaUnit());
    }

    @Override
    public void addChild(Integer index, LegacyDocStructHelperInterface child) throws TypeNotAllowedAsChildException {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public void addContentFile(LegacyContentFileHelper contentFile) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public void addMetadata(LegacyMetadataHelper metadata) throws MetadataTypeNotAllowedException {
        /*
         * Legacy code tries to add (empty) meta-data entries here. I guess this
         * is a bug.
         */
    }

    public LegacyDocStructHelperInterface addMetadata(String metadataType, String value) throws MetadataTypeNotAllowedException {
        // TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    public void addMetadataGroup(MetadataGroupInterface metadataGroup) throws MetadataTypeNotAllowedException {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    public void addPerson(PersonInterface person) throws MetadataTypeNotAllowedException {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public ReferenceInterface addReferenceTo(LegacyDocStructHelperInterface docStruct, String type) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    public LegacyDocStructHelperInterface copy(boolean copyMetaData, Boolean recursive) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    public LegacyDocStructHelperInterface createChild(String docStructType, DigitalDocumentInterface digitalDocument,
            PrefsInterface prefs) throws TypeNotAllowedAsChildException, TypeNotAllowedForParentException {

        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public void deleteUnusedPersonsAndMetadata() {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    public List<MetadataGroupTypeInterface> getAddableMetadataGroupTypes() {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
<<<<<<< HEAD:Kitodo/src/main/java/org/kitodo/production/helper/metadata/legacytypeimplementations/LegacyFileSetDocStructHelper.java
    public List<MetadataTypeInterface> getAddableMetadataTypes() {
        //TODO remove
=======
    public List<LegacyMetadataTypeHelper> getAddableMetadataTypes() {
>>>>>>> Remove references to MetadataTypeInterface [not compilable]:Kitodo/src/main/java/org/kitodo/helper/metadata/LegacyFileSetDocStructHelper.java
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public List<LegacyDocStructHelperInterface> getAllChildren() {
        List<LegacyDocStructHelperInterface> result = new ArrayList<>(mediaUnits.size());
        for (FileXmlElementAccessInterface mediaUnit : mediaUnits) {
            result.add(new LegacyInnerPhysicalDocStructHelper(mediaUnit));
        }
        return result;
    }

    @Override
    public List<LegacyDocStructHelperInterface> getAllChildrenByTypeAndMetadataType(String page, String asterisk) {
        List<LegacyDocStructHelperInterface> result = new ArrayList<>(mediaUnits.size());
        for (FileXmlElementAccessInterface mediaUnit : mediaUnits) {
            result.add(new LegacyInnerPhysicalDocStructHelper(mediaUnit));
        }
        return result;
    }

    public List<LegacyContentFileHelper> getAllContentFiles() {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    public List<ReferenceInterface> getAllFromReferences() {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    public List<LegacyMetadataHelper> getAllIdentifierMetadata() {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public List<LegacyMetadataHelper> getAllMetadata() {
        return Collections.emptyList();
    }

    @Override
    public List<? extends LegacyMetadataHelper> getAllMetadataByType(LegacyMetadataTypeHelper metadataType) {
        return Collections.emptyList();
    }

    public List<MetadataGroupInterface> getAllMetadataGroups() {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public List<PersonInterface> getAllPersons() {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

<<<<<<< HEAD:Kitodo/src/main/java/org/kitodo/production/helper/metadata/legacytypeimplementations/LegacyFileSetDocStructHelper.java
    public List<PersonInterface> getAllPersonsByType(MetadataTypeInterface metadataType) {
        //TODO remove
=======
    public List<PersonInterface> getAllPersonsByType(LegacyMetadataTypeHelper metadataType) {
>>>>>>> Remove references to MetadataTypeInterface [not compilable]:Kitodo/src/main/java/org/kitodo/helper/metadata/LegacyFileSetDocStructHelper.java
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public List<ReferenceInterface> getAllReferences(String direction) {
        /*
         * Although the method is called because the same loop is used for
         * logical and physical structure elements, it must come back empty.
         */
        return Collections.emptyList();
    }

    @Override
    public Collection<ReferenceInterface> getAllToReferences() {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public Collection<ReferenceInterface> getAllToReferences(String type) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    public Object getAllVisibleMetadata() {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public String getAnchorClass() {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    public LegacyDocStructHelperInterface getChild(String type, String identifierField, String identifier) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
<<<<<<< HEAD:Kitodo/src/main/java/org/kitodo/production/helper/metadata/legacytypeimplementations/LegacyFileSetDocStructHelper.java
    public List<MetadataTypeInterface> getDisplayMetadataTypes() {
        //TODO remove
=======
    public List<LegacyMetadataTypeHelper> getDisplayMetadataTypes() {
>>>>>>> Remove references to MetadataTypeInterface [not compilable]:Kitodo/src/main/java/org/kitodo/helper/metadata/LegacyFileSetDocStructHelper.java
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public String getImageName() {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    public LegacyDocStructHelperInterface getNextChild(LegacyDocStructHelperInterface predecessor) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public LegacyDocStructHelperInterface getParent() {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
<<<<<<< HEAD:Kitodo/src/main/java/org/kitodo/production/helper/metadata/legacytypeimplementations/LegacyFileSetDocStructHelper.java
    public List<MetadataTypeInterface> getPossibleMetadataTypes() {
        //TODO remove
=======
    public List<LegacyMetadataTypeHelper> getPossibleMetadataTypes() {
>>>>>>> Remove references to MetadataTypeInterface [not compilable]:Kitodo/src/main/java/org/kitodo/helper/metadata/LegacyFileSetDocStructHelper.java
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
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
    public LegacyLogicalDocStructTypeHelper getType() {
        StackTraceElement[] stackTrace = new RuntimeException().getStackTrace();
        logger.log(Level.WARN, "Method {}.{}() invokes {}.{}(), bypassing the interface!", stackTrace[1].getClassName(),
            stackTrace[1].getMethodName(), stackTrace[0].getClassName(), stackTrace[0].getMethodName());
        return getDocStructType();
    }

    public boolean isDocStructTypeAllowedAsChild(LegacyLogicalDocStructTypeHelper type) {
        // TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    public void removeChild(LegacyDocStructHelperInterface docStruct) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    public void removeContentFile(LegacyContentFileHelper contentFile) throws ContentFileNotLinkedException {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public void removeMetadata(LegacyMetadataHelper metaDatum) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    public void removeMetadataGroup(MetadataGroupInterface metadataGroup) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    public void removePerson(PersonInterface person) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public void removeReferenceTo(LegacyDocStructHelperInterface target) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    public void setImageName(String imageName) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

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
