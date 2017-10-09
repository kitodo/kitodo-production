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

package org.kitodo.lugh;

import java.nio.BufferOverflowException;
import java.util.*;
import java.util.function.Supplier;

/**
 * The results returned by a call to a getter on a node.
 */
public interface Result extends Set<ObjectType> {

    /**
     * Returns the accessible object.
     *
     * @return the accessible object
     * @throws LinkedDataException
     *             {@link NoDataException} if there is no accessible object,
     *             {@link AmbiguousDataException} if there are several possible
     *             answers
     */
    default AccessibleObject accessibleObject() throws LinkedDataException {
        return singleton(AccessibleObject.class);
    }

    /**
     * Returns the accessible object. This should only be called if its presence
     * was previously checked by {@link #isUniqueAccessibleObject()}.
     *
     * @return the accessible object
     * @throws NoSuchElementException
     *             if there is no named node
     * @throws BufferOverflowException
     *             if there are several possible answers
     */
    default AccessibleObject accessibleObjectExpectable() {
        return expectableSingleton(AccessibleObject.class);
    }

    /**
     * Returns the accessible object if unique, otherwise returns {@code other}.
     *
     * @param other
     *            the value to be returned if there is no unique accessible
     *            object
     *
     * @return the accessible object, if present, otherwise other
     */
    default AccessibleObject accessibleObjectOrElse(AccessibleObject other) {
        Set<AccessibleObject> accessibleObject = subset(AccessibleObject.class);
        return (accessibleObject.size() == 1) && (size() == 1) ? accessibleObject.iterator().next() : other;
    }

    /**
     * Returns the accessible object if unique, otherwise invokes {@code other}
     * and return the result of that invocation.
     *
     * @param other
     *            a {@code Supplier} whose result is returned if there is no
     *            unique accessible object
     *
     * @return the accessible object, if present, otherwise the result of
     *         {@code other.get()}
     */
    default AccessibleObject accessibleObjectOrElseGet(Supplier<AccessibleObject> other) {
        Set<AccessibleObject> accessibleObject = subset(AccessibleObject.class);
        return (accessibleObject.size() == 1) && (size() == 1) ? accessibleObject.iterator().next() : other.get();
    }

    /**
     * Returns all accessible objects.
     *
     * @return all accessible objects
     */
    default Set<AccessibleObject> accessibleObjects() {
        return subset(AccessibleObject.class);
    }

    /**
     * Count the elements in this result that can be cast to the given class, at
     * least up to the given limit. If there are more elements, the function may
     * or may not return a value equal to or larger than limit.
     *
     * @param clazz
     *            object class to count
     * @param limit
     *            limit to count up to, at least
     * @return the number of elements in this result that can be cast to the
     *         given class
     */
    long count(Class<? extends ObjectType> clazz, int limit);

    /**
     * Returns the contained element. This should only be called if its presence
     * was previously checked by {@link #isUnique()}.
     *
     * @return the contained element
     * @throws NoSuchElementException
     *             if there is no contained element
     * @throws BufferOverflowException
     *             if there are several possible answers
     */
    default ObjectType expectable() {
        return expectableSingleton(ObjectType.class);
    }

    /**
     * Returns the contained object. This method should be used if the presence
     * of exactly one object was previously checked by {@link #isUnique()}.
     *
     * @param <T>
     *            subclass type to return
     *
     * @param clazz
     *            subclass type to return
     *
     * @return the contained object
     * @throws NoSuchElementException
     *             if the result is empty
     * @throws BufferOverflowException
     *             if there is more than one result
     */

    default <T extends ObjectType> T expectableSingleton(Class<T> clazz) {
        Set<T> elements = subset(clazz);
        switch (elements.size()) {
            case 0:
                throw new NoSuchElementException(); // No element of clazz
            case 1:
                if (size() > 1) {
                    throw new BufferOverflowException(); // Element of clazz
                                                         // intermingled with
                                                         // other objects
                }
                return (T) elements.iterator().next();
            default:
                throw new BufferOverflowException(); // More than one element of
                                                     // clazz
        }
    }

