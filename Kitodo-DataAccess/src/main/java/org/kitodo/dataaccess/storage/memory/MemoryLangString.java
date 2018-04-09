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

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.kitodo.dataaccess.LangString;
import org.kitodo.dataaccess.Literal;
import org.kitodo.dataaccess.Node;
import org.kitodo.dataaccess.NodeReference;
import org.kitodo.dataaccess.ObjectType;
import org.kitodo.dataaccess.RDF;
import org.kitodo.dataaccess.Result;

/**
 * An RDF lang string, that is a linked data literal with a language tag
 * attached.
 */
public class MemoryLangString extends MemoryLiteral implements LangString {
    /**
     * Identifies the human language of the subject as a RFC 4646 code.
     */
    private static final NodeReference XML_LANG = new MemoryNodeReference("http://www.w3.org/XML/1998/namespace#lang");

    /**
     * The locale of this literal.
     */
    private final Locale locale;

    /**
     * Creates a new localised Literal.
     *
     * @param value
     *            literal value
     * @param languageTag
     *            locale code
     */
    public MemoryLangString(String value, String languageTag) {
        super(value, RDF.LANG_STRING.getIdentifier());
        if (languageTag.isEmpty()) {
            throw new IllegalArgumentException("For language tag: " + languageTag);
        }
        locale = Locale.forLanguageTag(languageTag);
    }

    /**
     * Compares two objects for equality.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MemoryLangString other = (MemoryLangString) obj;
        if (locale == null) {
            if (other.locale != null) {
                return false;
            }
        } else if (!locale.equals(other.locale)) {
            return false;
        }
        return true;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    /**
     * Returns a hash code of the language-tagged string.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = (prime * result) + (locale == null ? 0 : locale.hashCode());
        return result;
    }

    @Override
    public boolean matches(ObjectType condition) {
        if (condition instanceof LangString) {
            LangString other = (LangString) condition;
            if ((other.getLocale() != null) && !other.getLocale().equals(locale)) {
                return false;
            }
            return super.matches(condition);
        } else if (condition instanceof Node) {
            Node filter = (Node) condition;
            if (!Literal.ALLOWED_RELATIONS.containsAll(filter.getRelations())) {
                return false;
            }
            Result expectedLanguage = filter.get(XML_LANG);
            switch ((int) expectedLanguage.countUntil(2)) {
                case 0:
                    break;
                case 1:
                    ObjectType checkLanguage = expectedLanguage.iterator().next();
                    if (!(checkLanguage instanceof Literal)) {
                        return false;
                    }
                    Locale other = Locale.forLanguageTag(((Literal) checkLanguage).getValue());
                    if (!other.getLanguage().isEmpty() && !other.getLanguage().equals(locale.getLanguage())) {
                        return false;
                    }
                    if (!other.getCountry().isEmpty() && !other.getCountry().equals(locale.getCountry())) {
                        return false;
                    }
                    if (!other.getVariant().isEmpty() && !other.getVariant().equals(locale.getVariant())) {
                        return false;
                    }
                    if (!other.getScript().isEmpty() && !other.getScript().equals(locale.getScript())) {
                        return false;
                    }
                    break;
                default:
                    return false;
            }
            return super.matches(condition);
        }
        return false;
    }

    @Override
    public RDFNode toRDFNode(Model model, Boolean unused) {
        return model.createLiteral(value, locale.toLanguageTag());
    }

    @Override
    public String toString() {
        String language = locale.toLanguageTag();
        StringBuffer result = new StringBuffer(value.length() + language.length() + 3);
        Matcher matcher = Pattern.compile("[\u0000-\u001F\\\\]").matcher(value);
        result.append('"');
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
        result.append('"');
        if (language != null) {
            result.append('@');
            result.append(language);
        }
        return result.toString();
    }

}
