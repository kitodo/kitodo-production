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

package org.kitodo.lugh.vocabulary;

import java.math.BigInteger;
import java.util.*;

import org.apache.jena.rdf.model.*;
import org.kitodo.lugh.*;

/**
 * The XML Schema datatypes.
 *
 * @see "https://www.w3.org/TR/xmlschema-2/"
 * @author Matthias Ronge
 */
public class XMLSchema {
    public static class IntegerDatatype implements NodeReference {
        private final NodeReference delegate;
        private final BigInteger max;
        private final BigInteger min;

        public IntegerDatatype(String url, String min, String max, Storage s) {
            delegate = s.createNodeReference(url);
            this.min = min != null ? new BigInteger(min) : null;
            this.max = max != null ? new BigInteger(max) : null;
        }

        @Override
        public String getIdentifier() {
            return delegate.getIdentifier();
        }

        public BigInteger getMax() {
            return max;
        }

        public BigInteger getMin() {
            return min;
        }

        @Override
        public RDFNode toRDFNode(Model model, Boolean addNamedNodesRecursively) {
            return delegate.toRDFNode(model, addNamedNodesRecursively);
        }
    }

    public static final NodeReference ANY_URI = MemoryStorage.INSTANCE
            .createNodeReference("http://www.w3.org/2001/XMLSchema#anyURI");

    public static final NodeReference BASE_64_BINARY = MemoryStorage.INSTANCE
            .createNodeReference("http://www.w3.org/2001/XMLSchema#base64Binary");

    public static final NodeReference BOOLEAN = MemoryStorage.INSTANCE
            .createNodeReference("http://www.w3.org/2001/XMLSchema#boolean");

    public static final NodeReference BYTE = new IntegerDatatype("http://www.w3.org/2001/XMLSchema#byte", "-128", "127",
            MemoryStorage.INSTANCE);

    public static final NodeReference DATE = MemoryStorage.INSTANCE
            .createNodeReference("http://www.w3.org/2001/XMLSchema#date");

    public static final NodeReference DATE_TIME = MemoryStorage.INSTANCE
            .createNodeReference("http://www.w3.org/2001/XMLSchema#dateTime");

    public static final NodeReference DECIMAL = MemoryStorage.INSTANCE
            .createNodeReference("http://www.w3.org/2001/XMLSchema#decimal");

    public static final NodeReference DOUBLE = MemoryStorage.INSTANCE
            .createNodeReference("http://www.w3.org/2001/XMLSchema#double");

    public static final NodeReference DURATION = MemoryStorage.INSTANCE
            .createNodeReference("http://www.w3.org/2001/XMLSchema#duration");

    public static final NodeReference ENTITIES = MemoryStorage.INSTANCE
            .createNodeReference("http://www.w3.org/2001/XMLSchema#ENTITIES");

    public static final NodeReference ENTITY = MemoryStorage.INSTANCE
            .createNodeReference("http://www.w3.org/2001/XMLSchema#ENTITY");

    public static final NodeReference FLOAT = MemoryStorage.INSTANCE
            .createNodeReference("http://www.w3.org/2001/XMLSchema#float");

    public static final NodeReference G_DAY = MemoryStorage.INSTANCE
            .createNodeReference("http://www.w3.org/2001/XMLSchema#gDay");

    public static final NodeReference G_MONTH = MemoryStorage.INSTANCE
            .createNodeReference("http://www.w3.org/2001/XMLSchema#gMonth");

    public static final NodeReference G_MONTH_DAY = MemoryStorage.INSTANCE
            .createNodeReference("http://www.w3.org/2001/XMLSchema#gMonthDay");

    public static final NodeReference G_YEAR = MemoryStorage.INSTANCE
            .createNodeReference("http://www.w3.org/2001/XMLSchema#gYear");

    public static final NodeReference G_YEAR_MONTH = MemoryStorage.INSTANCE
            .createNodeReference("http://www.w3.org/2001/XMLSchema#gYearMonth");

    public static final NodeReference HEX_BINARY = MemoryStorage.INSTANCE
            .createNodeReference("http://www.w3.org/2001/XMLSchema#hexBinary");

    public static final NodeReference ID = MemoryStorage.INSTANCE
            .createNodeReference("http://www.w3.org/2001/XMLSchema#ID");

    public static final NodeReference IDREF = MemoryStorage.INSTANCE
            .createNodeReference("http://www.w3.org/2001/XMLSchema#IDREF");

    public static final NodeReference IDREFS = MemoryStorage.INSTANCE
            .createNodeReference("http://www.w3.org/2001/XMLSchema#IDREFS");

    public static final NodeReference INT = new IntegerDatatype("http://www.w3.org/2001/XMLSchema#int", "-2147483648",
            "2147483647", MemoryStorage.INSTANCE);

    public static final NodeReference INTEGER = MemoryStorage.INSTANCE
            .createNodeReference("http://www.w3.org/2001/XMLSchema#integer");

    public static final NodeReference LANGUAGE = MemoryStorage.INSTANCE
            .createNodeReference("http://www.w3.org/2001/XMLSchema#language");

    public static final NodeReference LONG = new IntegerDatatype("http://www.w3.org/2001/XMLSchema#long",
            "-9223372036854775808", "9223372036854775807", MemoryStorage.INSTANCE);

