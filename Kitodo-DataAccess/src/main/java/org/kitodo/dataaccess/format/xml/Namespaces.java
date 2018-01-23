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

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.kitodo.dataaccess.RDF;
import org.kitodo.dataaccess.RDFS;

/**
 * Class to handle namespaces.
 *
 * <p>
 * Technically, a namespace is an arbitrary abbreviation of a URI. For example,
 * the URL {@code https://www.kitodo.org/software/kitodoproduction} can be
 * abbreviated as {@code a:oduction}, given the namespace prefix {@code o:} has
 * been declared as {@code https://www.kitodo.org/software/kitodopr}. However,
 * such abbreviations are not very useful.
 *
 * <p>
 * Compared to files in directories, the namespace can be considered as the
 * path. There are two variants of namespaces that must be distinguished: URIs
 * where the simple name is separated by a number sign ({@code #}), and URIs
 * where it is separated by a slash ({@code /}).
 *
 * <p>
 * <em>In Linked Data,</em> namespaces are syntactical sugar. Some Linked Data
 * formats do not even make use of namespaces at all. For the others, the
 * namespace should be considered as a part of the interface. Namespaces must be
 * declared the way that, concatenated with the simple name, they form the
 * complete URI.
 *
 * <p>
 * <em>For XML files,</em> if namespaces are to be used (they are optional),
 * they <strong>must</strong> be declared using namespace prefixes.
 * ({@code <mods:mods>} is a valid XML tag, while
 * {@code <http://www.loc.gov/mods/v3#mods>} is not.) For XML attributes, that
 * belong to the same namespace as the XML element on which they are declared,
 * the prefix can be omitted. ({@code <mets:structMap TYPE="LOGICAL">} is equal
 * to {@code <mets:structMap mets:TYPE="LOGICAL">}. The attribute {@code TYPE}
 * implicitly is a member of the namespace abbreviated as {@code mets:}, while
 * the value {@code "LOGICAL"} is not. The value is a plain string. This is an
 * oddity of the XML format.) Unlike any other format, namespaces ending with a
 * number sign are declared <strong>without</strong> the trailing number sign,
 * while namespaces ending in a slash are declared <strong>with</strong> the
 * trailing slash.
 */
public class Namespaces extends DualHashBidiMap<String, String> {
    /**
     * Maps the constant namespaces "xml:" and "xmlns:" to their reserved
     * prefixes. The map is populated in a static block below.
     */
    private static final Map<String, String> CONSTANTS = new HashMap<>(3);

    /**
     * The default "rdf:" prefix, which may however be overridden. In this
     * class, all prefix Strings are without the colon.
     */
    private static final String DEFAULT_PREFIX_RDF = "rdf";

    /**
     * The default "rdfs:" prefix, which may however be overridden. In this
     * class, all prefix Strings are without the colon.
     */
    private static final String DEFAULT_PREFIX_RDFS = "rdfs";

    /**
     * The "xml:" prefix which is reserved and must neither be used otherwise
     * nor be named differently. In this class, all prefix Strings are without
     * the colon.
     */
    private static final String PREFIX_XML = "xml";

    /**
     * The "xmlns:" prefix which is reserved and must neither be used otherwise
     * nor be named differently. In this class, all prefix Strings are without
     * the colon.
     */
    private static final String PREFIX_XMLNS = "xmlns";

    private static final long serialVersionUID = 1L;
    /**
     * The XMLNS namespace URL.
     */
    public static final String XMLNS_NAMESPACE = "http://www.w3.org/2000/xmlns/";

    /**
     * Populates the map with the constant namespaces "xml:" and "xmlns:".
     */
    static {
        CONSTANTS.put(XMLNS_NAMESPACE, PREFIX_XMLNS);
        CONSTANTS.put(XML.NAMESPACE, PREFIX_XML);
    }

    /**
     * Counter to create new namespace prefixes.
     */
    private long next;

    /**
     * The map of used namespaces in serializing an XML document. Mapping
     * direction is namespace to prefix, {@code #} namespaces are stored without
     * the {@code #} at the end, {@code /} namespaces are stored with a
     * {@code /} as last character.
     */
    private final Map<String, String> used;

    /**
     * Creates a new NamespaceHandler for a given Node which is considered the
     * root node of the XML document.
     */
    public Namespaces() {
        this(null);
    }

