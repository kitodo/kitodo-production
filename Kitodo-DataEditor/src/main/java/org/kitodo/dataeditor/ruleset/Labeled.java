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

import java.text.Collator;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Locale.LanguageRange;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;
import org.kitodo.dataeditor.ruleset.xml.Label;
import org.kitodo.dataeditor.ruleset.xml.Ruleset;

/**
 * A provider for translated labels for elements. There are three of these, as
 * divisions, keys and options.
 */
public class Labeled {
    /**
     * The undefined language. Yes, there is something like that. But it isn’t
     * spoken by anyone.
     */
    private static final String UNDEFINED_LANGUAGE = "und";

    /**
     * The undefined language range. This is an area where only languages that
     * do not exist are spoken, so basically none are spoken.
     */
    private static final List<LanguageRange> UNDEFINED_LANGUAGE_RANGE = LanguageRange.parse(UNDEFINED_LANGUAGE);

    /**
     * The undefined locale. A place where the unspoken language is spoken. So
     * nowhere.
     */
    private static final Locale UNDEFINED_LOCALE = Locale.forLanguageTag(UNDEFINED_LANGUAGE);

    /**
     * Lists the entries alphabetically by the translated label, taking into
     * account the language preferred by the user for alphabetical sorting. In
     * the auxiliary map, the keys are sorted by the sequence translated label +
     * separator + key, to account for the case where multiple keys are
     * (inadvertently) translated equally.
     *
     * @param ruleset
     *            The ruleset. From the ruleset, we learn which language is a
     *            label that does not have a language attribute, that is what is
     *            the default language of the rule set.
     * @param elements
     *            The items to sort. The function is so generic that you can
     *            sort divisions, keys, and options with it.
     * @param keyGetter
     *            A function to extract from the element the value used in the
     *            result map as key.
     * @param labelGetter
     *            A function that allows you to extract the list of labels from
     *            the element and then select the best translated one.
     * @param priorityList
     *            The list of languages spoken by the user human
     * @return a map in the order of the best-fitting translated label
     */
    public static <T> LinkedHashMap<String, String> listByTranslatedLabel(Ruleset ruleset, Collection<T> elements,
            Function<T, String> keyGetter, Function<T, Collection<Label>> labelGetter,
            List<LanguageRange> priorityList) {

        Locale sortLocale = Locale.lookup(priorityList, Arrays.asList(Collator.getAvailableLocales()));
        TreeMap<String, Pair<String, String>> byLabelSorter = sortLocale != null
                ? new TreeMap<>(Collator.getInstance(sortLocale))
                : new TreeMap<>();
        for (T element : elements) {
            String key = keyGetter.apply(element);
            Labeled labeled = new Labeled(ruleset, key, labelGetter.apply(element));
            String label = labeled.getLabel(priorityList);
            byLabelSorter.put(label + '\037' + key, Pair.of(key, label));
        }
        LinkedHashMap<String, String> list = new LinkedHashMap<>((int) Math.ceil(elements.size() / 0.75));
        for (Pair<String, String> entry : byLabelSorter.values()) {
            list.put(entry.getKey(), entry.getValue());
        }
        return list;
    }

    /**
     * The identifier serves to identify itself.
     */
    protected final String id;

    /**
     * Label everywhere.
     */
    private final Collection<Label> labels;

    /**
     * The ruleset.
     */
    protected final Ruleset ruleset;

    /**
     * That may be whether he is undefined or not.
     */
    private final Boolean undefined;

    /**
     * Constructor for a provider for translated labels.
     *
     * @param ruleset
     *            the ruleset
     * @param id
     *            the identifier
     * @param labels
     *            the labels
     */
    Labeled(Ruleset ruleset, String id, Collection<Label> labels) {
        this.ruleset = ruleset;
        this.id = id;
        this.labels = labels;
        this.undefined = null;
    }

    /**
     * Constructor for a provider for translated labels. This is protected and
     * called only by subclasses, as part of them. Every key declaration, also
     * nesting keys and divisions are labeled.
     *
     * @param ruleset
     *            the ruleset
     * @param id
     *            the identifier
     * @param labels
     *            the labels
     * @param undefined
     *            whether he is undefined or not
     */
    protected Labeled(Ruleset ruleset, String id, Collection<Label> labels, boolean undefined) {
        this.ruleset = ruleset;
        this.id = id;
        this.labels = labels;
        this.undefined = undefined;
    }

    /**
     * Returns the identifier.
     *
     * @return the identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the etiquette in the favorite language.
     *
     * @param priorityList
     *            wish list for favorite language
     * @return the etiquette in the favorite language
     */
    String getLabel(List<LanguageRange> priorityList) {
        Map<Locale, String> labelMap = new HashMap<>();
        for (Label label : labels) {
            Optional<Locale> langAttribute = label.getLanguage();
            if (langAttribute.isPresent()) {
                labelMap.put(langAttribute.get(), label.getValue());
            } else {
                labelMap.put(new Locale(ruleset.getDefaultLang()), label.getValue());
                labelMap.put(UNDEFINED_LOCALE, label.getValue());
            }
        }
        Locale bestMatching = Locale.lookup(priorityList, labelMap.keySet());
        if (bestMatching == null) {
            bestMatching = Locale.lookup(UNDEFINED_LANGUAGE_RANGE, labelMap.keySet());
        }
        return Optional.ofNullable(labelMap.get(bestMatching)).orElse(id);
    }

    /**
     * Access method for the labels (to pass them to the
     * {@code listByTranslatedLabel()} method).
     *
     * @return the labels
     */
    public Collection<Label> getLabels() {
        return labels;
    }

    /**
     * Returns whether he is undefined or not.
     *
     * @return whether he is undefined or not
     */
    boolean isUndefined() {
        return undefined;
    }
}