    /**
     * Returns the identifiable node.
     *
     * @return the identifiable node
     * @throws LinkedDataException
     *             {@link NoDataException} if there is no identifiable node,
     *             {@link AmbiguousDataException} if there are several possible
     *             answers
     */
    default IdentifiableNode identifiableNode() throws LinkedDataException {
        return singleton(IdentifiableNode.class);
    }

    /**
     * Returns the identifiable node. This should only be called if its presence
     * was previously checked by {@link #isUniqueIdentifiableNode()}.
     *
     * @return the identifiable node
     * @throws NoSuchElementException
     *             if there is no identifiable node
     * @throws BufferOverflowException
     *             if there are several possible answers
     */
    default IdentifiableNode identifiableNodeExpectable() {
        return expectableSingleton(IdentifiableNode.class);
    }

    /**
     * Returns the identifiable node if unique, otherwise returns {@code other}.
     *
     * @param other
     *            the value to be returned if there is no unique identifiable
     *            node
     *
     * @return the identifiable node, if present, otherwise other
     */
    default IdentifiableNode identifiableNodeOrElse(IdentifiableNode other) {
        Set<IdentifiableNode> identifiableNode = subset(IdentifiableNode.class);
        return (identifiableNode.size() == 1) && (size() == 1) ? identifiableNode.iterator().next() : other;
    }

    /**
     * Returns the identifiable node if unique, otherwise invokes {@code other}
     * and return the result of that invocation.
     *
     * @param other
     *            a {@code Supplier} whose result is returned if there is no
     *            unique identifiable node
     *
     * @return the identifiable node, if present, otherwise the result of
     *         {@code other.get()}
     */
    default IdentifiableNode identifiableNodeOrElseGet(Supplier<IdentifiableNode> other) {
        Set<IdentifiableNode> identifiableNode = subset(IdentifiableNode.class);
        return (identifiableNode.size() == 1) && (size() == 1) ? identifiableNode.iterator().next() : other.get();
    }

    /**
     * Returns all identifiable nodes.
     *
     * @return all identifiable nodes
     */
    default Set<IdentifiableNode> identifiableNodes() {
        return subset(IdentifiableNode.class);
    }

    /**
     * Returns true, if there is at least one element in this Result.
     *
     * @return true, if there is at least one element
     */
    default boolean isAny() {
        return count(ObjectType.class, 1) > 0;
    }

    /**
     * Returns true, if there is at least one accessible object.
     *
     * @return true, if there is at least one accessible object
     */
    default boolean isAnyAccessibleObject() {
        return count(AccessibleObject.class, 1) > 0;
    }

    /**
     * Returns true, if there is at least one identifiable node.
     *
     * @return true, if there is at least one identifiable node
     */
    default boolean isAnyIdentifiableNode() {
        return count(IdentifiableNode.class, 1) > 0;
    }

    /**
     * Returns true, if there is at least one language-tagged string.
     *
     * @return true, if there is at least one language-tagged string
     */
    default boolean isAnyLangString() {
        return count(LangString.class, 1) > 0;
    }

    /**
     * Returns true, if there is at least one literal.
     *
     * @return true, if there is at least one literal
     */
    default boolean isAnyLiteral() {
        return count(Literal.class, 1) > 0;
    }

    /**
     * Returns true, if there is at least one named node.
     *
     * @return true, if there is at least one named node
     */
    default boolean isAnyNamedNode() {
        return count(NamedNode.class, 1) > 0;
    }

    /**
     * Returns true, if there is at least one node.
     *
     * @return true, if there is at least one node
     */
    default boolean isAnyNode() {
        return count(Node.class, 1) > 0;
    }

    /**
     * Returns true, if there is at least one node reference.
     *
     * @return true, if there is at least one node reference
     */
    default boolean isAnyNodeReference() {
        return count(NodeReference.class, 1) > 0;
    }

    /**
     * Returns true, if there is at least one node type.
     *
     * @return true, if there is at least one node type
     */
    default boolean isAnyNodeType() {
        return count(NodeType.class, 1) > 0;
    }

