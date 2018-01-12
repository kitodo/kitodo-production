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

package org.kitodo.api.ugh;

import java.util.Collection;
import java.util.List;
import org.kitodo.api.ugh.exceptions.ContentFileNotLinkedException;
import org.kitodo.api.ugh.exceptions.MetadataTypeNotAllowedException;
import org.kitodo.api.ugh.exceptions.TypeNotAllowedAsChildException;
import org.kitodo.api.ugh.exceptions.TypeNotAllowedForParentException;

public interface DocStructInterface {

    /**
     * @return wether inchild isn’t null and its type isn’t null. The return
     *         value is never used.
     */
    boolean addChild(DocStructInterface dsvolume) throws TypeNotAllowedAsChildException;

    /**
     * @param index
     *            {@code int} would be sufficient. Throws NullPointerException
     *            if {@code null}.
     * @return {@code false} if {@code child} is {@code null} or does not have a
     *         type, true otherwise. The return value is never used.
     */
    boolean addChild(Integer index, DocStructInterface child) throws TypeNotAllowedAsChildException;

    void addContentFile(ContentFileInterface cf);

    /**
     * @return always {@code true}. The return value is never used.
     */
    boolean addMetadata(MetadataInterface newMetadata) throws MetadataTypeNotAllowedException;

    /**
     * @return {@code this}. The return value is never used.
     */
    DocStructInterface addMetadata(String fieldName, String value) throws MetadataTypeNotAllowedException;

    /**
     * @return always {@code true}. The return value is never used.
     */
    boolean addMetadataGroup(MetadataGroupInterface metadataGroupInterface) throws MetadataTypeNotAllowedException;

    /**
     * @return always {@code true}. The return value is never used.
     */
    boolean addPerson(PersonInterface p) throws MetadataTypeNotAllowedException;

    /**
     * @return a newly created References object containing information about
     *         linking both DocStructs. The return value is never used.
     */
    ReferenceInterface addReferenceTo(DocStructInterface newPage, String string);

    DocStructInterface copy(boolean b, Boolean c);

    /**
     * FIXME: Why is this function still here? It should have been removed. Is
     * there something not yet merged?
     */
    DocStructInterface createChild(String docStructType, DigitalDocumentInterface digitalDocumentInterface,
            PrefsInterface ruleset) throws TypeNotAllowedAsChildException, TypeNotAllowedForParentException;

    void deleteUnusedPersonsAndMetadata();

    List<MetadataGroupTypeInterface> getAddableMetadataGroupTypes();

    List<MetadataTypeInterface> getAddableMetadataTypes();

    List<DocStructInterface> getAllChildren();

    List<DocStructInterface> getAllChildrenByTypeAndMetadataType(String type, String metadataType);

    List<ContentFileInterface> getAllContentFiles();

    List<ReferenceInterface> getAllFromReferences();

    List<MetadataInterface> getAllIdentifierMetadata();

    /**
     * @return type {@code Collection<>} would be sufficient.
     */
    List<MetadataInterface> getAllMetadata();

    List<? extends MetadataInterface> getAllMetadataByType(MetadataTypeInterface mdt);

    List<MetadataGroupInterface> getAllMetadataGroups();

    List<PersonInterface> getAllPersons();

    List<PersonInterface> getAllPersonsByType(MetadataTypeInterface authorType);

    /*
     * @param string seems to be always "to"
     */
    List<ReferenceInterface> getAllReferences(String string);

    Collection<ReferenceInterface> getAllToReferences();

    /*
     * @param string always "logical_physical" ?
     * 
     * @return type would be sufficient to be iterable, but there is a check for
     * size()=0?
     */
    Collection<ReferenceInterface> getAllToReferences(String string);

    /*
     * Is null/is non-null -check only.
     */
    Object getAllVisibleMetadata();

    String getAnchorClass();

    DocStructInterface getChild(String type, String identifierField, String identifier);

    List<MetadataTypeInterface> getDisplayMetadataTypes();

    String getImageName();

    /*
     * @throws IndexOutOfBoundsException
     */
    DocStructInterface getNextChild(DocStructInterface tempDS);

    DocStructInterface getParent();

    /**
     * @return type of {@code Collection<>} would be sufficient
     */
    List<MetadataTypeInterface> getPossibleMetadataTypes();

    DocStructTypeInterface getType();

    boolean isDocStructTypeAllowedAsChild(DocStructTypeInterface type);

    /**
     * @return whether the list contained that child. The return value is never
     *         used.
     */
    boolean removeChild(DocStructInterface docStructInterface);

    /**
     * @return whether the content file references list was initialized with an
     *         list instance before the function call. The return value is never
     *         used.
     */
    boolean removeContentFile(ContentFileInterface contentFileInterface) throws ContentFileNotLinkedException;

    /**
     * @return always {@code true}. The return value is never used.
     */
    boolean removeMetadata(MetadataInterface md);

    /**
     * @return always {@code true}. The return value is never used.
     */
    boolean removeMetadataGroup(MetadataGroupInterface metadataGroupInterface);

    /**
     * @return whether the persons list was initialized with an list instance
     *         before the function call. The return value is never used.
     */
    boolean removePerson(PersonInterface p);

    /**
     * @return always {@code true}. The return value is never used.
     */
    boolean removeReferenceTo(DocStructInterface docStructInterface);

    void setImageName(String otherFile);

    /**
     * @return aways {@code true}. The return value is never used.
     */
    boolean setType(DocStructTypeInterface dst);

}
