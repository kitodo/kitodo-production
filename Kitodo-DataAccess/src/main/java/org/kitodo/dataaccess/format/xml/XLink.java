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

package org.kitodo.dataaccess.format.xml;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.kitodo.dataaccess.IdentifiableNode;
import org.kitodo.dataaccess.LangString;
import org.kitodo.dataaccess.Node;
import org.kitodo.dataaccess.NodeReference;
import org.kitodo.dataaccess.RDF;
import org.kitodo.dataaccess.Storage;

/**
 * The {@code http://www.w3.org/1999/xlink} namespace.
 */
public enum XLink implements NodeReference {

    /**
     * Communicate the desired timing of traversal from the starting resource to
     * the ending resource.
     */
    ACTUATE("http://www.w3.org/1999/xlink#actuate"),

    /**
     * Describes the relation between the starting resource and the ending
     * resource. When expressing statements, the value is the URI of the
     * predicate of the statement.
     */
    ARCROLE("http://www.w3.org/1999/xlink#arcrole"),

    /**
     * Defines resources from which traversal may be initiated. When expressing
     * statements, the value is the URI of the (named) subject of the statement.
     */
    FROM("http://www.w3.org/1999/xlink#from"),

    /**
     * Defines the target of a simple link. Unlike {@link #TO}, applications are
     * explicitly not required to check that the value of the xlink:href
     * attribute conforms to the syntactic rules of a URI.
     */
    HREF("http://www.w3.org/1999/xlink#href"),

    /**
     * Adds an identifier to any XML element to provide for referencing that
     * element using {@link #FROM} and {@link #TO}.
     */
    LABEL("http://www.w3.org/1999/xlink#label"),

    /**
     * Describes the role of the ending resource.
     */
    ROLE("http://www.w3.org/1999/xlink#role"),

    /**
     * Communicates the desired presentation of the ending resource on traversal
     * from the starting resource.
     */
    SHOW("http://www.w3.org/1999/xlink#show"),

    /**
     * Describes the meaning of a link or resource in a human-readable fashion.
     */
    TITLE("http://www.w3.org/1999/xlink#title"),

    /**
     * Defines resources that may be traversed to. When expressing statements,
     * the value is the URI of the (named) object of the statement.
     */
    TO("http://www.w3.org/1999/xlink#to"),
    TYPE("http://www.w3.org/1999/xlink#type");

    public enum Actuate {
        /**
         * The behavior of an application traversing to the ending resource is
         * unconstrained by this specification. No other markup is present to
         * help the application determine the appropriate behavior.
         */
        NONE("none"),

        /**
         * An application should traverse to the ending resource immediately on
         * loading the starting resource.
         */
        ON_LOAD("onLoad"),

        /**
         * An application should traverse from the starting resource to the
         * ending resource only on a post-loading event triggered for the
         * purpose of traversal.
         */
        ON_REQUEST("onRequest"),

        /**
         * The behavior of an application traversing to the ending resource is
         * unconstrained by this specification. The application should look for
         * other markup present in the link to determine the appropriate
         * behavior.
         */
        OTHER("other");

        private static final Map<String, Actuate> reversed = new HashMap<>(6);

        static {
            for (Actuate c : Actuate.values()) {
                reversed.put(c.name, c);
            }
        }

        /**
         * Returns the enum constant for a string value.
         *
         * @param name
         *            enum constant name to resolve
         * @return the enum constant
         */
        public static Actuate getEnum(String name) {
            if (!reversed.containsKey(name)) {
                throw new IllegalArgumentException("Unknown String Value: " + name);
            }
            return reversed.get(name);
        }

        private String name;

        Actuate(String name) {
            this.name = name;
        }

        /**
         * Returns the string value for this constant.
         */
        @Override
        public String toString() {
            return name;
        }
    }

    public enum Show {
        /**
         * An application traversing to the ending resource should load its
         * presentation in place of the link into the presentation of the
         * starting resource.
         */
        EMBED("embed"),

        /**
         * An application traversing to the ending resource should load it in a
         * new presentation context.
         */
        NEW("new"),

        /**
         * The behavior of an application traversing to the ending resource is
         * unconstrained by this specification. No other markup is present to
         * help the application determine the appropriate behavior.
         */
        NONE("none"),

        /**
         * The behavior of an application traversing to the ending resource is
         * unconstrained by this specification. The application should look for
         * other markup present in the link to determine the appropriate
         * behavior.
         */
        OTHER("other"),

        /**
         * An application traversing to the ending resource should load the
         * resource in the presentation context in which the starting resource
         * was loaded before, replacing it.
         */
        REPLACE("replace");

        private static final Map<String, Show> reversed = new HashMap<>(7);

        static {
            for (Show c : Show.values()) {
                reversed.put(c.name, c);
            }
        }

        /**
         * Returns the enum constant for a string value.
         *
         * @param name
         *            enum constant name to resolve
         * @return the enum constant
         */
        public static Show getEnum(String name) {
            if (!reversed.containsKey(name)) {
                throw new IllegalArgumentException("Unknown String Value: " + name);
            }
            return reversed.get(name);
        }