    /**
     * Tests whether this result contains exactly one object of the given class.
     *
     * @param clazz
     *            class to look for
     * @return whether this contains exactly one object of that class
     */
    default boolean isSingleton(Class<? extends ObjectType> clazz) {
        return (count(clazz, 2) == 1) && (count(ObjectType.class, 2) == 1);
    }

    /**
     * Returns true, if there is exactly one element in this Result.
     *
     * @return true, if there is exactly one element
     */
    default boolean isUnique() {
        return count(ObjectType.class, 2) == 1;
    }

    /**
     * Returns true, if there is exactly one accessible object.
     *
     * @return true, if there is exactly one accessible object
     */
    default boolean isUniqueAccessibleObject() {
        return (count(AccessibleObject.class, 2) == 1) && (count(ObjectType.class, 2) == 1);
    }

    /**
     * Returns true, if there is exactly one identifiable node.
     *
     * @return true, if there is exactly one identifiable node
     */
    default boolean isUniqueIdentifiableNode() {
        return (count(IdentifiableNode.class, 2) == 1) && (count(ObjectType.class, 2) == 1);
    }

    /**
     * Returns true, if there is exactly one language-tagged string.
     *
     * @return true, if there is exactly one language-tagged string
     */
    default boolean isUniqueLangString() {
        return (count(LangString.class, 2) == 1) && (count(ObjectType.class, 2) == 1);
    }

    /**
     * Returns true, if there is exactly one literal.
     *
     * @return true, if there is exactly one literal
     */
    default boolean isUniqueLiteral() {
        return (count(Literal.class, 2) == 1) && (count(ObjectType.class, 2) == 1);
    }

    /**
     * Returns true, if there is exactly one named node.
     *
     * @return true, if there is exactly one named node
     */
    default boolean isUniqueNamedNode() {
        return (count(NamedNode.class, 2) == 1) && (count(ObjectType.class, 2) == 1);
    }

    /**
     * Returns true, if there is exactly one node.
     *
     * @return true, if there is exactly one node
     */
    default boolean isUniqueNode() {
        return (count(Node.class, 2) == 1) && (count(ObjectType.class, 2) == 1);
    }

    /**
     * Returns true, if there is exactly one node reference.
     *
     * @return true, if there is exactly one node reference
     */
    default boolean isUniqueNodeReference() {
        return (count(NodeReference.class, 2) == 1) && (count(ObjectType.class, 2) == 1);
    }

    /**
     * Returns true, if there is exactly one node type.
     *
     * @return true, if there is exactly one node type
     */
    default boolean isUniqueNodeType() {
        return (count(NodeType.class, 2) == 1) && (count(ObjectType.class, 2) == 1);
    }

    /**
     * Returns the language-tagged string.
     *
     * @return the language-tagged string
     * @throws LinkedDataException
     *             {@link NoDataException} if there is no language-tagged
     *             string, {@link AmbiguousDataException} if there are several
     *             possible answers
     */
    default LangString langString() throws LinkedDataException {
        return singleton(LangString.class);
    }

    /**
     * Returns the language-tagged string. This should only be called if its
     * presence was previously checked by {@link #isUniqueLangString()}.
     *
     * @return the language-tagged string
     * @throws NoSuchElementException
     *             if there is no language-tagged string
     * @throws BufferOverflowException
     *             if there are several possible answers
     */
    default LangString langStringExpectable() {
        return expectableSingleton(LangString.class);
    }

    /**
     * Returns the language-tagged string if unique, otherwise returns
     * {@code other}.
     *
     * @param other
     *            the value to be returned if there is no unique language-tagged
     *            string
     *
     * @return the language-tagged string, if present, otherwise other
     */
    default LangString langStringOrElse(LangString other) {
        Set<LangString> langString = subset(LangString.class);
        return (langString.size() == 1) && (size() == 1) ? langString.iterator().next() : other;
    }

    /**
     * Returns the language-tagged string if unique, otherwise invokes
     * {@code other} and return the result of that invocation.
     *
     * @param other
     *            a {@code Supplier} whose result is returned if there is no
     *            unique language-tagged string
     *
     * @return the language-tagged string, if present, otherwise the result of
     *         {@code other.get()}
     */
    default LangString langStringOrElseGet(Supplier<LangString> other) {
        Set<LangString> langString = subset(LangString.class);
        return (langString.size() == 1) && (size() == 1) ? langString.iterator().next() : other.get();
    }

