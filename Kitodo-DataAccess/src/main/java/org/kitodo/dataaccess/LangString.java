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

import java.util.Locale;

import org.kitodo.dataaccess.storage.memory.MemoryNodeReference;

/**
 * An RDF lang string, that is a linked data literal with a language tag
 * attached.
 */
public interface LangString extends Literal {
    /**
     * Identifies the human language of the subject as a RFC 4646 code.
     */
    static final NodeReference XML_LANG = new MemoryNodeReference("http://www.w3.org/XML/1998/namespace#lang");

    /**
     * Returns a well-formed IETF BCP 47 language tag representing the locale of
     * the language-tagged string.
     *
     * @return a BCP47 language tag representing the locale of the
     *         language-tagged string
     */
    default String getLanguageTag() {
        return getLocale().toLanguageTag();
    }

    /**
     * Returns the locale of the language-tagged string.
     *
     * @return the locale
     */
    Locale getLocale();
}