    public static final NodeReference NAME = MemoryStorage.INSTANCE
            .createNodeReference("http://www.w3.org/2001/XMLSchema#Name");

    static final String NAMESPACE = "http://www.w3.org/2001/XMLSchema#";

    public static final NodeReference NC_NAME = MemoryStorage.INSTANCE
            .createNodeReference("http://www.w3.org/2001/XMLSchema#NCName");

    public static final NodeReference NEGATIVE_INTEGER = new IntegerDatatype(
            "http://www.w3.org/2001/XMLSchema#negativeInteger", null, "-1", MemoryStorage.INSTANCE);

    public static final NodeReference NMTOKEN = MemoryStorage.INSTANCE
            .createNodeReference("http://www.w3.org/2001/XMLSchema#NMTOKEN");

    public static final NodeReference NMTOKENS = MemoryStorage.INSTANCE
            .createNodeReference("http://www.w3.org/2001/XMLSchema#NMTOKENS");

    public static final NodeReference NON_NEGATIVE_INTEGER = new IntegerDatatype(
            "http://www.w3.org/2001/XMLSchema#nonNegativeInteger", "0", null, MemoryStorage.INSTANCE);

    public static final NodeReference NON_POSITIVE_INTEGER = new IntegerDatatype(
            "http://www.w3.org/2001/XMLSchema#nonPositiveInteger", null, "0", MemoryStorage.INSTANCE);

    public static final NodeReference NORMALIZED_STRING = MemoryStorage.INSTANCE
            .createNodeReference("http://www.w3.org/2001/XMLSchema#normalizedString");

    public static final NodeReference NOTATION = MemoryStorage.INSTANCE
            .createNodeReference("http://www.w3.org/2001/XMLSchema#NOTATION");

    public static final NodeReference POSITIVE_INTEGER = new IntegerDatatype(
            "http://www.w3.org/2001/XMLSchema#positiveInteger", "1", null, MemoryStorage.INSTANCE);

    public static final NodeReference Q_NAME = MemoryStorage.INSTANCE
            .createNodeReference("http://www.w3.org/2001/XMLSchema#QName");

    /**
     * A reverse map of the URLs to the constants.
     */
    private static final Map<String, NodeReference> reversed;

    public static final NodeReference SHORT = new IntegerDatatype("http://www.w3.org/2001/XMLSchema#short", "-32768",
            "32767", MemoryStorage.INSTANCE);

    public static final NodeReference STRING = MemoryStorage.INSTANCE
            .createNodeReference("http://www.w3.org/2001/XMLSchema#string");

    public static final NodeReference TIME = MemoryStorage.INSTANCE
            .createNodeReference("http://www.w3.org/2001/XMLSchema#time");

    public static final NodeReference TOKEN = MemoryStorage.INSTANCE
            .createNodeReference("http://www.w3.org/2001/XMLSchema#token");

    public static final NodeReference UNSIGNED_BYTE = new IntegerDatatype(
            "http://www.w3.org/2001/XMLSchema#unsignedByte", "0", "255", MemoryStorage.INSTANCE);

    public static final NodeReference UNSIGNED_INT = new IntegerDatatype("http://www.w3.org/2001/XMLSchema#unsignedInt",
            "0", "4294967295", MemoryStorage.INSTANCE);

    public static final NodeReference UNSIGNED_LONG = new IntegerDatatype(
            "http://www.w3.org/2001/XMLSchema#unsignedLong", "0", "18446744073709551615", MemoryStorage.INSTANCE);

    public static final NodeReference UNSIGNED_SHORT = new IntegerDatatype(
            "http://www.w3.org/2001/XMLSchema#unsignedShort", "0", "65535", MemoryStorage.INSTANCE);

    /**
     * Populates the reverse map of the URLs to the constants.
     */
    static {
        reversed = new HashMap<>(59);
        for (NodeReference value : new NodeReference[] {ANY_URI, BASE_64_BINARY, BOOLEAN, BYTE, DATE, DATE_TIME,
                DECIMAL, DOUBLE, DURATION, ENTITIES, ENTITY, FLOAT, G_DAY, G_MONTH, G_MONTH_DAY, G_YEAR, G_YEAR_MONTH,
                HEX_BINARY, ID, IDREF, IDREFS, INT, INTEGER, LANGUAGE, LONG, NAME, NC_NAME, NEGATIVE_INTEGER, NMTOKEN,
                NMTOKENS, NON_NEGATIVE_INTEGER, NON_POSITIVE_INTEGER, NORMALIZED_STRING, NOTATION, POSITIVE_INTEGER,
                Q_NAME, SHORT, STRING, TIME, TOKEN, UNSIGNED_BYTE, UNSIGNED_INT, UNSIGNED_LONG, UNSIGNED_SHORT }) {
            reversed.put(value.getIdentifier(), value);
        }
    }

    /**
     * Returns a constant by its URL value
     *
     * @param url
     *            URL to resolve
     * @return the enum constant
     * @throws IllegalArgumentException
     *             if the URL is not mappped in the RDF namespace
     */
    public static NodeReference valueOf(String url) {
        if (!reversed.containsKey(url)) {
            throw new IllegalArgumentException("Unknown URL: " + url);
        }
        return reversed.get(url);
    }
}