    /**
     * Returns all language-tagged strings.
     *
     * @return all language-tagged strings
     */
    default Set<LangString> langStrings() {
        return subset(LangString.class);
    }

    /**
     * Returns all leaves as strings. Leaves are literals or node references’
     * URIs.
     *
     * @return the literal
     */
    Set<String> leaves();

    /**
     * Returns all leaves from this node, joined by the given separator. Leaves
     * are literals or node references’ URIs.
     *
     * @param separator
     *            separator to use
     * @return all literals from this node as String
     */
    String leaves(String separator);

    /**
     * Returns the literal.
     *
     * @return the literal
     * @throws LinkedDataException
     *             {@link NoDataException} if there is no literal,
     *             {@link AmbiguousDataException} if there are several possible
     *             answers
     */
    default Literal literal() throws LinkedDataException {
        return singleton(Literal.class);
    }

    /**
     * Returns the literal. This should only be called if its presence was
     * previously checked by {@link #isUniqueLiteral()}.
     *
     * @return the literal
     * @throws NoSuchElementException
     *             if there is no literal
     * @throws BufferOverflowException
     *             if there are several possible answers
     */
    default Literal literalExpectable() {
        return expectableSingleton(Literal.class);
    }

    /**
     * Returns the literal if unique, otherwise returns {@code other}.
     *
     * @param other
     *            the value to be returned if there is no unique literal
     *
     * @return the literal, if present, otherwise other
     */
    default Literal literalOrElse(Literal other) {
        Set<Literal> literal = subset(Literal.class);
        return (literal.size() == 1) && (size() == 1) ? literal.iterator().next() : other;
    }

    /**
     * Returns the literal if unique, otherwise invokes {@code other} and return
     * the result of that invocation.
     *
     * @param other
     *            a {@code Supplier} whose result is returned if there is no
     *            unique literal
     *
     * @return the literal, if present, otherwise the result of
     *         {@code other.get()}
     */
    default Literal literalOrElseGet(Supplier<Literal> other) {
        Set<Literal> literal = subset(Literal.class);
        return (literal.size() == 1) && (size() == 1) ? literal.iterator().next() : other.get();
    }

    /**
     * Returns all literals.
     *
     * @return all literals
     */
    default Set<Literal> literals() {
        return subset(Literal.class);
    }

    /**
     * Returns the named node.
     *
     * @return the named node
     * @throws LinkedDataException
     *             {@link NoDataException} if there is no named node,
     *             {@link AmbiguousDataException} if there are several possible
     *             answers
     */
    default NamedNode namedNode() throws LinkedDataException {
        return singleton(NamedNode.class);
    }

    /**
     * Returns the named node. This should only be called if its presence was
     * previously checked by {@link #isUniqueNamedNode()}.
     *
     * @return the named node
     * @throws NoSuchElementException
     *             if there is no named node
     * @throws BufferOverflowException
     *             if there are several possible answers
     */
    default NamedNode namedNodeExpectable() {
        return expectableSingleton(NamedNode.class);
    }

    /**
     * Returns the named node if unique, otherwise returns {@code other}.
     *
     * @param other
     *            the value to be returned if there is no unique named node
     *
     * @return the named node, if present, otherwise other
     */
    default NamedNode namedNodeOrElse(NamedNode other) {
        Set<NamedNode> namedNode = subset(NamedNode.class);
        return (namedNode.size() == 1) && (size() == 1) ? namedNode.iterator().next() : other;
    }

    /**
     * Returns the named node if unique, otherwise invokes {@code other} and
     * return the result of that invocation.
     *
     * @param other
     *            a {@code Supplier} whose result is returned if there is no
     *            unique named node
     *
     * @return the named node, if present, otherwise the result of
     *         {@code other.get()}
     */
    default NamedNode namedNodeOrElseGet(Supplier<NamedNode> other) {
        Set<NamedNode> namedNode = subset(NamedNode.class);
        return (namedNode.size() == 1) && (size() == 1) ? namedNode.iterator().next() : other.get();
    }

