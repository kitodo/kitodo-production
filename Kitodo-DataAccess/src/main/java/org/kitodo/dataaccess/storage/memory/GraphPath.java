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

package org.kitodo.dataaccess.storage.memory;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.kitodo.dataaccess.IdentifiableNode;
import org.kitodo.dataaccess.Node;
import org.kitodo.dataaccess.NodeReference;
import org.kitodo.dataaccess.ObjectType;
import org.kitodo.dataaccess.RDF;
import org.kitodo.dataaccess.Result;
import org.kitodo.dataaccess.format.xml.Namespaces;

/**
 * A path to select objects through a linked data graph. The graph path is
 * basically the list of relations to follow, separated by blanks, where
 * {@code *} means <i>any relation</i>. After each relation specification,
 * conditions on the node can be specified in square brackets.
 *
 * <p>
 * Example graph path to get the work’s author from a METS XML file:
 *
 * <p>
 * {@code * [rdf:type mets:dmdSec] * [rdf:type mets:mdWrap] * [rdf:type
 * mets:xmlData] * [rdf:type mods:mods] * [rdf:type mods:name, mods:type
 * personal, * [rdf:type mods:role, * [rdf:type mods:roleTerm, rdf:_1 aut]]]
 * * [rdf:type mods:displayForm] rdf:_1}
 *
 * <p>
 * Inspired by XPath.
 */
public class GraphPath extends MemoryNode {

    /**
     * Indicates that the object must be referenced from the subject, but the
     * predicate of the reference is not specified.
     */
    public static final NodeReference ANY_PREDICATE = MemoryStorage.INSTANCE
            .createNodeReference("http://names.kitodo.org/GraphPath/v1#anyPredicate");

    /**
     * Character in the graph path string indicating that the object must be
     * referenced from the subject, but the predicate of the reference is not
     * specified.
     */
    private static final String ANY_PREDICATE_CHAR = "*";

    /**
     * This is a graph path.
     */
    private static final NodeReference GRAPH_PATH = MemoryStorage.INSTANCE
            .createNodeReference("http://names.kitodo.org/GraphPath/v1#GraphPath");

    /**
     * This is a location step of a graph path.
     */
    static final NodeReference LOCATION_STEP = MemoryStorage.INSTANCE
            .createNodeReference("http://names.kitodo.org/GraphPath/v1#LocationStep");

    /**
     * Direction of reference is forwards one step.
     */
    static final NodeReference TO = MemoryStorage.INSTANCE
            .createNodeReference("http://names.kitodo.org/GraphPath/v1#to");

    /**
     * Applies a graph path on a collection of nodes.
     *
     * <p>
     * Currently only supports relation type “to”.
     *
     * @param nodes
     *            nodes to apply the graph path on
     * @param path
     *            the graph path to apply
     * @return the matching nodes
     */
    public static Result apply(Set<ObjectType> nodes, Node path) {
        MemoryResult result = new MemoryResult();
        Set<Node> allTo = path.get(TO).nodes();
        Result nodeResult = nodes instanceof Result ? (Result) nodes : new MemoryResult();
        if (allTo.isEmpty() || nodes.isEmpty()) {
            return nodeResult;
        }
        Set<Node> nodesOnly = nodeResult.nodes();
        for (Node toSegment : allTo) {
            MemoryResult thisResult = new MemoryResult();
            Set<String> predicates = new HashSet<>();
            for (IdentifiableNode predicate : toSegment.get(RDF.PREDICATE).identifiableNodes()) {
                predicates.add(predicate.getIdentifier());
            }
            Collection<ObjectType> objects = new LinkedList<>();
            toSegment.get(RDF.OBJECT).forEach(objects::add);
            for (Node node : nodesOnly) {
                node.get(predicates, objects).forEach(thisResult::add);
            }
            apply(thisResult, toSegment).forEach(result::add);
        }
        return result;
    }

    /**
     * Replaces the prefix of the string, if any and specified in the map of
     * prefixes.
     *
     * @param prefixes
     *            map of prefixes, direction of mapping is prefix to namespace.
     * @param string
     *            the string to process
     * @return the string with the prefix replaced
     */
    private static String applyPrefixes(Map<String, String> prefixes, String string) {
        final int NOT_FOUND = -1;
        int colonIndex = string.indexOf(':');
        if (colonIndex == NOT_FOUND) {
            return string;
        }
        String prefix = string.substring(0, colonIndex);
        if (prefixes.containsKey(prefix)) {
            return Namespaces.concat(prefixes.get(prefix), string.substring(colonIndex + 1));
        }
        return string;
    }

