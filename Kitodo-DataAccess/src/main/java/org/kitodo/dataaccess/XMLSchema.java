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

package org.kitodo.dataaccess;

import java.math.BigInteger;
import java.util.Optional;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;

/**
 * The XML Schema datatypes.
 *
 * @see "https://www.w3.org/TR/xmlschema-2/"
 */
public enum XMLSchema implements NodeReference {
    /**
     * <b>anyURI</b> represents a Uniform Resource Identifier Reference (URI).
     * An anyURI value can be absolute or relative, and may have an optional
     * fragment identifier (i.e., it may be a URI Reference).
     */
    ANY_URI("http://www.w3.org/2001/XMLSchema#anyURI"),

    /**
     * <b>base64Binary</b> represents Base64-encoded arbitrary binary data.
     */
    BASE_64_BINARY("http://www.w3.org/2001/XMLSchema#base64Binary"),

    /**
     * <b>boolean</b> has the value space required to support the mathematical
     * concept of binary-valued logic: {true, false}.
     */
    BOOLEAN("http://www.w3.org/2001/XMLSchema#boolean"),

    /**
     * <b>byte</b> represents an integer value with a limited range of {x ∊ ℕ |
     * x ≥ −128 ∧ x ≤ 127}.
     */
    BYTE("http://www.w3.org/2001/XMLSchema#byte", "-128", "127"),

    /**
     * A <b>date</b> is an object with year, month, and day properties, plus an
     * optional timezone-valued timezone property. A date object corresponds to
     * an interval on the time line, beginning on the beginning moment of each
     * day (in each timezone), i.e. '00:00:00', up to but not including
     * '24:00:00'.
     */
    DATE("http://www.w3.org/2001/XMLSchema#date"),

    /**
     * <b>dateTime</b> values may be viewed as objects with integer-valued year,
     * month, day, hour and minute properties, a decimal-valued second property,
     * and a boolean timezoned property. Each such object also has one
     * decimal-valued method or computed property, timeOnTimeline, whose value
     * is always a decimal number; the values are dimensioned in seconds, the
     * integer 0 is 0001-01-01T00:00:00 and the value of timeOnTimeline for
     * other dateTime values is computed using the Gregorian algorithm as
     * modified for leap-seconds.
     */
    DATE_TIME("http://www.w3.org/2001/XMLSchema#dateTime"),

    /**
     * <b>decimal</b> represents a subset of the real numbers, which can be
     * represented by decimal numerals.
     */
    DECIMAL("http://www.w3.org/2001/XMLSchema#decimal"),

    /**
     * The <b>double</b> datatype is patterned after the IEEE double-precision
     * 64-bit floating point type.
     */
    DOUBLE("http://www.w3.org/2001/XMLSchema#double"),

    /**
     * <b>duration</b> represents a duration of time. The value space of
     * duration is a six-dimensional space where the coordinates designate the
     * Gregorian year, month, day, hour, minute, and second components defined
     * in ISO 8601. These components are ordered in their significance by their
     * order of appearance i.e. as year, month, day, hour, minute, and second.
     */
    DURATION("http://www.w3.org/2001/XMLSchema#duration"),

    /**
     * <b>ENTITIES</b> represents the ENTITIES attribute type from XML 1.0,
     * Second Edition.
     *
     * @see "http://www.w3.org/TR/2000/WD-xml-2e-20000814#NT-TokenizedType"
     */
    ENTITIES("http://www.w3.org/2001/XMLSchema#ENTITIES"),

    /**
     * <b>ENTITY</b> represents the ENTITY attribute type from XML 1.0, Second
     * Edition.
     *
     * @see "http://www.w3.org/TR/2000/WD-xml-2e-20000814#NT-TokenizedType"
     */
    ENTITY("http://www.w3.org/2001/XMLSchema#ENTITY"),

    /**
     * <b>float</b> is patterned after the IEEE single-precision 32-bit floating
     * point type.
     */
    FLOAT("http://www.w3.org/2001/XMLSchema#float"),

    /**
     * <b>gDay</b> is a gregorian day that recurs on a day of the month.
     */
    G_DAY("http://www.w3.org/2001/XMLSchema#gDay"),

    /**
     * <b>gMonth</b> is a gregorian month that recurs every year.
     */
    G_MONTH("http://www.w3.org/2001/XMLSchema#gMonth"),

    /**
     * <b>gMonthDay</b> is a gregorian date that recurs every year.
     */
    G_MONTH_DAY("http://www.w3.org/2001/XMLSchema#gMonthDay"),

    /**
     * <b>gYear</b> represents a gregorian calendar year.
     */
    G_YEAR("http://www.w3.org/2001/XMLSchema#gYear"),

    /**
     * <b>gYearMonth</b> represents a specific gregorian month in a specific
     * gregorian year.
     */
    G_YEAR_MONTH("http://www.w3.org/2001/XMLSchema#gYearMonth"),