    /**
     * Returns all named nodes.
     *
     * @return all named nodes
     */
    default Set<NamedNode> namedNodes() {
        return subset(NamedNode.class);
    }

    /**
     * Returns the node.
     *
     * @return the node
     * @throws LinkedDataException
     *             {@link NoDataException} if there is no node,
     *             {@link AmbiguousDataException} if there are several possible
     *             answers
     */
    default Node node() throws LinkedDataException {
        return singleton(Node.class);
    }

    /**
     * Returns the node. This should only be called if its presence was
     * previously checked by {@link #isUniqueNode()}.
     *
     * @return the node
     * @throws NoSuchElementException
     *             if there is no node
     * @throws BufferOverflowException
     *             if there are several possible answers
     */
    default Node nodeExpectable() {
        return expectableSingleton(Node.class);
    }

    /**
     * Returns the node if unique, otherwise returns {@code other}.
     *
     * @param other
     *            the value to be returned if there is no unique node
     *
     * @return the node, if present, otherwise other
     */
    default Node nodeOrElse(Node other) {
        Set<Node> node = subset(Node.class);
        return (node.size() == 1) && (size() == 1) ? node.iterator().next() : other;
    }

    /**
     * Returns the node if unique, otherwise invokes {@code other} and return
     * the result of that invocation.
     *
     * @param other
     *            a {@code Supplier} whose result is returned if there is no
     *            unique node
     *
     * @return the node, if present, otherwise the result of {@code other.get()}
     */
    default Node nodeOrElseGet(Supplier<Node> other) {
        Set<Node> node = subset(Node.class);
        return (node.size() == 1) && (size() == 1) ? node.iterator().next() : other.get();
    }

    /**
     * Returns the node reference.
     *
     * @return the node reference
     * @throws LinkedDataException
     *             {@link NoDataException} if there is no node reference,
     *             {@link AmbiguousDataException} if there are several possible
     *             answers
     */
    default NodeReference nodeReference() throws LinkedDataException {
        return singleton(NodeReference.class);
    }

    /**
     * Returns the node reference. This should only be called if its presence
     * was previously checked by {@link #isUniqueNodeReference()}.
     *
     * @return the node reference
     * @throws NoSuchElementException
     *             if there is no node reference
     * @throws BufferOverflowException
     *             if there are several possible answers
     */
    default NodeReference nodeReferenceExpectable() {
        return expectableSingleton(NodeReference.class);
    }

    /**
     * Returns the node reference if unique, otherwise returns {@code other}.
     *
     * @param other
     *            the value to be returned if there is no unique node reference
     *
     * @return the node reference, if present, otherwise other
     */
    default NodeReference nodeReferenceOrElse(NodeReference other) {
        Set<NodeReference> nodeReference = subset(NodeReference.class);
        return (nodeReference.size() == 1) && (size() == 1) ? nodeReference.iterator().next() : other;
    }

    /**
     * Returns the node reference if unique, otherwise invokes {@code other} and
     * return the result of that invocation.
     *
     * @param other
     *            a {@code Supplier} whose result is returned if there is no
     *            unique node reference
     *
     * @return the node reference, if present, otherwise the result of
     *         {@code other.get()}
     */
    default NodeReference nodeReferenceOrElseGet(Supplier<NodeReference> other) {
        Set<NodeReference> nodeReference = subset(NodeReference.class);
        return (nodeReference.size() == 1) && (size() == 1) ? nodeReference.iterator().next() : other.get();
    }

    /**
     * Returns all node reference.
     *
     * @return all node reference
     */
    default Set<NodeReference> nodeReferences() {
        return subset(NodeReference.class);
    }

    /**
     * Returns all nodes.
     *
     * @return all nodes
     */
    default Set<Node> nodes() {
        return subset(Node.class);
    }

    /**
     * Returns the node type.
     *
     * @return the node type
     * @throws LinkedDataException
     *             {@link NoDataException} if there is no node type,
     *             {@link AmbiguousDataException} if there are several possible
     *             answers
     */
    default NodeType nodeType() throws LinkedDataException {
        return singleton(NodeType.class);
    }

