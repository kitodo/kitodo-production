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

import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
public interface LegacyDocStructHelperInterface {
    static final Logger logger = LogManager.getLogger(LegacyDocStructHelperInterface.class);

    /**
     * Adds another {@code DocStruct} as a child to this instance. The new child
     * will automatically become the last child in the list.
     *
     * @param child
     *            DocStruct to be added
     */
    @Deprecated
    default void addChild(LegacyDocStructHelperInterface child) {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    /**
     * Adds a DocStruct object as a child to this instance. The new child will
     * become the element at the specified position in the child list while the
     * element currently at that position (if any) and any subsequent elements
     * are shifted to the right (so that one gets added to their indices), or
     * the last child in the list if index is null.
     *
     * @param index
     *            index at which the child is to be inserted
     * @param child
     *            DocStruct to be added
     */
    @Deprecated
    default void addChild(Integer index, LegacyDocStructHelperInterface child) {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    /**
     * Adds a new reference to a content file, and adds the content file to the
     * file set.
     *
     * @param contentFile
     *            content file to add
     */
    @Deprecated
    default void addContentFile(LegacyContentFileHelper contentFile) {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

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
     */
    @Deprecated
    default void addMetadata(LegacyMetadataHelper metadata) {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

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
    @Deprecated
    default LegacyReferenceHelper addReferenceTo(LegacyDocStructHelperInterface docStruct, String type) {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

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
    @Deprecated
    default void deleteUnusedPersonsAndMetadata() {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

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
    @Deprecated
    default List<LegacyMetadataTypeHelper> getAddableMetadataTypes() {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    /**
     * Returns a list containing all children of this DocStruct. If this
     * instance has no children, {@code null} is returned.
     *
     * @return all children of this DocStruct
     */
    @Deprecated
    default List<LegacyDocStructHelperInterface> getAllChildren() {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

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
    @Deprecated
    default List<LegacyDocStructHelperInterface> getAllChildrenByTypeAndMetadataType(String docStructType,
            String metaDataType) {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    /**
     * Returns all meta-data from this instance. If no {@code Metadata} is
     * available, {@code null} is returned.
     *
     * @return all meta-data from this instance. A return type
     *         {@code Collection<>} would be sufficient.
     */
    @Deprecated
    default List<LegacyMetadataHelper> getAllMetadata() {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

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
    @Deprecated
    default List<LegacyMetadataHelper> getAllMetadataByType(LegacyMetadataTypeHelper metadataType) {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    /**
     * Returns incoming or outgoing {@code Reference}s.
     *
     * @param direction
     *            can be "{@code to}" or "{@code from}". String seems to be
     *            always "{@code to}".
     * @return incoming or outgoing {@code Reference}s
     */
    @Deprecated
    default List<LegacyReferenceHelper> getAllReferences(String direction) {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    /**
     * Returns all references that are directed from this instance to another.
     * This are all {@code Reference}s in which this instance is the source.
     *
     * @return all outgoing {@code Reference}s
     */
    @Deprecated
    default Collection<LegacyReferenceHelper> getAllToReferences() {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

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
    @Deprecated
    default Collection<LegacyReferenceHelper> getAllToReferences(String type) {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

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
    @Deprecated
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
    @Deprecated
    default List<LegacyMetadataTypeHelper> getDisplayMetadataTypes() {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    /**
     * Returns the image name.
     *
     * @return the image name
     */
    @Deprecated
    default String getImageName() {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    /**
     * Returns the parent of this instance. Returns {@code null} if this
     * instance is the root of the tree.
     *
     * @return the parent, if any
     */
    @Deprecated
    default LegacyDocStructHelperInterface getParent() {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

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
    @Deprecated
    default List<LegacyMetadataTypeHelper> getPossibleMetadataTypes() {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    /**
     * Get the type of this DocStruct.
     *
     * @return the type of this DocStruct
     */
    @Deprecated
    default LegacyLogicalDocStructTypeHelper getDocStructType() {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    /**
     * Returns whether a {@code DocStruct} of the given {@code DocStructType} is
     * allowed to be added to this instance.
     *
     * @param type
     *            the {@code DocStructType} in question
     * @return true, if {@code DocStruct} of this type can be added; otherwise
     *         false
     */
    default boolean isDocStructTypeAllowedAsChild(LegacyLogicalDocStructTypeHelper type) {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    /**
     * Removes a child from this instance.
     *
     * @param docStruct
     *            to be removed
     */
    @Deprecated
    default void removeChild(LegacyDocStructHelperInterface docStruct) {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    /**
     * Removes a meta-datum from this instance. If (according to configuration)
     * at least one {@code Metadata} of this type is required on this instance,
     * the meta-datum will <i>not be removed</i>.
     *
     * @param metaDatum
     *            meta-datum which should be removed
     */
    @Deprecated
    default void removeMetadata(LegacyMetadataHelper metaDatum) {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    /**
     * Removes an outgoing reference. An outgoing reference is a reference to
     * another {@code DocStruct} instance. The corresponding incoming
     * {@code Reference} in the target {@code DocStruct} is also deleted.
     *
     * @param target
     *            {@code DocStruct}
     */
    @Deprecated
    default void removeReferenceTo(LegacyDocStructHelperInterface target) {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    /**
     * This method generates a comprehensible log message in case something was
     * overlooked and one of the unimplemented methods should ever be called in
     * operation. The name was chosen deliberately short in order to keep the
     * calling code clear.
     * 
     * @param exception
     *            created {@code UnsupportedOperationException}
     * @return the exception
     */
    static UnsupportedOperationException andLog(UnsupportedOperationException exception) {
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
