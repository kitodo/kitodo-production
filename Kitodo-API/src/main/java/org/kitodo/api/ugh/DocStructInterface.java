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
import java.util.NoSuchElementException;

import org.kitodo.api.ugh.exceptions.ContentFileNotLinkedException;
import org.kitodo.api.ugh.exceptions.DocStructHasNoTypeException;
import org.kitodo.api.ugh.exceptions.IncompletePersonObjectException;
import org.kitodo.api.ugh.exceptions.MetadataTypeNotAllowedException;
import org.kitodo.api.ugh.exceptions.TypeNotAllowedAsChildException;
import org.kitodo.api.ugh.exceptions.TypeNotAllowedForParentException;

/**
 * One node of a tree depicting the structure of the document.
 *
 * <p>
 * A DocStruct object represents a structure entity in work. Every document
 * consists of a structure, which can be separated into several structure
 * entities, which build hierarchical structure. Usually a
 * {@code DigitalDocument} contains two structures; a logical and a physical
 * one. Each structure consists of a top DocStruct element that is embedded in
 * some kind of structure. This structure is represented by parent and children
 * of {@code DocStruct} objects.
 *
 * <p>
 * This class contains methods to:
 * <ul>
 * <li>Retrieve information about the structure (add, move and remove children),
 * <li>set the parent (the top element has no parent),
 * <li>set and retrieve meta-data, which describe a structure entity,
 * <li>handle content files, which are linked to a structure entity.
 * </ul>
 *
 * <p>
 * Every structure entity is of a special kind. The kind of entity is stored in
 * a {@code DocStructType} element. Depending on the type of structure entities
 * certain meta-data and children a permitted or forbidden.
 */
public interface DocStructInterface {

    /**
     * Adds another {@code DocStruct} as a child to this instance. The new child
     * will automatically become the last child in the list. When adding a
     * {@code DocStruct}, configuration is checked, whether a {@code DocStruct}
     * of this type can be added. If not, a
     * {@code TypeNotAllowedAsChildException} is thrown. The parent of this
     * child (this instance) is set automatically.
     *
     * @param child
     *            DocStruct to be added
     * @throws TypeNotAllowedAsChildException
     *             if a child should be added, but it's DocStruct type isn't
     *             member of this instance's DocStruct type
     */
    void addChild(DocStructInterface child) throws TypeNotAllowedAsChildException;

    /**
     * Adds a DocStruct object as a child to this instance. The new child will
     * become the element at the specified position in the child list while the
     * element currently at that position (if any) and any subsequent elements
     * are shifted to the right (so that one gets added to their indices), or
     * the last child in the list if index is null. When adding a DocStruct,
     * configuration is checked, whether a DocStruct of this type can be added.
     * If not, a TypeNotAllowedAsChildException is thrown. The parent of this
     * child (this instance) is set automatically.
     *
     * @param index
     *            index at which the child is to be inserted
     * @param child
     *            DocStruct to be added
     * @throws TypeNotAllowedAsChildException
     *             if a child should be added, but it's DocStruct type isn't
     *             member of this instance's DocStruct type
     */
    void addChild(Integer index, DocStructInterface child) throws TypeNotAllowedAsChildException;

    /**
     * Adds a new reference to a content file, and adds the content file to the
     * file set.
     *
     * @param contentFile
     *            content file to add
     */
    void addContentFile(ContentFileInterface contentFile);

    /**
     * Adds a meta-data object to this instance. The method checks, if it is
     * allowed to add it, based on the configuration. If so, the object is added
     * and the method returns {@code true}, otherwise it returns {@code false}.
     * <p>
     * The {@code Metadata} object must already include all necessary
     * information, such as {@code MetadataType} and value.
     * <p>
     * For internal reasons, this method replaces the {@code MetadataType}
     * object by a local copy, which is retrieved from the {@code DocStructType}
     * of this instance. The internal name of both {@code MetadataType} objects
     * will still be identical afterwards. If a local copy cannot be found,
     * which means that the meta-data type is invalid on this instance, false is
     * returned.
     *
     * @param metadata
     *            meta-data object to add
     * @throws MetadataTypeNotAllowedException
     *             if this instance does not allow the meta-data type to be
     *             added, or if the maximum allowed number of meta-data of this
     *             type has already been added
     * @throws DocStructHasNoTypeException
     *             if no {@code DocStructType} is set on this instance. In this
     *             case, the meta-data element cannot be added because we cannot
     *             check whether the the meta-data type is allowed or not.
     */
    void addMetadata(MetadataInterface metadata) throws MetadataTypeNotAllowedException;

