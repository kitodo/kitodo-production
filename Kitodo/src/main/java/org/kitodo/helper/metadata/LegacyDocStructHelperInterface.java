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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.kitodo.api.ugh.DocStructInterface;
import org.kitodo.api.ugh.DocStructTypeInterface;
import org.kitodo.api.ugh.MetadataInterface;
import org.kitodo.api.ugh.MetadataTypeInterface;
import org.kitodo.api.ugh.PersonInterface;
import org.kitodo.api.ugh.ReferenceInterface;
import org.kitodo.api.ugh.exceptions.DocStructHasNoTypeException;
import org.kitodo.api.ugh.exceptions.MetadataTypeNotAllowedException;
import org.kitodo.api.ugh.exceptions.TypeNotAllowedAsChildException;

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
public interface LegacyDocStructHelperInterface extends DocStructInterface {

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
    void addContentFile(LegacyContentFileHelper contentFile);

    /**
     * Adds a meta-data object to this instance. The method checks, if it is
     * allowed to add it, based on the configuration. If so, the object is added
     * and the method returns {@code true}, otherwise it returns {@code false}.
     * 
     * <p>
     * The {@code Metadata} object must already include all necessary
     * information, such as {@code MetadataType} and value.
     * 
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
     * Returns a list of all persons. If no {@code Person} objects are
     * available, {@code null} is returned.
     *
     * @return all persons
     */
    default List<PersonInterface> getAllPersons() {
        return Collections.emptyList();
    }

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
     * Retrieves the name of the anchor structure, if any, or null otherwise.
     * Anchors are a special type of document structure, which group other
     * structure entities together, but have no own content. Imagine a
     * periodical as such an anchor. The periodical itself is a virtual
     * structure entity without any own content, but groups all years of
     * appearance together. Years may be anchors again for volumes, etc.
     *
     * @return String, which is null, if it cannot be used as an anchor
     */
    default String getAnchorClass() {
        return null;
    }

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
     * Removes a child from this instance.
     *
     * @param docStruct
     *            to be removed
     */
    void removeChild(DocStructInterface docStruct);

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
     * Removes an outgoing reference. An outgoing reference is a reference to
     * another {@code DocStruct} instance. The corresponding incoming
     * {@code Reference} in the target {@code DocStruct} is also deleted.
     *
     * @param target
     *            {@code DocStruct}
     */
    void removeReferenceTo(DocStructInterface target);
}