    /**
     * <b>hexBinary</b> represents arbitrary hex-encoded binary data.
     */
    HEX_BINARY("http://www.w3.org/2001/XMLSchema#hexBinary"),

    /**
     * <b>ID</b> represents the ID attribute type from XML 1.0, Second Edition.
     *
     * @see "http://www.w3.org/TR/2000/WD-xml-2e-20000814#NT-TokenizedType"
     */
    ID("http://www.w3.org/2001/XMLSchema#ID"),

    /**
     * <b>IDREF</b> represents the IDREF attribute type from XML 1.0, Second
     * Edition.
     *
     * @see "http://www.w3.org/TR/2000/WD-xml-2e-20000814#NT-TokenizedType"
     */
    IDREF("http://www.w3.org/2001/XMLSchema#IDREF"),

    /**
     * <b>IDREFS</b> represents the IDREFS attribute type from XML 1.0, Second
     * Edition.
     *
     * @see "http://www.w3.org/TR/2000/WD-xml-2e-20000814#NT-TokenizedType"
     */
    IDREFS("http://www.w3.org/2001/XMLSchema#IDREFS"),

    /**
     * <b>int</b> represents an integer value with a limited range of {x ∊ ℕ | x
     * ≥ −2147483648 ∧ x ≤ 2147483647}.
     */
    INT("http://www.w3.org/2001/XMLSchema#int", "-2147483648", "2147483647"),

    /**
     * <b>integer</b> represents the standard mathematical concept of the
     * integer numbers, ℕ.
     */
    INTEGER("http://www.w3.org/2001/XMLSchema#integer", null, null),

    /**
     * <b>language</b> represents natural language identifiers as defined by by
     * RFC 3066.
     *
     * @see "http://www.ietf.org/rfc/rfc3066.txt"
     */
    LANGUAGE("http://www.w3.org/2001/XMLSchema#language"),

    /**
     * <b>long</b> represents an integer value with a limited range of {x ∊ ℕ |
     * x ≥ −9223372036854775808 ∧ x ≤ 9223372036854775807}.
     */
    LONG("http://www.w3.org/2001/XMLSchema#long", "-9223372036854775808", "9223372036854775807"),

    /**
     * <b>Name</b> represents XML Names.
     *
     * @see "http://www.w3.org/TR/2000/WD-xml-2e-20000814#dt-name"
     */
    NAME("http://www.w3.org/2001/XMLSchema#Name"),

    /**
     * <b>NCName</b> represents XML "non-colonized" Names.
     *
     * @see "http://www.w3.org/TR/1999/REC-xml-names-19990114/#NT-NCName"
     */
    NC_NAME("http://www.w3.org/2001/XMLSchema#NCName"),

    /**
     * <b>negativeInteger</b> represents the standard mathematical concept of
     * the negative integers, {x ∊ ℕ | x &lt; 0}.
     */
    NEGATIVE_INTEGER("http://www.w3.org/2001/XMLSchema#negativeInteger", null, "-1"),

    /**
     * <b>NMTOKEN</b> represents the NMTOKEN attribute type from XML 1.0, Second
     * Edition.
     *
     * @see "http://www.w3.org/TR/2000/WD-xml-2e-20000814#NT-TokenizedType"
     */
    NMTOKEN("http://www.w3.org/2001/XMLSchema#NMTOKEN"),

    /**
     * NMTOKENS represents the NMTOKENS attribute type from XML 1.0, Second
     * Edition.
     *
     * @see "http://www.w3.org/TR/2000/WD-xml-2e-20000814#NT-TokenizedType"
     */
    NMTOKENS("http://www.w3.org/2001/XMLSchema#NMTOKENS"),

    /**
     * <b>nonPositiveInteger</b> represents the standard mathematical concept of
     * the non-negative integers, {x ∊ ℕ | x ≥ 0}.
     */
    NON_NEGATIVE_INTEGER("http://www.w3.org/2001/XMLSchema#nonNegativeInteger", "0", null),

    /**
     * <b>nonPositiveInteger</b> represents the standard mathematical concept of
     * the non-positive integers, {x ∊ ℕ | x ≤ 0}.
     */
    NON_POSITIVE_INTEGER("http://www.w3.org/2001/XMLSchema#nonPositiveInteger", null, "0"),

    /**
     * <b>normalizedString</b> represents white space normalized strings. The
     * value space of normalizedString is the set of strings that do not contain
     * the carriage return (#xD), line feed (#xA) nor tab (#x9) characters.
     */
    NORMALIZED_STRING("http://www.w3.org/2001/XMLSchema#normalizedString"),

    /**
     * <b>NOTATION</b> represents the NOTATION attribute type from XML 1.0,
     * Second Edition.
     *
     * @see "http://www.w3.org/TR/2000/WD-xml-2e-20000814#NT-NotationType"
     */
    NOTATION("http://www.w3.org/2001/XMLSchema#NOTATION"),