    /**
     * Adds a meta data field with the given name to this DocStruct and sets it
     * to the given value.
     *
     * @param metadataType
     *            name of the meta data field to add
     * @param value
     *            value to set the field to
     * @return {@code this}, for method chaining. The return value is never
     *         used.
     * @throws MetadataTypeNotAllowedException
     *             if no corresponding MetadataType object is returned by
     *             getAddableMetadataTypes()
     */
    DocStructInterface addMetadata(String metadataType, String value) throws MetadataTypeNotAllowedException;

    /**
     * Adds a meta-data group to this instance. The method checks, if it is
     * allowed to add it, based on the configuration. If so, the object is added
     * and the method returns {@code true}, otherwise it returns {@code false}.
     * The {@code MetadataGroup} object must already include all necessary
     * information, such as {@code MetadataGroupType} and value.
     *
     * <p>
     * For internal reasons, this method replaces the {@code MetadataGroupType}
     * object by a local copy, which is retrieved from the {@code DocStructType}
     * of this instance. The internal name of both {@code MetadataGroupType}
     * objects will still be identical afterwards. If a local copy cannot be
     * found, which means that the meta-data type is invalid on this instance,
     * false is returned.
     *
     * @param metadataGroup
     *            meta-data group to be added
     * @throws MetadataTypeNotAllowedException
     *             if the {@code DocStructType} of this instance does not allow
     *             the {@code MetadataGroupType}, or if the maximum number of
     *             meta-data groups of this type has already been added
     * @throws DocStructHasNoTypeException
     *             if no {@code DocStructType} is set on this instance. In this
     *             case, the meta-data group cannot be added because we cannot
     *             check whether the the meta-data group type is allowed or not.
     */
    void addMetadataGroup(MetadataGroupInterface metadataGroup) throws MetadataTypeNotAllowedException;

    /**
     * Adds a person to the document structure.
     *
     * @param person
     *            person to add
     * @throws MetadataTypeNotAllowedException
     *             if no corresponding MetadataType object is returned by
     *             getAddableMetadataTypes()
     */
    void addPerson(PersonInterface person) throws MetadataTypeNotAllowedException;

    /**
     * Adds an outgoing reference to another {@code DocStruct} instance.
     * {@code Reference}s are always linked both ways. Both {@code DocStruct}
     * instances are storing a reference to the other {@code DocStruct}
     * instance. This methods stores the outgoing reference. The
     * {@code DocStruct} instance given as a parameter is the target of the
     * Reference (to which is linked). The corresponding back-reference (from
     * the target to the source—this instance) is set automatically. Each
     * reference can contain a type.
     *
     * @param docStruct
     *            target to link to
     * @param type
     *            the type of reference
     * @return a newly created References object containing information about
     *         linking both DocStructs. The return value is never used.
     */
    ReferenceInterface addReferenceTo(DocStructInterface docStruct, String type);

    /**
     * Creates a copy of this instance, with some or all {@code Metadata} and
     * {@code Person} objects attached.
     *
     * @param copyMetaData
     *            if true, copies {@code Metadata} objects
     * @param recursive
     *            if true, copies all children as well; if null, copies all
     *            children which are of the same anchor class; if false, doesn’t
     *            copy any children
     * @return a new DocStruct instance
     */
    DocStructInterface copy(boolean copyMetaData, Boolean recursive);

