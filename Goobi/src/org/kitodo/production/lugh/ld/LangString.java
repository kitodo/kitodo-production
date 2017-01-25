/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General private License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.kitodo.production.lugh.ld;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;


/**
 * An RDF lang string, that is a linked data literal with a language tag attached.
 *
 * @author Matthias Ronge
 */
public class LangString extends Literal {
    /**
     * The results from calling {@link LangString#parseLocale(String)}.
     *
     * @author Matthias Ronge
     */
    private static class LocaleParseResult {

        /**
         * The locale.
         */
        private Locale language;

        /**
         * The script code.
         */
        private String script;
    }

    /**
     * Pattern to use for parsing the locale code.
     */
    private static final Pattern PARSER = Pattern
            .compile("([A-Za-z]{2,3})(?:-([A-Za-z]{4}))?(?:-([A-Za-z]{2}|\\d{3}))?");

    /**
     * Pattern group holding the language code.
     */
    private static final int PARSER_GROUP_LANGUAGE = 1;
    /**
     * Pattern group holding the region code.
     */
    private static final int PARSER_GROUP_REGION = 3;
    /**
     * Pattern group holding the script code.
     */
    private static final int PARSER_GROUP_SCRIPT = 2;
    /**
     * Identifies the human language of the subject as a RFC 4646 code.
     */
    private static final NodeReference XML_LANG = new NodeReference("http://www.w3.org/XML/1998/namespace#lang");    
    /**
     * The locale of this literal.
     */
    private final Locale language;

    /**
     * Four-letter script code. For the moment, this must be handled externally.
     * The script code will be availabe as part of Java’s Locale object from
     * Java 7 onwards.
     */
    private final String script;

    /**
     * Creates a new localised Literal.
     *
     * @param value
     *            literal value
     * @param locale
     *            locale code
     */
    public LangString(String value, String locale) {
        super(value, RDF.LANG_STRING.getIdentifier());
        LocaleParseResult parsed = parseLocale(locale);
        language = parsed.language;
        script = parsed.script;
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
        LangString other = (LangString) obj;
        if (language == null) {
            if (other.language != null) {
                return false;
            }
        } else if (!language.equals(other.language)) {
            return false;
        }
        if (script == null) {
            if (other.script != null) {
                return false;
            }
        } else if (!script.equals(other.script)) {
            return false;
        }
        return true;
    }

    /**
     * Returns the locale string, consisting of language and optionally script
     * and region code.
     *
     * @return the locale string
     */
    public String getLocaleString() {
        StringBuilder result = new StringBuilder(12);
        result.append(language.getLanguage());
        if (script != null) {
            result.append('-');
            result.append(script);
        }
        if (!language.getCountry().isEmpty()) {
            result.append('-');
            result.append(script);
        }
        return result.toString();
    }

    /**
     * Returns a hash code for the object.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (language == null ? 0 : language.hashCode());
        result = prime * result + (script == null ? 0 : script.hashCode());
        return result;
    }

    /**
     * Returns whether this LangString is described by the condition node type.
     */
    @Override
    public boolean matches(ObjectType condition) {
        if (condition instanceof LangString) {
            LangString other = (LangString) condition;
            if (other.language != null && !other.language.equals(language)) {
                return false;
            }
            if (other.script != null && other.script.length() != 0 && !other.language.equals(language)) {
                return false;
            }
            return super.matches(condition);
        } else if (condition instanceof Node) {
            Node filter = (Node) condition;
            if (!ALLOWED_RELATIONS.containsAll(filter.getRelations())) {
                return false;
            }
            Result expectedLanguage = filter.get(XML_LANG);
            switch (expectedLanguage.size()) {
                case 0:
                break;
                case 1:
                    ObjectType checkLanguage = expectedLanguage.iterator().next();
                    if (!(checkLanguage instanceof Literal)) {
                        return false;
                    }
                    LocaleParseResult p = parseLocale(((Literal) checkLanguage).getValue());
                    if (!language.equals(p.language)) {
                        return false;
                    }
                    if (script == null && p.script != null) {
                        return false;
                    }
                    if (script != null && !script.equals(p.script)) {
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

    /**
     * Locale parser for locales with script codes.
     *
     * @param locale
     *            locale String
     * @return parse result, consisting of Java 6 locale and additional script
     *         field.
     */
    private final LocaleParseResult parseLocale(String locale) {
        Matcher parsed = PARSER.matcher(locale);
        if (!parsed.matches()) {
            throw new IllegalArgumentException("Cannot interpret language code: " + locale);
        }
        LocaleParseResult result = new LocaleParseResult();
        String language = parsed.group(PARSER_GROUP_LANGUAGE);
        result.script = parsed.group(PARSER_GROUP_SCRIPT);
        String region = parsed.group(PARSER_GROUP_REGION);
        result.language = region == null ? new Locale(language) : new Locale(language, region);
        return result;
    }

    /**
     * Converts this lang string to an RDFNode as part of a Jena model.
     *
     * @param model
     *            model to create objects in
     * @return an RDFNode representing this node
     */
    @Override
    public RDFNode toRDFNode(Model model) {
        return model.createLiteral(value, getLocaleString());
    }

    /**
     * Returns a readable description of this literal to be seen in a debugger.
     */
    @Override
    public String toString() {
        String language = getLocaleString();
        StringBuffer result = new StringBuffer(value.length() + (language != null ? language.length() + 1 : 0) + 2);
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