    /**
     * Creates a new NamespaceHandler for a given Node which is considered the
     * root node of the XML document.
     *
     * @param presets
     *            Presets of namespace shortcuts, mapped namespace to shortcut.
     *            May be null or empty.
     */
    public Namespaces(Map<String, String> presets) {
        super.put(DEFAULT_PREFIX_RDF, RDF.NAMESPACE);
        super.put(DEFAULT_PREFIX_RDFS, RDFS.NAMESPACE);
        if (presets != null) {
            super.putAll(presets);
        }
        used = new HashMap<>();
    }

    /**
     * Returns the abbreviated attribute. If the attribute is in the same
     * namespace as the element that will hold it, the prefix is omitted.
     *
     * @param element
     *            node the attribute is on
     * @param attribute
     *            attribute key to convert
     * @return abbreviated or basic attribute
     */
    String abbreviateAttribute(String element, String attribute) {
        String ns = namespaceOfForXMLFile(attribute);
        if (ns.isEmpty()) {
            return attribute;
        }
        available(ns);
        String tag = attribute.substring(ns.endsWith("/") ? ns.length() : ns.length() + 1);
        return namespaceOfForXMLFile(element).equals(ns) ? tag : used.get(ns) + ':' + tag;
    }

    /**
     * Returns the abbreviated element name.
     *
     * @param element
     *            URL to abbreviate for an element name
     * @return the abbreviated node name
     */
    String abbreviateElement(String element) {
        String ns = namespaceOfForXMLFile(element);
        if (ns.isEmpty()) {
            return element;
        }
        available(ns);
        return used.get(ns) + ':' + element.substring(ns.endsWith("/") ? ns.length() : ns.length() + 1);
    }

    /**
     * Returns a sequence of letters from a positive whole number.
     *
     * @param value
     *            number to convert
     * @return a, b, c, …, x, y, z, aa, ab, ac, …
     */
    private static String asLetters(long value) {
        int codePoint = (int) ('a' + (--value % 26));
        long higher = value / 26;
        String letter = new String(Character.toChars(codePoint));
        return higher == 0 ? letter : asLetters(higher).concat(letter);
    }

    /**
     * Grants that a prefix is available for a namespace. Missing prefixes are
     * either fetched from the constants or the presets or else are created
     * alphabetically ascending.
     *
     * @param namespace
     *            the namespace
     */
    private void available(String namespace) {
        if (!used.containsKey(namespace)) {
            if (CONSTANTS.containsKey(namespace)) {
                used.put(namespace, CONSTANTS.get(namespace));
            } else {
                String withHash = namespace.endsWith("/") || namespace.endsWith("#") ? namespace
                        : namespace.concat("#");
                BidiMap<String, String> inversedSuper = super.inverseBidiMap();
                if (inversedSuper.containsKey(withHash)) {
                    used.put(namespace, inversedSuper.get(withHash));
                } else {
                    String prefix;
                    do {
                        prefix = asLetters(++next);
                    } while (super.containsKey(prefix) || CONSTANTS.containsValue(prefix));
                    used.put(namespace, prefix);
                }
            }
        }
    }

    /**
     * Recomposes an URL from a namespace and a local name, adding a number sign
     * in between if the URL does not end in a slash.
     *
     * @param namespace
     *            namespace for URL
     * @param localName
     *            local name for URL
     * @return combined URL
     */
    public static String concat(String namespace, String localName) {
        boolean addNumberSign = !namespace.endsWith("/") && !namespace.endsWith("#");
        StringBuilder result = new StringBuilder(namespace.length() + (addNumberSign ? 1 : 0) + localName.length());
        result.append(namespace);
        if (addNumberSign) {
            result.append('#');
        }
        result.append(localName);
        return result.toString();
    }

    /**
     * Expands an abbreviated URL.
     *
     * @param abbreviatedUrl
     *            URL to expand
     * @return expanded URL
     */
    public String expand(String abbreviatedUrl) {
        return expand(abbreviatedUrl, this);
    }