    /**
     * Creates a child DocStruct below a DocStruct. This is a convenience
     * function to add a DocStruct by its type name string.
     *
     * <p>
     * FIXME: Why is this function still here? It should have been removed. Is
     * there something not yet merged?
     *
     * @param docStructType
     *            structural type of the child to create
     * @param digitalDocument
     *            act to create the child in
     * @param prefs
     *            rule set the act is based on
     * @return the child created
     * @throws TypeNotAllowedForParentException
     *             is thrown, if this DocStruct is not allowed for a parent
     * @throws TypeNotAllowedAsChildException
     *             if a child should be added, but it's DocStruct type isn't
     *             member of this instance's DocStruct type
     */
    DocStructInterface createChild(String docStructType, DigitalDocumentInterface digitalDocument, PrefsInterface prefs)
            throws TypeNotAllowedAsChildException, TypeNotAllowedForParentException;

    /**
     * This method cleans the meta-data and person list of instances which do
     * not have a value. This method is usually used in conjunction with the
     * method {@code showMetadataForm(String, boolean)}. After
     * {@code showMetadataForm()} has been called and the form has been
     * displayed, this method should be called to delete the created empty
     * meta-data instances.
     *
     * <p>
     * An empty meta-data instance is:
     * <ul>
     * <li>A meta-data object with a value of null.</li>
     * <li>A person object with neither a lastname, nor a firstname, an
     * identifier, nor an institution.</li>
     * </ul>
     */
    void deleteUnusedPersonsAndMetadata();

    /**
     * Returns all meta-data group types that can be added to this instance and
     * shall be visible to the user. This method considers already added
     * {@code MetadataGroup}s, so meta-data group types which can only be
     * available once cannot be added a second time. Therefore these
     * {@code MetadataGroupType}s will not be included in this list.
     *
     * <p>
     * Internal meta-data groups, whose {@code MetadataGroupType} starts with
     * the {@code HIDDEN_METADATA_CHAR}, will also not be included.
     *
     * @return all meta-data group types that users can add to this instance
     */
    List<MetadataGroupTypeInterface> getAddableMetadataGroupTypes();

    /**
     * Returns all meta-data types that can be added to this instance and shall
     * be visible to the user. This method considers already added
     * {@code Metadata}, so meta-data types which can only be available once
     * cannot be added a second time. Therefore these {@code MetadataType}s will
     * not be included in this list.
     *
     * <p>
     * Internal meta-data groups, whose {@code MetadataGroupType} starts with
     * the {@code HIDDEN_METADATA_CHAR}, will also not be included.
     *
     * @return all meta-data types that users can add to this instance
     */
    List<MetadataTypeInterface> getAddableMetadataTypes();

    /**
     * Returns a list containing all children of this DocStruct. If this
     * instance has no children, {@code null} is returned.
     *
     * @return all children of this DocStruct
     */
    List<DocStructInterface> getAllChildren();

    /**
     * Returns all children of this instance which are of a given type and have
     * a given type of meta-data attached. For example, you can get all articles
     * which have an author. It is possible to use "{@code *}" as wildcard
     * character value for {@code theDocTypeName} and {@code theMDTypeName}.
     *
     * <p>
     * If this instance has no children, null is returned.
     *
     * @param docStructType
     *            name of the structural type
     * @param metaDataType
     *            name of the meta-data type
     * @return all children of the given type and with the given meta-data
     */
    List<DocStructInterface> getAllChildrenByTypeAndMetadataType(String docStructType, String metaDataType);

    /**
     * Returns all content files from this instance. If no {@code ContentFile}
     * is available, {@code null} is returned.
     *
     * @return the content files from this instance
     */
    List<ContentFileInterface> getAllContentFiles();

    /**
     * Returns all references that are directed from another instance to this
     * instance. This are all {@code Reference}s in which this instance is the
     * target.
     *
     * @return all incoming {@code Reference}s
     */
    List<ReferenceInterface> getAllFromReferences();

    /**
     * Returns all {@code Metadata} objects which are identifiers. Identifiers
     * are all {@code Metadata} objects whose {@code MetadataType#isIdentifier}
     * flag is set to {@code true}.
     *
     * <p>
     * If none were found, {@code null} is returned.
     *
     * @return all {@code Metadata} objects which are identifiers
     */
    List<MetadataInterface> getAllIdentifierMetadata();

    /**
     * Returns all meta-data from this instance. If no {@code Metadata} is
     * available, {@code null} is returned.
     *
     * @return all meta-data from this instance. A return type
     *         {@code Collection<>} would be sufficient.
     */
    List<MetadataInterface> getAllMetadata();

