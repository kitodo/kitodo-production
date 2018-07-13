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

package org.kitodo.config.xml.fileformats;

import java.util.Locale;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

/**
 * A human-redable label for a file format. This class corresponds to the
 * {@code <label>} tag in {@code kitodo_fileFormats.xml}.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"value" })
class Label {

    @XmlAttribute(name = "lang")
    protected String lang;

    @XmlValue
    protected String value;

    Optional<Locale> getLanguage() {
        return lang == null ? Optional.empty() : Optional.of(Locale.forLanguageTag(lang));
    }

    String getValue() {
        return value;
    }
}
