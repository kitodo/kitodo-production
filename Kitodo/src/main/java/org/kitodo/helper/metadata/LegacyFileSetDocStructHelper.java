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

package org.kitodo.helper.metadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataformat.mets.FileXmlElementAccessInterface;
import org.kitodo.api.ugh.ContentFileInterface;
import org.kitodo.api.ugh.DigitalDocumentInterface;
import org.kitodo.api.ugh.DocStructInterface;
import org.kitodo.api.ugh.DocStructTypeInterface;
import org.kitodo.api.ugh.FileSetInterface;
import org.kitodo.api.ugh.MetadataGroupInterface;
import org.kitodo.api.ugh.MetadataGroupTypeInterface;
import org.kitodo.api.ugh.MetadataInterface;
import org.kitodo.api.ugh.MetadataTypeInterface;
import org.kitodo.api.ugh.PersonInterface;
import org.kitodo.api.ugh.PrefsInterface;
import org.kitodo.api.ugh.ReferenceInterface;
import org.kitodo.api.ugh.VirtualFileGroupInterface;
import org.kitodo.api.ugh.exceptions.ContentFileNotLinkedException;
import org.kitodo.api.ugh.exceptions.MetadataTypeNotAllowedException;
import org.kitodo.api.ugh.exceptions.TypeNotAllowedAsChildException;
import org.kitodo.api.ugh.exceptions.TypeNotAllowedForParentException;

/**
 * Connects a legacy file set its corresponding doc struct to a media units
 * list. This is a soldering class to keep legacy code operational which is
 * about to be removed. Do not use this class.
 */

public class LegacyFileSetDocStructHelper implements FileSetInterface, DocStructInterface {
    private static final Logger logger = LogManager.getLogger(LegacyFileSetDocStructHelper.class);

    /**
     * The media units list accessed via this soldering class.
     */
    private List<FileXmlElementAccessInterface> mediaUnits;

    public LegacyFileSetDocStructHelper(List<FileXmlElementAccessInterface> mediaUnits) {
        this.mediaUnits = mediaUnits;
    }

    @Override
    public void addFile(ContentFileInterface contentFile) {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public void addVirtualFileGroup(VirtualFileGroupInterface virtualFileGroup) {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public Iterable<ContentFileInterface> getAllFiles() {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public void removeFile(ContentFileInterface contentFile) {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public void addChild(DocStructInterface child) throws TypeNotAllowedAsChildException {
        mediaUnits.add(((LegacyInnerPhysicalDocStructHelper) child).getMediaUnit());
    }

    @Override
    public void addChild(Integer index, DocStructInterface child) throws TypeNotAllowedAsChildException {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public void addContentFile(ContentFileInterface contentFile) {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public void addMetadata(MetadataInterface metadata) throws MetadataTypeNotAllowedException {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public DocStructInterface addMetadata(String metadataType, String value) throws MetadataTypeNotAllowedException {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public void addMetadataGroup(MetadataGroupInterface metadataGroup) throws MetadataTypeNotAllowedException {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public void addPerson(PersonInterface person) throws MetadataTypeNotAllowedException {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public ReferenceInterface addReferenceTo(DocStructInterface docStruct, String type) {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public DocStructInterface copy(boolean copyMetaData, Boolean recursive) {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public DocStructInterface createChild(String docStructType, DigitalDocumentInterface digitalDocument,
            PrefsInterface prefs) throws TypeNotAllowedAsChildException, TypeNotAllowedForParentException {

        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public void deleteUnusedPersonsAndMetadata() {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public List<MetadataGroupTypeInterface> getAddableMetadataGroupTypes() {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public List<MetadataTypeInterface> getAddableMetadataTypes() {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public List<DocStructInterface> getAllChildren() {
        List<DocStructInterface> result = new ArrayList<>(mediaUnits.size());
        for (FileXmlElementAccessInterface mediaUnit : mediaUnits) {
            result.add(new LegacyInnerPhysicalDocStructHelper(mediaUnit));
        }
        return result;
    }

    @Override
    public List<DocStructInterface> getAllChildrenByTypeAndMetadataType(String page, String asterisk) {
        List<DocStructInterface> result = new ArrayList<>(mediaUnits.size());
        for (FileXmlElementAccessInterface mediaUnit : mediaUnits) {
            result.add(new LegacyInnerPhysicalDocStructHelper(mediaUnit));
        }
        return result;
    }

    @Override
    public List<ContentFileInterface> getAllContentFiles() {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public List<ReferenceInterface> getAllFromReferences() {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public List<MetadataInterface> getAllIdentifierMetadata() {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public List<MetadataInterface> getAllMetadata() {
        return Collections.emptyList();
    }

    @Override
    public List<? extends MetadataInterface> getAllMetadataByType(MetadataTypeInterface metadataType) {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public List<MetadataGroupInterface> getAllMetadataGroups() {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public List<PersonInterface> getAllPersons() {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public List<PersonInterface> getAllPersonsByType(MetadataTypeInterface metadataType) {
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
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public Collection<ReferenceInterface> getAllToReferences(String type) {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public Object getAllVisibleMetadata() {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public String getAnchorClass() {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public DocStructInterface getChild(String type, String identifierField, String identifier) {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public List<MetadataTypeInterface> getDisplayMetadataTypes() {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public String getImageName() {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public DocStructInterface getNextChild(DocStructInterface predecessor) {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public DocStructInterface getParent() {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public List<MetadataTypeInterface> getPossibleMetadataTypes() {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public DocStructTypeInterface getDocStructType() {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    /**
     * This method is not part of the interface, but the JSF code digs in the
     * depths of the UGH and uses it on the guts.
     * 
     * @return Method delegated to {@link #getDocStructType()}
     */
    public DocStructTypeInterface getType() {
        StackTraceElement[] stackTrace = new RuntimeException().getStackTrace();
        logger.log(Level.WARN, "Method {}.{}() invokes {}.{}(), bypassing the interface!", stackTrace[1].getClassName(),
            stackTrace[1].getMethodName(), stackTrace[0].getClassName(), stackTrace[0].getMethodName());
        return getDocStructType();
    }

    @Override
    public boolean isDocStructTypeAllowedAsChild(DocStructTypeInterface type) {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public void removeChild(DocStructInterface docStruct) {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public void removeContentFile(ContentFileInterface contentFile) throws ContentFileNotLinkedException {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public void removeMetadata(MetadataInterface metaDatum) {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public void removeMetadataGroup(MetadataGroupInterface metadataGroup) {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public void removePerson(PersonInterface person) {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public void removeReferenceTo(DocStructInterface target) {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public void setImageName(String imageName) {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public void setType(DocStructTypeInterface docStructType) {
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