        private String name;

        Show(String name) {
            this.name = name;
        }

        /**
         * Returns the string value for this constant.
         */
        @Override
        public String toString() {
            return name;
        }
    }

    public enum Type {
        /**
         * {@code xlink:type="arc"}.
         *
         * <p>
         * Requited attribute: {@link #TYPE}
         *
         * <p>
         * Optional attributes: {@link #ACTUATE}, {@link #ARCROLE},
         * {@link #FROM}, {@link #SHOW}, {@link #TITLE}, {@link #TO}
         */
        ARC("arc"),

        /**
         * {@code xlink:type="extended"}.
         *
         * <p>
         * Requited attribute: {@link #TYPE}
         *
         * <p>
         * Optional attributes: {@link #ROLE}, {@link #TITLE}
         */
        EXTENDED("extended"),

        /**
         * {@code xlink:type="locator"}.
         *
         * <p>
         * Requited attribute: {@link #HREF}, {@link #TYPE}
         *
         * <p>
         * Optional attributes: {@link #LABEL}, {@link #ROLE}, {@link #TITLE}
         */
        LOCATOR("locator"),

        /**
         * {@code xlink:type="none"}.
         *
         * <p>
         * When the value of the type attribute is "none", the element has no
         * XLink-specified meaning, and any XLink-related content or attributes
         * have no XLink-specified relationship to the element.
         */
        NONE("none"),

        /**
         * {@code xlink:type="resource"}.
         *
         * <p>
         * Requited attribute: {@link #TYPE}
         *
         * <p>
         * Optional attributes: {@link #LABEL}, {@link #ROLE}, {@link #TITLE}
         */
        RESOURCE("resource"),

        /**
         * {@code xlink:type="simple"}.
         *
         * <p>
         * A simple link. At least one of the attributes {@link #TYPE} or
         * {@link #HREF} must be specified.
         *
         * <p>
         * Optional attributes: {@link #ACTUATE}, {@link #ARCROLE},
         * {@link #ROLE}, {@link #SHOW}, {@link #TITLE}
         */
        SIMPLE("simple"),

        /**
         * {@code xlink:type="title"}.
         *
         * <p>
         * Requited attribute: {@link #TYPE}
         *
         * <p>
         * Optional attributes: none
         */
        TITLE("title");

        private static final Map<String, Type> reversed = new HashMap<>(10);

        static {
            for (Type c : Type.values()) {
                reversed.put(c.name, c);
            }
        }

        /**
         * Returns the enum constant for a string value.
         *
         * @param name
         *            enum constant name to resolve
         * @return the enum constant
         */
        public static Type getEnum(String name) {
            if (!reversed.containsKey(name)) {
                throw new IllegalArgumentException("Unknown String Value: " + name);
            }
            return reversed.get(name);
        }

        private String name;

        Type(String name) {
            this.name = name;
        }

        /**
         * Returns the string value for this constant.
         */
        @Override
        public String toString() {
            return name;
        }
    }

    public static final String NAMESPACE = "http://www.w3.org/1999/xlink#";

    /**
     * Creates an arc link.
     *
     * @param storage
     *            storage instance to create the link in
     * @param from
     *            Link origin
     * @param relation
     *            Relation description. For example:
     *            {@code http://purl.org/dc/terms/references}
     * @param to
     *            Link destination
     * @param title
     *            link title(s) as human readable text
     * @param actuate
     *            when to load the resource
     * @param show
     *            context to show the resource in
     * @return the created link
     */
    public static Node createArcLink(Storage storage, Optional<String> from, Optional<String> relation,
            Optional<String> to, Collection<LangString> title, Optional<Actuate> actuate, Optional<Show> show) {

        Node node = storage.createNode();
        node.put(TYPE, Type.ARC.name);
        from.ifPresent(value -> node.put(FROM, value));
        relation.ifPresent(value -> node.put(ARCROLE, value));
        to.ifPresent(value -> node.put(TO, value));
        if (title.size() == 1) {
            LangString entry = title.iterator().next();
            node.put(TITLE, entry.getValue());
            node.put(XML.LANG, entry.getLanguageTag());
        } else {
            title.forEach(entry -> node
                    .add(storage.createNode(TITLE).put(XML.LANG, entry.getLanguageTag()).setValue(entry.getValue())));
        }
        actuate.ifPresent(value -> node.put(ACTUATE, value.toString()));
        show.ifPresent(value -> node.put(SHOW, value.toString()));
        return node;
    }

    /**
     * Creates an extended link.
     *
     * @param storage
     *            storage instance to create the link in
     * @param type
     *            {@code rdf:type} of the linked resource
     * @param title
     *            link title as human readable text
     * @return the created link
     */
    public static Node createExtendedLink(Storage storage, Optional<String> type, Optional<LangString> title) {
        Node node = storage.createNode();
        node.put(TYPE, Type.EXTENDED.name);
        type.ifPresent(value -> node.put(ROLE, value));
        title.ifPresent(value -> node.put(TITLE, value.getValue()).put(XML.LANG, value.getLanguageTag()));
        return node;
    }