    /**
     * Creates an empty GraphPath. An empty GraphPath points to the node it is
     * applied on.
     *
     * <p>
     * This method is used in tests to build comparable objects
     * programmatically.
     */
    protected GraphPath() {
        super(GRAPH_PATH);
    }

    /**
     * Creates a node representing the graph path string.
     *
     * @param string
     *            string to parse
     * @param prefixes
     *            a mapping of prefixes to namespaces which was used to shorten
     *            the string
     */
    public GraphPath(String string, Map<String, String> prefixes) {
        super(GRAPH_PATH);
        int index = 0;
        Node graphPosition = this;
        int length = string.length();
        while (index < length) {
            while ((index < length) && (string.codePointAt(index) <= ' ')) {
                index++;
            }
            if ((index < length) && (string.codePointAt(index) == '[')) {
                index++;
                Pair<Integer, Node> parseObjectRecursive = parseObject(string.substring(index), prefixes);
                index += parseObjectRecursive.getKey();
                index++;
                graphPosition.put(RDF.OBJECT, parseObjectRecursive.getValue());
            } else {
                Node nextLocationStep = new MemoryNode(LOCATION_STEP);
                NodeReference direction = RDF.NIL;
                switch (index < length ? string.codePointAt(index) : -1) {
                    case '<':
                        throw new IllegalArgumentException("Directive '<' not supported.");
                    case '>':
                        index++;
                        switch (index < length ? string.codePointAt(index) : -1) {
                            case '>':
                                index++;
                                if ((index < length) && (string.codePointAt(index) == '>')) {
                                    index++;
                                    throw new IllegalArgumentException("Directive '>|' not supported.");
                                } else {
                                    throw new IllegalArgumentException("Directive '>>' not supported.");
                                }
                            case '|':
                                throw new IllegalArgumentException("Directive '>|' not supported.");
                            default:
                                direction = TO;
                                break;
                        }
                        break;
                    case '|':
                        if (((index + 1) < length) && (string.codePointAt(index + 1) == '<')) {
                            throw new IllegalArgumentException("Directive '|<' not supported.");
                        }
                        break;
                    default:
                        direction = TO;
                        break;
                }
                while ((index < length) && (string.codePointAt(index) <= ' ')) {
                    index++;
                }
                graphPosition.put(direction, nextLocationStep);
                graphPosition = nextLocationStep;
                int predicatesStart = index;
                int codePoint;
                while ((index < length) && ((codePoint = string.codePointAt(index)) > ' ')) {
                    index += Character.charCount(codePoint);
                }
                String predicates = string.substring(predicatesStart, index);
                if (!predicates.equals(ANY_PREDICATE_CHAR)) {
                    for (String predicate : predicates.split("\\|")) {
                        graphPosition.put(RDF.PREDICATE, applyPrefixes(prefixes, predicate));
                    }
                }
            }
        }
    }

    /**
     * Parses an object from a graph path string.
     *
     * @param string
     *            string to parse
     * @return the number of code points consumed and the object parsed
     */
    private final Pair<Integer, Node> parseObject(String string, Map<String, String> prefixes) {
        int length = string.length();
        Node result = new MemoryNode();
        int index = 0;
        NodeReference currentPredicate = null;
        do {
            while ((index < length) && (string.codePointAt(index) <= ' ')) {
                index++;
            }
            if ((index >= length) || (string.codePointAt(index) == ']')) {
                return Pair.of(index, result);
            } else if (string.codePointAt(index) == ',') {
                index++;
                currentPredicate = null;
            } else if (string.codePointAt(index) == '[') {
                index++;
                Pair<Integer, Node> recursion = parseObject(string.substring(index), prefixes);
                index += recursion.getKey();
                index++;
                result.put(currentPredicate != null ? currentPredicate : ANY_PREDICATE, recursion.getValue());
            } else {
                if (currentPredicate == null) {
                    int predicatesStart = index;
                    int codePoint;
                    while ((index < length) && ((codePoint = string.codePointAt(index)) > ' ')) {
                        index += Character.charCount(codePoint);
                    }
                    String predicate = string.substring(predicatesStart, index);
                    currentPredicate = predicate.equals(ANY_PREDICATE_CHAR) ? ANY_PREDICATE
                            : MemoryStorage.INSTANCE.createNodeReference(applyPrefixes(prefixes, predicate));
                } else {
                    int literalStart = index;
                    int cp;
                    while ((index < length) && ((cp = string.codePointAt(index)) > ' ') && (cp != ',') && (cp != ']')) {
                        index += Character.charCount(cp);
                    }
                    String value = applyPrefixes(prefixes, string.substring(literalStart, index));
                    result.put(currentPredicate, MemoryLiteral.createLeaf(value, null));
                }
            }
        } while (index < length);
        return Pair.of(length, result);
    }
}
