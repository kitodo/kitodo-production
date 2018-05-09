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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.kitodo.dataaccess.IdentifiableNode;
import org.kitodo.dataaccess.LangString;
import org.kitodo.dataaccess.Literal;
import org.kitodo.dataaccess.Node;
import org.kitodo.dataaccess.NodeReference;
import org.kitodo.dataaccess.ObjectType;
import org.kitodo.dataaccess.RDF;
import org.kitodo.dataaccess.Result;
import org.kitodo.dataaccess.XMLSchema;
import org.kitodo.dataaccess.format.xml.Namespaces;

/**
 * A linked data literal.
 */
public class MemoryLiteral implements Literal {

    /**
     * Three relations allowed on a node describing a literal.
     */
    /*
     * Constants from `RDF.java` cannot be used here because the constants are
     * NodeReference objects, thus a subclass of this class and hence cannot yet
     * be accessed at creation time of this class.
     */
    protected static final Set<String> ALLOWED_RELATIONS = new HashSet<>(Arrays.asList(
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/XML/1998/namespace#lang",
                      "http://www.w3.org/1999/02/22-rdf-syntax-ns#value"));

    /**
     * Three literal types, not including RDF.LANG_STRING, that may be passed to
     * the Literal constructor when creating a common literal.
     */
    /*
     * Constants from `RDF.java` cannot be used here because the constants are
     * NodeReference objects, thus a subclass of this class and hence cannot yet
     * be accessed at creation time of this class.
     */
    private static final List<String> LITERAL_TYPES = Arrays
            .asList("http://www.w3.org/1999/02/22-rdf-syntax-ns#PlainLiteral",
                                  "http://www.w3.org/1999/02/22-rdf-syntax-ns#HTML",
                                  "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral");

    /**
     * Pattern to check whether a String starts with an URL scheme as specified
     * in RFC 1738.
     */
    private static final Pattern SCHEME_TEST = Pattern.compile("[+\\-\\.0-9A-Za-z]+:[^ ]+");

    static final String XSD_NAMESPACE = "http://www.w3.org/2001/XMLSchema#";

    /**
     * Creates a literal object from a String. If the literal starts with
     * {@code http://}, a node reference is created, otherwise if a language is
     * given, a LangString will be created, otherwise a plain literal.
     *
     * @param value
     *            the literal value
     * @param lang
     *            language, may be {@code ""} but not {@code null}
     * @return the literal object
     */
    public static ObjectType createLeaf(String value, String lang) {
        if (SCHEME_TEST.matcher(value).matches()) {
            return new MemoryNodeReference(value);
        } else {
            return createLiteral(value, lang);
        }
    }

    /**
     * Creates a literal object from a String. If a language is given, a
     * LangString will be created, otherwise a plain literal.
     *
     * @param value
     *            the literal value
     * @param lang
     *            language, may be {@code ""} but not {@code null}
     * @return the literal object
     */
    public static MemoryLiteral createLiteral(String value, String lang) {
        if ((lang == null) || lang.isEmpty()) {
            return new MemoryLiteral(value, RDF.PLAIN_LITERAL);
        } else {
            return new MemoryLangString(value, lang);
        }
    }

    /**
     * Type of this literal.
     */
    private final String type;

    /**
     * The Literal value.
     */
    protected final String value;

    /**
     * Creates a new Literal with a value and a type.
     *
     * @param value
     *            literal value
     * @param type
     *            literal type, one of RDF.HTML, RDF.PLAIN_LITERAL,
     *            RDF.XML_LITERAL, or a literal type defined in XMLSchema.
     */
    public MemoryLiteral(String value, NodeReference type) {
        if (value == null) {
            throw new IllegalArgumentException("Value must not be null.");
        }
        this.value = value;
        this.type = type != null ? type.getIdentifier() : RDF.PLAIN_LITERAL.getIdentifier();
        if (!LITERAL_TYPES.contains(this.type) && !XSD_NAMESPACE.equals(Namespaces.namespaceOf(this.type))) {
            throw new IllegalArgumentException(type.getIdentifier());
        }
    }