    /**
     * Returns all meta-data of a given type, including persons. Can be used to
     * get all titles, authors, etc.
     *
     * <p>
     * If no {@code MetadataGroup}s are available, an empty list is returned.
     *
     * @param metadataType
     *            meta-data type to look for
     * @return all meta-data of the given type
     */
    List<? extends MetadataInterface> getAllMetadataByType(MetadataTypeInterface metadataType);

    /**
     * Returns all meta-data groups from this instance. If no
     * {@code MetadataGroup} is available, null is returned.
     *
     * @return all meta-data groups from this instance
     */
    List<MetadataGroupInterface> getAllMetadataGroups();

    /**
     * Returns a list of all persons. If no {@code Person} objects are
     * available, {@code null} is returned.
     *
     * @return all persons
     */
    List<PersonInterface> getAllPersons();

    /**
     * Returns all persons of a given type. {@code Person}s only.
     *
     * <p>
     * If no {@code Person} objects are available, null is returned.
     *
     * @param metadataType
     *            meta-data type to look for
     * @return all meta-data of the given type
     */
    List<PersonInterface> getAllPersonsByType(MetadataTypeInterface metadataType);

    /**
     * Returns incoming or outgoing {@code Reference}s.
     *
     * @param direction
     *            can be "{@code to}" or "{@code from}". String seems to be
     *            always "{@code to}".
     * @return incoming or outgoing {@code Reference}s
     */
    List<ReferenceInterface> getAllReferences(String direction);

    /**
     * Returns all references that are directed from this instance to another.
     * This are all {@code Reference}s in which this instance is the source.
     *
     * @return all outgoing {@code Reference}s
     */
    Collection<ReferenceInterface> getAllToReferences();

    /**
     * Returns all references that are directed from this instance to another
     * and have a given type. For example, the type "{@code logical_physical}"
     * refers to references from logical structures to physical structures.
     *
     * @param type
     *            type of the references to return, always "logical_physical"?
     * @return all outgoing {@code Reference}s of the given type. The return
     *         type would be sufficient to be an Iterable, but there is a check
     *         for size()=0.
     */
    Collection<ReferenceInterface> getAllToReferences(String type);

    /**
     * Returns all meta-data for this instance that shall be displayed. This
     * excludes all {@code Metadata} whose {@code MetadataType} starts with the
     * {@code HIDDEN_METADATA_CHAR}.
     *
     * <p>
     * Method usage: is null/is non-null -check only.
     *
     * @return all meta-data that shall be displayed
     */
    Object getAllVisibleMetadata();

    /**
     * Retrieves the name of the anchor structure, if any, or null otherwise.
     * Anchors are a special type of document structure, which group other
     * structure entities together, but have no own content. Imagine a
     * periodical as such an anchor. The periodical itself is a virtual
     * structure entity without any own content, but groups all years of
     * appearance together. Years may be anchors again for volumes, etc.
     *
     * @return String, which is null, if it cannot be used as an anchor
     */
    String getAnchorClass();

    /**
     * The function getChild() returns a child of a DocStruct, identified by its
     * type and an identifier in a meta data field of choice. More formally,
     * returns the first child matching the given conditions and does not work
     * recursively. If no matching child is found, throws
     * NoSuchElementException.
     *
     * @param type
     *            structural type of the child to locate
     * @param identifierField
     *            meta data field that holds the identifier to locate the child
     * @param identifier
     *            identifier of the child to locate
     * @return the child, if found
     * @throws NoSuchElementException
     *             if no matching child is found
     */
    DocStructInterface getChild(String type, String identifierField, String identifier);

    /**
     * Returns all meta-data types that shall be displayed even if they have no
     * value.
     *
     * <p>
     * Comprises all meta-data types whose attribute
     * {@code defaultDisplay="true"} is set in the {@code Preferences}. Hidden
     * meta-data, whose {@code MetadataType} starts with the
     * {@code HIDDEN_METADATA_CHAR}, will not be included.
     *
     * @return all meta-data group types that shall always be displayed
     */
    List<MetadataTypeInterface> getDisplayMetadataTypes();

    /**
     * Returns the image name.
     *
     * @return the image name
     */
    String getImageName();

