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

package org.kitodo.dataeditor.ruleset;

import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.List;
import java.util.Locale.LanguageRange;
import java.util.Map;
import java.util.Optional;

import org.kitodo.api.dataeditor.rulesetmanagement.DatesSimpleMetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.Domain;
import org.kitodo.api.dataeditor.rulesetmanagement.InputType;

/**
 * A view on a key.
 */
class KeyView extends AbstractKeyView<UniversalKey> implements DatesSimpleMetadataViewInterface {
    /**
     * The universal rule for this key.
     */
    private UniversalRule universalRule;

    /**
     * The schema in which the part of the date relevant to this division is
     * stored. Apart from the dates built into Java and interpreted by the
     * runtime, there is still the special string “{@code yyyy/yyyy}”, which
     * stands for a double year, eg. an operation year that starts on a day
     * other than January 1. This works in conjunction with {@link #yearBegin}.
     */
    private String scheme;

    /**
     * The settings for this key.
     */
    private Settings settings;

    /**
     * Annual start of operation year.
     */
    private MonthDay yearBegin;

    /**
     * A new key view is created.
     *
     * @param universalKey
     *            the universal key
     * @param universalRule
     *            the universal rule
     * @param settings
     *            the settings
     * @param priorityList
     *            the user’s wish list for the preferred human language
     */
    KeyView(UniversalKey universalKey, UniversalRule universalRule, Settings settings,
            List<LanguageRange> priorityList) {

        super(universalKey, priorityList);
        this.universalRule = universalRule;
        this.settings = settings;
    }

    /**
     * Returns the predefined values for a new key.
     *
     * @return the predefined values
     */
    @Override
    public Collection<String> getDefaultItems() {
        return universal.getDefaultItems();
    }

    /**
     * Returns the input field type for the input field for this meta-data key.
     *
     * @return the input type
     */
    @Override
    public InputType getInputType() {
        /*
         * If the meta-data key has a type that requires a special input field,
         * return the corresponding field type.
         */
        switch (universal.getType()) {
            case ANY_URI: {
                return InputType.ONE_LINE_TEXT;
            }
            case BOOLEAN: {
                return InputType.BOOLEAN;
            }
            case DATE: {
                return InputType.DATE;
            }
            case INTEGER: {
                return InputType.INTEGER;
            }
            default: {
                // do noting
            }
        }

        /*
         * If the meta-data key defines options, return the corresponding
         * selection type.
         */
        if (universal.isHavingOptions()) {
            if (universalRule.isRepeatable()) {
                return InputType.MULTIPLE_SELECTION;
            }
            if (settings.isMultiline(universal.getId())) {
                return InputType.MULTI_LINE_SINGLE_SELECTION;
            } else {
                return InputType.ONE_LINE_SINGLE_SELECTION;
            }
        }

        // otherwise, check if the key is required to have an enlarged text box
        if (settings.isMultiline(universal.getId())) {
            return InputType.MULTI_LINE_TEXT;
        } else {
            return InputType.ONE_LINE_TEXT;
        }
    }

    @Override
    public String getScheme() {
        return scheme;
    }

    /**
     * Returns the possible values if the meta-data key is a list of values.
     * Depending on the operating mode, the values are either re-sorted or
     * restricted based on the specified values in the presence of a permit
     * rule.
     *
     * @return the possible values
     */
    @Override
    public Map<String, String> getSelectItems() {
        Map<String, String> selectItems = universal.getSelectItems(priorityList);
        return universalRule.getSelectItems(selectItems);
    }

    @Override
    public MonthDay getYearBegin() {
        return yearBegin;
    }

    @Override
    public boolean isEditable() {
        return settings.isEditable(universal.getId());
    }

    /**
     * Checks if a URI is in the configured namespace, if one has been
     * specified. Typically, a namespace is used as a URL prefix. For namespaces
     * on the WWW ending in {@code /}, the {@code /} required. For the other
     * namespaces on the WWW ending in {@code #}, the {@code #} is optional.
     * Both spellings are equivalent. Then there is the spelling with braces.
     * This is no longer a valid URL, but is also allowed here. If the name
     * space is not on the WWW (i.e. does not start with {@code http}), the
     * variant is also allowed to be a simple prefix. However, this only needs
     * to be checked if the namespace has not ended with a {@code /}, otherwise
     * the test is equivalent.
     *
     * @param uri
     *            URI for which you want to check if it is in the specified
     *            namespace.
     * @return true, if the URI is in the specified namespace or no namespace is
     *         specified
     */
    private boolean isLocatedInTheNamespace(String uri) {
        Optional<String> optionalNamespace = universal.getNamespace();
        if (optionalNamespace.isPresent()) {
            String namespaceAsStated = optionalNamespace.get();
            boolean endsWithSlash = namespaceAsStated.endsWith("/");
            String wwwNamespacePrefix = endsWithSlash || namespaceAsStated.endsWith("#") ? namespaceAsStated
                    : namespaceAsStated.concat("#");
            if (uri.startsWith(wwwNamespacePrefix) || uri.startsWith('{' + namespaceAsStated + '}')) {
                return true;
            }
            return !endsWithSlash && !namespaceAsStated.toLowerCase().startsWith("http")
                    && uri.startsWith(namespaceAsStated);
        }
        return true;
    }

    /**
     * Checks if a value for a key is valid. This means a basic check, if the
     * value fits the key at all and can be saved. If this check fails, the
     * interface should reject the input.
     *
     * @return whether a value is valid
     */
    @Override
    public boolean isValid(String value) {
        /*
         * Some data types are easily validated by Java built-in functions. We
         * will not implement it here again but try to create a corresponding
         * object. So it’s just about whether the constructor throws no mistake.
         * This simplifies the examination considerably.
         */
        try {
            switch (universal.getType()) {
                case ANY_URI: {
                    if (!isLocatedInTheNamespace(value)) {
                        return false;
                    }
                    new URI(value);
                    break;
                }
                case DATE: {
                    DateTimeFormatter.ISO_LOCAL_DATE.parse(value);
                    break;
                }
                case INTEGER: {
                    new BigInteger(value);
                    break;
                }
                default: {
                    if (!isLocatedInTheNamespace(value)) {
                        return false;
                    }
                }
            }
        } catch (URISyntaxException | DateTimeParseException | NumberFormatException e) {
            return false;
        }

        // If the key has options, then the value must be in it.

        if (universal.isHavingOptions() && !universalRule.getSelectItems(universal.getSelectItems()).contains(value)) {
            return false;
        }

        /*
         * Then we check against the regular expression, if there is one. The
         * tests can be combined with each other, then all conditions must
         * apply.
         */
        if (!universal.getPattern().isPresent()) {
            return true;
        }
        return universal.getPattern().get().matcher(value).matches();
    }

    void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public void setYearBegin(MonthDay yearBegin) {
        this.yearBegin = yearBegin;
    }

    @Override
    public Optional<Domain> getDomain() {
        return universal.getDomain();
    }
}
