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

public interface DocStruct {

    void addChild(DocStruct dsvolume) throws TypeNotAllowedAsChildException;

    void addChild(int positionByRank, DocStruct child);

    void addContentFile(ContentFile cf);

    void addMetadata(Metadata newMetadata) throws MetadataTypeNotAllowedException;

    void addMetadata(String key, String value) throws MetadataTypeNotAllowedException;

    void addMetadataGroup(MetadataGroup metadataGroup) throws MetadataTypeNotAllowedException;

    void addPerson(Person p) throws MetadataTypeNotAllowedException;

    void addReferenceTo(DocStruct newPage, String string);

    DocStruct copy(boolean b, boolean c);

    DocStruct createChild(String docStructType, DigitalDocument digitalDocument, Prefs ruleset)
            throws TypeNotAllowedAsChildException, TypeNotAllowedForParentException;

    void deleteUnusedPersonsAndMetadata();

    List<MetadataGroupType> getAddableMetadataGroupTypes();

    List<MetadataType> getAddableMetadataTypes();

    List<DocStruct> getAllChildren();

    List<DocStruct> getAllChildrenByTypeAndMetadataType(String string, String string2);

    List<ContentFile> getAllContentFiles();

    List<Reference> getAllFromReferences();

    List<Metadata> getAllIdentifierMetadata();

    List<Metadata> getAllMetadata(); /*
                                      * return type collection would be
                                      * sufficient
                                      */

    List<? extends Metadata> getAllMetadataByType(MetadataType mdt);

    List<MetadataGroup> getAllMetadataGroups();

    List<Person> getAllPersons();

    List<Person> getAllPersonsByType(MetadataType authorType);

    List<Reference> getAllReferences(String string); // string seems to be
                                                     // always "to"

    Collection<Reference> getAllToReferences();

    Collection<Reference> getAllToReferences(String string); // string alway
                                                             // "logical_physical"
                                                             // ?
    // return type would be sufficient to be iterable, but there is a check for
    // size()=0?

    Object getAllVisibleMetadata(); // is null/is non-null -check only

    String getAnchorClass();

    DocStruct getChild(String type, String identifierField, String identifier);

    List<MetadataType> getDisplayMetadataTypes();

    String getImageName();

    DocStruct getNextChild(DocStruct tempDS); // throws
                                              // IndexOutOfBoundsException

    DocStruct getParent();

    List<MetadataType> getPossibleMetadataTypes(); // collection would be
                                                   // sufficient

    DocStructType getType();

    boolean isDocStructTypeAllowedAsChild(DocStructType type);

    void removeChild(DocStruct docStruct);

    void removeContentFile(ContentFile contentFile) throws ContentFileNotLinkedException;

    void removeMetadata(Metadata md);

    void removeMetadataGroup(MetadataGroup metadataGroup);

    void removePerson(Person p);

    void removeReferenceTo(DocStruct docStruct);

    void setImageName(String otherFile);

    void setType(DocStructType dst);

}