    /**
     * Creates a locator link.
     *
     * @param storage
     *            storage instance to create the link in
     * @param to
     *            link destination. If the node is a named node, the
     *            {@code rdf:type}s will be added as {@code xlink:role}s as
     *            well.
     * @param title
     *            link title as human readable text
     * @return the created link
     */
    public static Node createLocatorLink(Storage storage, IdentifiableNode to, Collection<LangString> title) {

        return createLocatorLink(storage, to.getIdentifier(),
            to instanceof Node ? ((Node) to).get(RDF.TYPE).leaves() : Collections.emptySet(), title);
    }

    /**
     * Creates a locator link.
     *
     * @param storage
     *            storage instance to create the link in
     * @param to
     *            link destination
     * @param type
     *            Collection of {@code rdf:type}s of the linked resource
     * @param title
     *            link title as human readable text
     * @return the created link
     */
    public static Node createLocatorLink(Storage storage, String to, Collection<String> type,
            Collection<LangString> title) {

        Node node = storage.createNode();
        node.put(TYPE, Type.LOCATOR.name);
        node.put(HREF, to);
        type.forEach(entry -> node.put(ROLE, entry));
        if (title.size() == 1) {
            LangString entry = title.iterator().next();
            node.put(TITLE, entry.getValue());
            node.put(XML.LANG, entry.getLanguageTag());
        } else {
            title.forEach(entry -> node
                    .add(storage.createNode(TITLE).put(XML.LANG, entry.getLanguageTag()).setValue(entry.getValue())));
        }
        return node;
    }

    /**
     * Creates a link to a local resource.
     *
     * @param storage
     *            storage to create the local resource in
     * @param localIdentifier
     *            a string locally identifying the resource
     * @param type
     *            the {@code rdf:type} of the resource
     * @param title
     *            a human readable title of the resource
     * @return the created link
     */
    public static Node createResourceLink(Storage storage, Optional<String> localIdentifier, Optional<String> type,
            Optional<LangString> title) {
        Node node = storage.createNode();
        node.put(TYPE, Type.RESOURCE.name);
        localIdentifier.ifPresent(value -> node.put(LABEL, value));
        type.ifPresent(value -> node.put(ROLE, value));
        title.ifPresent(value -> node.put(TITLE, value.getValue()).put(XML.LANG, value.getLanguageTag()));
        return node;
    }

    /**
     * Creates a simple link.
     *
     * @param storage
     *            storage instance to create the link in
     * @param relation
     *            Relation description. For example:
     *            {@code http://purl.org/dc/terms/references}
     * @param to
     *            link destination. If the node is a named node, the
     *            {@code rdf:type}s will be added as {@code xlink:role}s as
     *            well.
     * @param title
     *            link title(s) as human readable text
     * @param actuate
     *            when to load the resource
     * @param show
     *            context to show the resource in
     * @return the created link
     */
    public static Node createSimpleLink(Storage storage, Optional<IdentifiableNode> relation, IdentifiableNode to,
            Collection<LangString> title, Optional<Actuate> actuate, Optional<Show> show) {

        return createSimpleLink(storage, relation, to.getIdentifier(),
            to instanceof Node ? ((Node) to).get(RDF.TYPE).leaves() : Collections.emptySet(), title, actuate, show);
    }

    /**
     * Creates a simple link.
     *
     * @param storage
     *            storage instance to create the link in
     * @param relation
     *            Relation description. For example:
     *            {@code http://purl.org/dc/terms/references}
     * @param to
     *            link destination
     * @param type
     *            Collection of {@code rdf:type}s of the linked resource
     * @param title
     *            link title(s) as human readable text
     * @param actuate
     *            when to load the resource
     * @param show
     *            context to show the resource in
     * @return the created link
     */
    public static Node createSimpleLink(Storage storage, Optional<IdentifiableNode> relation, String to,
            Collection<String> type, Collection<LangString> title, Optional<Actuate> actuate, Optional<Show> show) {
        Node node = storage.createNode();
        node.put(HREF, to);
        type.forEach(entry -> node.put(ROLE, entry));
        relation.ifPresent(value -> node.put(ARCROLE, value.getIdentifier()));
        if (title.size() == 1) {
            LangString entry = title.iterator().next();
            node.put(TITLE, entry.getValue());
            node.put(XML.LANG, entry.getLanguageTag());
        } else {
            title.forEach(entry -> node
                    .add(storage.createNode(TITLE).put(XML.LANG, entry.getLanguageTag()).setValue(entry.getValue())));
        }
        actuate.ifPresent(value -> node.put(ACTUATE, value.toString()));
        show.ifPresent(value -> node.put(SHOW, value.toString()));
        return node;
    }

    private String identifier;

    /**
     * Enum constants constructor.
     *
     * @param identifier
     *            referenced URL
     */
    XLink(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public RDFNode toRDFNode(Model model, Boolean unused) {
        return model.createResource(identifier);
    }

    /**
     * Returns a version of this node reference which, in a debugger, will
     * symbolically represent it.
     */
    @Override
    public String toString() {
        return '↗' + identifier;
    }

}