    /**
     * Returns the next child in the list of all children. If the given
     * {@code DocStruct} isn’t a child of the current instance, {@code null} is
     * returned.
     *
     * @param predecessor
     *            {@code DocStruct} whose successor shall be returned
     * @return the next {@code DocStruct} after {@code inChild}, {@code null}
     *         otherwise
     */
    DocStructInterface getNextChild(DocStructInterface predecessor);

    /**
     * Returns the parent of this instance. Returns {@code null} if this
     * instance is the root of the tree.
     *
     * @return the parent, if any
     */
    DocStructInterface getParent();

    /**
     * Returns all meta-data types that can be added to this instance. Includes
     * meta-data groups, whose {@code MetadataGroupType} starts with the
     * {@code HIDDEN_METADATA_CHAR}.
     *
     * <p>
     * This method considers already added {@code Metadata}, so meta-data types
     * which can only be available once cannot be added a second time. Therefore
     * these {@code MetadataType}s will not be included in this list.
     *
     * @return all meta-data types that can be added to this instance. A return
     *         type of {@code Collection<>} would be sufficient.
     */
    List<MetadataTypeInterface> getPossibleMetadataTypes();

    /**
     * Get the type of this DocStruct.
     *
     * @return the type of this DocStruct
     */
    DocStructTypeInterface getDocStructType();

    /**
     * Returns whether a {@code DocStruct} of the given {@code DocStructType} is
     * allowed to be added to this instance.
     *
     * @param type
     *            the {@code DocStructType} in question
     * @return true, if {@code DocStruct} of this type can be added; otherwise
     *         false
     */
    boolean isDocStructTypeAllowedAsChild(DocStructTypeInterface type);

    /**
     * Removes a child from this instance.
     *
     * @param docStruct
     *            to be removed
     */
    void removeChild(DocStructInterface docStruct);

    /**
     * Removes all links from this instance to a given content file. If the
     * given {@code ContentFile} is referenced more than once from this
     * instance, all links are removed. For that reason, all attached
     * {@code ContentFileReference} objects are searched.
     *
     * @param contentFile
     *            the content file to be removed
     * @throws ContentFileNotLinkedException
     *             if the content file to be removed is not referenced
     */
    void removeContentFile(ContentFileInterface contentFile) throws ContentFileNotLinkedException;

    /**
     * Removes a meta-datum from this instance. If (according to configuration)
     * at least one {@code Metadata} of this type is required on this instance,
     * the meta-datum will <i>not be removed</i>.
     *
     * @param metaDatum
     *            meta-datum which should be removed
     */
    void removeMetadata(MetadataInterface metaDatum);

    /**
     * Removes a meta-data group from this instance. If (according to
     * configuration) at least one meta-data group of this type is required on
     * this instance, the meta-data group will <i>not be removed</i>.
     *
     * <p>
     * If you want to remove a meta-data group just to replace it, use the
     * method {@code changeMetadataGroup(MetadataGroup, MetadataGroup)} instead.
     *
     * @param metadataGroup
     *            meta-data group which should be removed
     */
    void removeMetadataGroup(MetadataGroupInterface metadataGroup);

    /**
     * Removes a person from this instance.
     *
     * @param person
     *            person which should be removed
     * @throws IncompletePersonObjectException
     *             if {@code in} does not have a {@code MetadataType}
     */
    void removePerson(PersonInterface person);

    /**
     * Removes an outgoing reference. An outgoing reference is a reference to
     * another {@code DocStruct} instance. The corresponding incoming
     * {@code Reference} in the target {@code DocStruct} is also deleted.
     *
     * @param target
     *            {@code DocStruct}
     */
    void removeReferenceTo(DocStructInterface target);

    /**
     * Sets the image name.
     *
     * @param imageName
     *            image name to set
     */
    void setImageName(String imageName);

    /**
     * Sets the type of this DocStruct. When changing the type, the allowed
     * meta-data elements and children are <i>not</i> checked. Therefore it is
     * possible to create documents that are not valid against the current
     * preferences file.
     *
     * @param docStructType
     *            type to set
     */
    void setType(DocStructTypeInterface docStructType);

}
