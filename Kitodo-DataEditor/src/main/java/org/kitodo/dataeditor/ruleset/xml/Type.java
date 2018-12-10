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

package org.kitodo.dataeditor.ruleset.xml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 * This class is a backing bean for the XML attribute type in the ruleset. With
 * it, JAXB can map the attribute to an enum. These are constants from XML
 * Schema data types.
 */
@XmlEnum(String.class)
public enum Type {
    /**
     * A URI. This can be a URL, and it is usually also. There are also others
     * possible here, e.g. URN.
     */
    @XmlEnumValue("anyURI")
    ANY_URI,

    /**
     * Boolean is a value from a set of values with exactly two values, which
     * are usually called “true” and “false”. In our case, the Boolean behaves a
     * bit differently, in the form that it either consists of a fixed, but in
     * principle arbitrary, value, or no value is written. But even in this
     * variant, there are exactly two cases that are distinguished.
     */
    @XmlEnumValue("boolean")
    BOOLEAN,

    /**
     * According to the specification, a top-open interval of exactly one day in
     * length on the timeline, beginning at the moment of the beginning of the
     * day in the respective time zone. In short, a valid date according to the
     * Gregorian calendar without specifying a time zone. The date is stored in
     * a string of ten characters, which must conform to the following pattern:
     * “yyyy-mm-dd”.
     */
    @XmlEnumValue("date")
    DATE,

    /**
     * An integer. The data type according to the XML schema is not limited in
     * size and also allows zero and negative numbers. For a limitation, for
     * example, it can be combined with a regular expression.
     */
    @XmlEnumValue("integer")
    INTEGER,

    /**
     * In short, you can enter everything there. That is also the standard case.
     */
    @XmlEnumValue("string")
    STRING
}
