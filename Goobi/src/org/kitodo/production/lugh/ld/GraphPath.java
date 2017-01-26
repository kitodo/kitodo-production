/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General private License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.kitodo.production.lugh.ld;

import java.util.*;

/**
 * A path to select objects through a linked data graph.
 *
 * The graph path is basically the list of relations to follow, separated by
 * blanks, where {@code *} means <i>any relation</i>. After each relation
 * specification, conditions on the node can be specified in square brackets
 * <p>
 * Example graph path to get the work’s author from a METS XML file:
 * <p>
 * {@code * [rdf:type mets:dmdSec] * [rdf:type mets:mdWrap] * [rdf:type
 * mets:xmlData] * [rdf:type mods:mods] * [rdf:type mods:name, mods:type
 * personal, * [rdf:type mods:role, * [rdf:type mods:roleTerm, rdf:_1 aut]]]
 * * [rdf:type mods:displayForm] rdf:_1}
 * <p>
 * Inspired by XPath.
 *
 * @author Matthias Ronge
 */
public class GraphPath extends Node {

    /**
     * Parser to recursively parse an object. The parsing method returns the
     * number of code points consumed, the result is provided in the field
     * {@code result}.
     *
     * @author Matthias Ronge
     */
    private static class ObjectParser {
        private final Map<String, String> prefixes;

        private final Node result = new Node();

        /**
         * Creates a new parser
         *
         * @param prefixes
         *            the map with prefixes to resolve
         */
        private ObjectParser(Map<String, String> prefixes) {
            this.prefixes = prefixes;
        }

        /**
         * Parses an object from a graph path string.
         *
         * @param string
         *            string to parse
         * @return the number of code points consumed
         */
        private int consume(String string) {
            int index = 0;
            int length = string.length();
            NodeReference currentPredicate = null;
            do {
                while ((index < length) && (string.codePointAt(index) <= ' ')) {
                    index++;
                }
                if ((index >= length) || (string.codePointAt(index) == ']')) {
                    return index;
                } else if (string.codePointAt(index) == ',') {
                    index++;
                    currentPredicate = null;
                } else if (string.codePointAt(index) == '[') {
                    index++;
                    ObjectParser recursion = new ObjectParser(prefixes);
                    index += recursion.consume(string.substring(index));
                    index++;
                    result.put(currentPredicate != null ? currentPredicate : ANY_PREDICATE, recursion.result);
                } else {
                    if (currentPredicate == null) {
                        int predicatesStart = index;
                        int codePoint;
                        while ((index < length) && ((codePoint = string.codePointAt(index)) > ' ')) {
                            index += Character.charCount(codePoint);
                        }
                        String predicate = string.substring(predicatesStart, index);
                        currentPredicate = predicate.equals(ANY_PREDICATE_CHAR) ? ANY_PREDICATE
                                : new NodeReference(applyPrefixes(prefixes, predicate));
                    } else {
                        int literalStart = index;
                        int cp;
                        while ((index < length) && ((cp = string.codePointAt(index)) > ' ') && (cp != ',')
                                && (cp != ']')) {
                            index += Character.charCount(cp);
                        }
                        String value = applyPrefixes(prefixes, string.substring(literalStart, index));
                        result.put(currentPredicate, Literal.create(value, null));
                    }
                }
            } while (index < length);
            return length;
        }
    }

    /**
     * Indicates that the object must be referenced from the subject, but the
     * predicate of the reference is not specified.
     */
    static final NodeReference ANY_PREDICATE = new NodeReference("http://names.zeutschel.de/GraphPath/v1#anyPredicate");

    /**
     * Character in the graph path string indicating that the object must be
     * referenced from the subject, but the predicate of the reference is not
     * specified.
     */
    private static final String ANY_PREDICATE_CHAR = "*";

    /**
     * This is a graph path.
     */
    private static final NodeReference GRAPH_PATH = new NodeReference(
            "http://names.zeutschel.de/GraphPath/v1#GraphPath");

    /**
     * This is a location step of a graph path.
     */
    private static final NodeReference LOCATION_STEP = new NodeReference(
            "http://names.zeutschel.de/GraphPath/v1#LocationStep");

    /**
     * Direction of reference is forwards one step.
     */
    private static final NodeReference TO = new NodeReference("http://names.zeutschel.de/GraphPath/v1#to");

    /**
     * Applies a graph path on a collection of nodes.
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
        Result result = new Result();

        Set<Node> allTo = path.get(TO).nodes();

        Result nodeResult = nodes instanceof Result ? (Result) nodes : new Result(nodes);

        if (allTo.isEmpty() || nodes.isEmpty()) {
            return nodeResult;
        }

        Set<Node> nodesOnly = nodeResult.nodes();

        for (Node toSegment : allTo) {
            Result thisResult = new Result();
            Set<String> predicates = new HashSet<>();
            for (IdentifiableNode predicate : toSegment.get(RDF.PREDICATE).identifiableNodes()) {
                predicates.add(predicate.getIdentifier());
            }
            Set<ObjectType> objects = toSegment.get(RDF.OBJECT);
            for (Node node : nodesOnly) {
                thisResult.addAll(node.get(predicates, objects));
            }
            result.addAll(apply(thisResult, toSegment));
        }

        // TODO: Implement other relation types as well
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
            return Namespace.concat(prefixes.get(prefix), string.substring(colonIndex + 1));
        }
        return string;
    }

    /**
     * Parse a graph path string.
     *
     * @param string
     *            string to parse
     * @param prefixes
     *            a mapping of prefixes to namespaces which was used to shorten
     *            the string
     * @return a node representing the graph path
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

                ObjectParser parseObjectRecursive = new ObjectParser(prefixes);
                index += parseObjectRecursive.consume(string.substring(index));
                index++;
                graphPosition.put(RDF.OBJECT, parseObjectRecursive.result);

            } else {

                Node nextLocationStep = new Node(LOCATION_STEP);
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
}