    /**
     * Returns the node type. This should only be called if its presence was
     * previously checked by {@link #isUniqueNodeType()}.
     *
     * @return the nodeType
     * @throws NoSuchElementException
     *             if there is no node type
     * @throws BufferOverflowException
     *             if there are several possible answers
     */
    default NodeType nodeTypeExpectable() {
        return expectableSingleton(NodeType.class);
    }

    /**
     * Returns the node type if unique, otherwise returns {@code other}.
     *
     * @param other
     *            the value to be returned if there is no unique node type
     *
     * @return the nodeType, if present, otherwise other
     */
    default NodeType nodeTypeOrElse(NodeType other) {
        Set<NodeType> nodeType = subset(NodeType.class);
        return (nodeType.size() == 1) && (size() == 1) ? nodeType.iterator().next() : other;
    }

    /**
     * Returns the node type if unique, otherwise invokes {@code other} and
     * return the result of that invocation.
     *
     * @param other
     *            a {@code Supplier} whose result is returned if there is no
     *            unique node type
     *
     * @return the nodeType, if present, otherwise the result of
     *         {@code other.get()}
     */
    default NodeType nodeTypeOrElseGet(Supplier<NodeType> other) {
        Set<NodeType> nodeType = subset(NodeType.class);
        return (nodeType.size() == 1) && (size() == 1) ? nodeType.iterator().next() : other.get();
    }

    /**
     * Returns all node types.
     *
     * @return all node types
     */
    default Set<NodeType> nodeTypes() {
        return subset(NodeType.class);
    }

    /**
     * Returns the contained element if unique, otherwise returns {@code other}.
     *
     * @param other
     *            the value to be returned if there is no unique element
     *
     * @return the contained element, if present, otherwise other
     */
    default ObjectType orElse(ObjectType other) {
        return count(ObjectType.class, 2) == 1 ? iterator().next() : other;
    }

    /**
     * Returns the contained element if unique, otherwise invokes {@code other}
     * and return the result of that invocation.
     *
     * @param other
     *            a {@code Supplier} whose result is returned if there is no
     *            unique element
     *
     * @return the contained element, if present, otherwise the result of
     *         {@code other.get()}
     */
    default ObjectType orElseGet(Supplier<ObjectType> other) {
        return count(ObjectType.class, 2) == 1 ? iterator().next() : other.get();
    }

    /**
     * Returns the singleton value object by class.
     *
     * @param <T>
     * @param clazz
     * @return the singleton value object
     * @throws LinkedDataException
     *             {@link NoDataException} if there is no such object,
     *             {@link AmbiguousDataException} if there are several possible
     *             answers
     */
    default <T extends ObjectType> T singleton(Class<T> clazz) throws LinkedDataException {
        Set<T> elements = subset(clazz);
        switch (elements.size()) {
            case 0:
                throw new NoDataException(); // No element of clazz
            case 1:
                if (size() > 1) {
                    throw new AmbiguousDataException(); // Element of clazz
                                                        // intermingled with
                                                        // other objects
                }
                return (T) elements.iterator().next();
            default:
                throw new AmbiguousDataException(); // More than one element of
                                                    // clazz
        }
    }

    /**
     * Returns all the literals as strings.
     *
     * @return the literal
     */
    Set<String> strings();

    /**
     * Returns all literals from this node, joined by the given separator.
     *
     * @param separator
     *            separator to use
     * @return all literals from this node as String
     */
    String strings(String separator);

    /**
     * Returns all elements from this result that can be cast to the given
     * subclass.
     *
     * @param <T>
     *            subclass to filter for
     * @param clazz
     *            subclass to filter for
     * @return all elements that can be cast to the subclass
     */
    <T extends ObjectType> Set<T> subset(Class<T> clazz);

    /**
     * Returns the contained element.
     *
     * @return the contained element
     * @throws LinkedDataException
     *             {@link NoDataException} if there is no contained element,
     *             {@link AmbiguousDataException} if there are several possible
     *             answers
     */
    default ObjectType value() throws LinkedDataException {
        return singleton(ObjectType.class);
    }

}
