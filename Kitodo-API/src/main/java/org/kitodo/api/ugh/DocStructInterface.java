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
import ugh.exceptions.ContentFileNotLinkedException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.TypeNotAllowedAsChildException;
import ugh.exceptions.TypeNotAllowedForParentException;

public interface DocStructInterface {

    void addChild(DocStructInterface dsvolume) throws TypeNotAllowedAsChildException;

    void addChild(int positionByRank, DocStructInterface child);

    void addContentFile(ContentFileInterface cf);

    void addMetadata(MetadataInterface newMetadata) throws MetadataTypeNotAllowedException;

    void addMetadata(String key, String value) throws MetadataTypeNotAllowedException;

    void addMetadataGroup(MetadataGroupInterface metadataGroupInterface) throws MetadataTypeNotAllowedException;

    void addPerson(PersonInterface p) throws MetadataTypeNotAllowedException;

    void addReferenceTo(DocStructInterface newPage, String string);

    DocStructInterface copy(boolean b, boolean c);

    DocStructInterface createChild(String docStructType, DigitalDocumentInterface digitalDocumentInterface, PrefsInterface ruleset)
            throws TypeNotAllowedAsChildException, TypeNotAllowedForParentException;

    void deleteUnusedPersonsAndMetadata();

    List<MetadataGroupTypeInterface> getAddableMetadataGroupTypes();

    List<MetadataTypeInterface> getAddableMetadataTypes();

    List<DocStructInterface> getAllChildren();

    List<DocStructInterface> getAllChildrenByTypeAndMetadataType(String string, String string2);

    List<ContentFileInterface> getAllContentFiles();

    List<ReferenceInterface> getAllFromReferences();

    List<MetadataInterface> getAllIdentifierMetadata();

    List<MetadataInterface> getAllMetadata(); /*
                                      * return type collection would be
                                      * sufficient
                                      */

    List<? extends MetadataInterface> getAllMetadataByType(MetadataTypeInterface mdt);

    List<MetadataGroupInterface> getAllMetadataGroups();

    List<PersonInterface> getAllPersons();

    List<PersonInterface> getAllPersonsByType(MetadataTypeInterface authorType);

    List<ReferenceInterface> getAllReferences(String string); // string seems to be
                                                     // always "to"

    Collection<ReferenceInterface> getAllToReferences();

    Collection<ReferenceInterface> getAllToReferences(String string); // string alway
                                                             // "logical_physical"
                                                             // ?
    // return type would be sufficient to be iterable, but there is a check for
    // size()=0?

    Object getAllVisibleMetadata(); // is null/is non-null -check only

    String getAnchorClass();

    DocStructInterface getChild(String type, String identifierField, String identifier);

    List<MetadataTypeInterface> getDisplayMetadataTypes();

    String getImageName();

    DocStructInterface getNextChild(DocStructInterface tempDS); // throws
                                              // IndexOutOfBoundsException

    DocStructInterface getParent();

    List<MetadataTypeInterface> getPossibleMetadataTypes(); // collection would be
                                                   // sufficient

    DocStructTypeInterface getType();

    boolean isDocStructTypeAllowedAsChild(DocStructTypeInterface type);

    void removeChild(DocStructInterface docStructInterface);

    void removeContentFile(ContentFileInterface contentFileInterface) throws ContentFileNotLinkedException;

    void removeMetadata(MetadataInterface md);

    void removeMetadataGroup(MetadataGroupInterface metadataGroupInterface);

    void removePerson(PersonInterface p);

    void removeReferenceTo(DocStructInterface docStructInterface);

    void setImageName(String otherFile);

    void setType(DocStructTypeInterface dst);

}
