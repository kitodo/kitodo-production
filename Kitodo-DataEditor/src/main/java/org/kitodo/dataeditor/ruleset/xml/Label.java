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

import java.util.Locale;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

/**
 * A label maker for everything. This class is the background for the
 * {@code <label>} elements.
 */
public class Label {
    /**
     * Language of the label. We are very international here.
     */
    @XmlAttribute
    private String lang;

    /**
     * Text on the label.
     */
    @XmlValue
    private String value;

    /**
     * <b>Constructor.</b> Creates a new label.
     */
    /*
     * The default constructor is used by JAXB to create a new label. It must be
     * made explicit because another constructos are provided.
     */
    public Label() {
    }

    /**
     * <b>Constructor.</b> Creates a new default label.
     *
     * @param value
     *            text content of the label
     */
    Label(String value) {
        this.value = value;
    }

    /**
     * <b>Constructor.</b> Creates a new label for a given language.
     *
     * @param lang
     *            language code of the label
     * @param value
     *            text content of the label
     */
    Label(String lang, String value) {
        this.lang = lang;
        this.value = value;
    }

    /**
     * Returns the language if one is specified. Otherwise it is the default
     * value. There should always be.
     *
     * @return the language, if any
     */
    public Optional<Locale> getLanguage() {
        return lang == null ? Optional.empty() : Optional.of(Locale.forLanguageTag(lang));
    }

    /**
     * Returns for the text on the label.
     *
     * @return the text on the label
     */
    public String getValue() {
        return value;
    }
}