    /**
     * <b>positiveInteger</b> represents the standard mathematical concept of
     * the positive integers, {x ∊ ℕ | x &gt; 0}.
     */
    POSITIVE_INTEGER("http://www.w3.org/2001/XMLSchema#positiveInteger", "1", null),

    /**
     * <b>QName</b> represents XML qualified names.
     *
     * @see "http://www.w3.org/TR/1999/REC-xml-names-19990114/#dt-qname"
     */
    Q_NAME("http://www.w3.org/2001/XMLSchema#QName"),

    /**
     * <b>short</b> represents an integer value with a limited range of {x ∊ ℕ |
     * x ≥ −32768 ∧ x ≤ 32767}.
     */
    SHORT("http://www.w3.org/2001/XMLSchema#short", "-32768", "32767"),

    /**
     * The <b>string</b> datatype represents character strings.
     */
    STRING("http://www.w3.org/2001/XMLSchema#string"),

    /**
     * <b>time</b> represents an instant of time that recurs every day.
     */
    TIME("http://www.w3.org/2001/XMLSchema#time"),

    /**
     * <b>token</b> represents tokenized strings. The value space of token is
     * the set of strings that do not contain the carriage return (#xD), line
     * feed (#xA) nor tab (#x9) characters, that have no leading or trailing
     * spaces (#x20) and that have no internal sequences of two or more spaces.
     */
    TOKEN("http://www.w3.org/2001/XMLSchema#token"),

    /**
     * <b>unsignedByte</b> represents an integer value with a limited range of
     * {x ∊ ℕ | x ≥ 0 ∧ x ≤ 255}.
     */
    UNSIGNED_BYTE("http://www.w3.org/2001/XMLSchema#unsignedByte", "0", "255"),

    /**
     * <b>unsignedInt</b> represents an integer value with a limited range of {x
     * ∊ ℕ | x ≥ 0 ∧ x ≤ 4294967295}.
     */
    UNSIGNED_INT("http://www.w3.org/2001/XMLSchema#unsignedInt", "0", "4294967295"),

    /**
     * <b>unsignedLong</b> represents an integer value with a limited range of
     * {x ∊ ℕ | x ≥ 0 ∧ x ≤ 18446744073709551615}.
     */
    UNSIGNED_LONG("http://www.w3.org/2001/XMLSchema#unsignedLong", "0", "18446744073709551615"),

    /**
     * <b>unsignedShort</b> represents an integer value with a limited range of
     * {x ∊ ℕ | x ≥ 0 ∧ x ≤ 65535}.
     */
    UNSIGNED_SHORT("http://www.w3.org/2001/XMLSchema#unsignedShort", "0", "65535");

    /**
     * The XML Schema namespace.
     */
    public static final String NAMESPACE = "http://www.w3.org/2001/XMLSchema#";

    /**
     * Returns the maximum value required for this datatype, if any.
     *
     * @param datatype
     *            datatype identifier
     * @return the maximum value, if any
     */
    public static Optional<BigInteger> getMaximum(IdentifiableNode datatype) {
        return getMaximum(datatype.getIdentifier());
    }

    /**
     * Returns the maximum value required for this datatype, if any.
     *
     * @param datatype
     *            datatype identifier
     * @return the maximum value, if any
     */
    public static Optional<BigInteger> getMaximum(String datatype) {
        for (XMLSchema value : values()) {
            if (value.identifier.equals(datatype)) {
                if (value.maximum == null) {
                    return Optional.empty();
                }
            } else {
                return Optional.of(new BigInteger(value.maximum));
            }
        }
        return Optional.empty();
    }

    /**
     * Returns the minimum value required for this datatype, if any.
     *
     * @param datatype
     *            datatype identifier
     * @return the minimum value, if any
     */
    public static Optional<BigInteger> getMinimum(IdentifiableNode datatype) {
        return getMaximum(datatype.getIdentifier());
    }

    /**
     * Returns the minimum value required for this datatype, if any.
     *
     * @param datatype
     *            datatype identifier
     * @return the minimum value, if any
     */
    public static Optional<BigInteger> getMinimum(String datatype) {
        for (XMLSchema value : values()) {
            if (value.identifier.equals(datatype)) {
                if (value.minimum == null) {
                    return Optional.empty();
                }
            } else {
                return Optional.of(new BigInteger(value.minimum));
            }
        }
        return Optional.empty();
    }

    private final String identifier;

    private final String maximum;

    private final String minimum;

    /**
     * Creates a new NodeReference.
     *
     * @param identifier
     *            referenced URL
     */
    private XMLSchema(String identifier) {
        this.identifier = identifier;
        minimum = null;
        maximum = null;
    }

    /**
     * Creates a new NodeReference for an integer datatype.
     *
     * @param identifier
     *            referenced URL
     * @param minimum
     *            smallest allowed value
     * @param maximum
     *            greatest allowed value
     */
    private XMLSchema(String identifier, String minimum, String maximum) {
        this.identifier = identifier;
        this.minimum = minimum;
        this.maximum = maximum;
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