    /**
     * Expands an abbreviated URL referencing a map of prefixes.
     *
     * @param abbreviatedUrl
     *            URL to expand
     * @param prefixes
     *            map of prefixes
     * @return expanded URL
     */
    public static String expand(String abbreviatedUrl, Map<String, String> prefixes) {
        String namespace = prefixes.get(getPrefix(abbreviatedUrl));
        if (namespace == null) {
            return abbreviatedUrl;
        }
        return concat(namespace, abbreviatedUrl.substring(abbreviatedUrl.indexOf(':') + 1));
    }

    /**
     * Returns the prefix of an abbreviated URL.
     *
     * @param abbreviatedUrl
     *            abbreviated URL
     * @return the prefix
     */
    public static String getPrefix(String abbreviatedUrl) {
        return abbreviatedUrl.substring(0, abbreviatedUrl.indexOf(':'));
    }

    /**
     * Returns the local name part of an URL. If the URL contains an anchor
     * symbol the local part is considered the sequence after it, otherwise the
     * local name is considered the sequence after the last slash.
     *
     * @param url
     *            URL to return the local name from
     * @return the local name
     */
    public static String localNameOf(String url) {
        int numberSign = url.indexOf('#');
        if (numberSign > -1) {
            return url.substring(numberSign + 1);
        }
        return url.substring(url.lastIndexOf('/') + 1);
    }

    /**
     * Converts a URI to a globally unique name space. The name space is formed
     * with the host name of the machine the program is running on.
     *
     * @param uri
     *            URI to create a globally unique URI for
     * @return globally unique namespace
     * @throws IOException
     *             if it fails
     */
    public static String namespaceFromURI(URI uri) throws IOException {
        try {
            String host = uri.getHost();
            String path = uri.getPath();
            if (host == null) {
                host = InetAddress.getLocalHost().getCanonicalHostName();
                if ((path != null) && path.startsWith("//")) {
                    int pathStart = path.indexOf('/', 2);
                    String remote = path.substring(2, pathStart);
                    path = path.substring(pathStart);
                    host = remote.contains(".") ? remote
                            : remote.concat(host.substring(InetAddress.getLocalHost().getHostName().length()));
                }
            }
            String scheme = uri.getScheme();
            if ((scheme == null) || !scheme.toLowerCase().startsWith("http")) {
                scheme = "http";
            }
            return new URI(scheme, uri.getUserInfo(), host, uri.getPort(), path, uri.getQuery(), "").toASCIIString();
        } catch (URISyntaxException e) {
            String message = e.getMessage();
            throw new IllegalArgumentException(message != null ? message : e.getClass().getName(), e);
        }
    }

    /**
     * Returns the namespace part of an URL. If the URL contains an anchor
     * symbol the namespace is considered the sequence up to and including it,
     * otherwise the namespace is considered the sequence up to and including
     * the last slash.
     *
     * @param url
     *            url to return the namespace from
     * @return the namespace
     */
    public static String namespaceOf(String url) {
        int numberSign = url.indexOf('#');
        if (numberSign > -1) {
            return url.substring(0, numberSign + 1);
        }
        return url.substring(0, url.lastIndexOf('/') + 1);
    }

    /**
     * Returns the namespace part of an URL. If the URL contains an anchor
     * symbol the namespace is considered the sequence before it, omitting the
     * anchor symbol, otherwise the namespace is considered the sequence up to
     * and including the last slash.
     *
     * @param url
     *            url to return the namespace from
     * @return the namespace
     */
    static String namespaceOfForXMLFile(String url) {
        int numberSign = url.indexOf('#');
        if (numberSign > -1) {
            return url.substring(0, numberSign);
        }
        return url.substring(0, url.lastIndexOf('/') + 1);
    }

    /**
     * Returns all namespaces to be added to the document head. Mapping
     * direction is namespace to prefix, {@code #} namespaces are stored without
     * the {@code #} at the end, {@code /} namespaces are stored with a
     * {@code /} as last character. The method must be called after the document
     * has been converted.
     *
     * @return all namespaces
     */
    Set<Entry<String, String>> namespaceSetForXMLFile() {
        Map<String, String> result = new HashMap<>((int) Math.ceil(used.size() / 0.75));
        for (Entry<String, String> entry : used.entrySet()) {
            String prefix = entry.getKey();
            result.put(PREFIX_XMLNS + ':' + entry.getValue(), prefix);
        }
        return result.entrySet();
    }
}
