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

import java.util.Map;

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
public class Namespaces {
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
}