    /**
     * Constructor for use in subclass {@link LangString}.
     *
     * @param value
     *            literal value
     * @param type
     *            literal type
     */
    protected MemoryLiteral(String value, String type) {
        if (value == null) {
            throw new IllegalArgumentException("Value must not be null.");
        }
        this.value = value;
        this.type = (type == null) || type.isEmpty() ? RDF.PLAIN_LITERAL.getIdentifier() : type;
        assert URI_SCHEME.matcher(this.type).find() : "Illegal type.";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MemoryLiteral other = (MemoryLiteral) obj;
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else {
            String canonicalType = XMLSchema.STRING.getIdentifier().equals(type) ? RDF.PLAIN_LITERAL.getIdentifier()
                    : type;
            String canonicalOtherType = XMLSchema.STRING.getIdentifier().equals(other.type)
                    ? RDF.PLAIN_LITERAL.getIdentifier()
                    : other.type;
            if (!canonicalType.equals(canonicalOtherType)) {
                return false;
            }
        }
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        String canonicalType = XMLSchema.STRING.getIdentifier().equals(type) ? RDF.PLAIN_LITERAL.getIdentifier() : type;
        result = (prime * result) + (canonicalType == null ? 0 : canonicalType.hashCode());
        result = (prime * result) + (value == null ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean matches(ObjectType condition) {
        if (condition instanceof Literal) {
            Literal other = (Literal) condition;
            if ((other.getType() != null) && !other.getType().equals(type)) {
                return false;
            }
            if ((other.getValue() != null) && !other.getValue().isEmpty() && !other.getValue().equals(value)) {
                return false;
            }
            return true;
        } else if (condition instanceof Node) {
            Node filter = (Node) condition;
            if (!ALLOWED_RELATIONS.containsAll(filter.getRelations())) {
                return false;
            }

            Result expectedType = filter.get(RDF.TYPE);
            switch ((int) expectedType.countUntil(2)) {
                case 0:
                    break;
                case 1:
                    ObjectType checkType = expectedType.iterator().next();
                    if (!(checkType instanceof IdentifiableNode)) {
                        return false;
                    }
                    if (!((IdentifiableNode) checkType).getIdentifier().equals(type)) {
                        return false;
                    }
                    break;
                default:
                    return false;
            }

            Result expectedValue = filter.get(RDF.VALUE);
            switch ((int) expectedValue.countUntil(2)) {
                case 0:
                    break;
                case 1:
                    ObjectType checkType = expectedValue.iterator().next();
                    if (!(checkType instanceof Literal)) {
                        return false;
                    }
                    if (!((Literal) checkType).getValue().equals(value)) {
                        return false;
                    }
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    @Override
    public RDFNode toRDFNode(Model model, Boolean unused) {
        if (type.equals(RDF.PLAIN_LITERAL.getIdentifier())) {
            return model.createLiteral(value);
        } else {
            return model.createTypedLiteral(value, type);
        }
    }

    /**
     * Returns a readable description of this literal to be seen in a debugger.
     */
    @Override
    public String toString() {
        StringBuffer result = new StringBuffer(value.length() + 2);
        boolean isXsdInteger = "http://www.w3.org/2001/XMLSchema#integer".equals(type);
        Matcher matcher = Pattern.compile("[\u0000-\u001F\\\\]").matcher(value);
        if (!isXsdInteger) {
            result.append('"');
        }
        while (matcher.find()) {
            matcher.appendReplacement(result, ""); // http://bugs.java.com/bugdatabase/view_bug.do?bug_id=652315
            result.append('\\');
            if (matcher.group().equals("\\")) {
                result.append(matcher.group());
            } else {
                result.append(new String(Character.toChars(60 + matcher.group().codePointAt(0))));
            }
        }
        matcher.appendTail(result);
        if (!isXsdInteger) {
            result.append('"');
        }
        if (!isXsdInteger && !RDF.PLAIN_LITERAL.getIdentifier().equals(type)) {
            result.append("^^");
            String namespace = Namespaces.namespaceOf(type);
            if (namespace.equals(XSD_NAMESPACE)) {
                result.append("xsd:");
                result.append(Namespaces.localNameOf(type));
            } else if (namespace.equals(RDF.NAMESPACE)) {
                result.append("rdf:");
                result.append(Namespaces.localNameOf(type));
            } else {
                result.append(type);
            }
        }
        return result.toString();
    }
}
