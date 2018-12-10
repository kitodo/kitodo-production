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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale.LanguageRange;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.kitodo.api.dataeditor.rulesetmanagement.Domain;
import org.kitodo.dataeditor.ruleset.xml.Key;
import org.kitodo.dataeditor.ruleset.xml.Label;
import org.kitodo.dataeditor.ruleset.xml.Option;
import org.kitodo.dataeditor.ruleset.xml.Ruleset;
import org.kitodo.dataeditor.ruleset.xml.Type;

/**
 * A universal key provides access to a key in the ruleset. A key in the ruleset
 * can either be nestable or simple, both are both possible and have the same
 * universal key. Distinction is only view of the nested key view or normal key
 * view for it.
 */
class UniversalKey extends Labeled {
    /**
     * The key, if there is one.
     */
    private final Optional<Key> optionalKey;

    /**
     * This constructor produces a universal key for a known key.
     *
     * @param ruleset
     *            the ruleset
     * @param key
     *            the key
     */
    UniversalKey(Ruleset ruleset, Key key) {
        super(ruleset, key.getId(), key.getLabels(), false);
        optionalKey = Optional.of(key);
    }

    /**
     * This constructor produces a universal key for an unknown key. A key is
     * unknown if it is not in the ruleset (but it is in the data!) This, one
     * must be able to handle and not the application crashes. Here we want to
     * be better than before when nothing went.
     *
     * @param ruleset
     *            the ruleset
     * @param id
     *            the identifier of the key
     */
    UniversalKey(Ruleset ruleset, String id) {
        super(ruleset, id, Collections.emptyList(), true);
        optionalKey = Optional.empty();
    }

    /**
     * Create a new universal division. This constructor is called by the
     * universal division to create a fake universal key for a division. Because
     * basically a division is nothing more than a nested key only with extra
     * feature. Basically, most of the data is pretty much the same anyway, just
     * different.
     *
     * @param ruleset
     *            the ruleset
     * @param id
     *            the identifier of the division
     * @param labels
     *            the labels of the division
     * @param undefined
     *            whether the division is unknown
     */
    protected UniversalKey(Ruleset ruleset, String id, Collection<Label> labels, boolean undefined) {
        super(ruleset, id, labels, undefined);
        optionalKey = Optional.of(ruleset.getFictiousRulesetKey());
    }

    /**
     * Returns the default element(s) that will be displayed when creating a new
     * meta-data entry. Empty, however, is the nominal case.
     *
     * @return the default element(s)
     */
    Collection<String> getDefaultItems() {
        if (!optionalKey.isPresent()) {
            return Collections.emptyList();
        }
        Key key = optionalKey.get();
        return key.getPresets();
    }

    Optional<Domain> getDomain() {
        if (optionalKey.isPresent()) {
            return Optional.ofNullable(optionalKey.get().getDomain());
        } else {
            return Optional.empty();
        }
    }

    /**
     * Issues a universal key for a subkey (for nested keys).
     *
     * @param keyId
     *            identifier of the required universal key
     * @return a universal key for the subkey
     */
    UniversalKey getUniversalKey(String keyId) {
        if (optionalKey.isPresent()) {
            Optional<Key> keyInKey = optionalKey.get().getKeys().parallelStream()
                    .filter(key -> keyId.equals(key.getId())).findAny();
            if (keyInKey.isPresent()) {
                return new UniversalKey(ruleset, keyInKey.get());
            }
        }
        return new UniversalKey(ruleset, keyId);
    }

    /**
     * Issues universal keys for all subkeys.
     *
     * @return a universal keys for all subkeys
     */
    Collection<UniversalKey> getUniversalKeys() {
        if (optionalKey.isPresent()) {
            return optionalKey.get().getKeys().parallelStream().map(key -> new UniversalKey(ruleset, key))
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Returns the namespace of the key if there is one. This is needed for
     * validation.
     *
     * @return the namespace of the key, if any
     */
    Optional<String> getNamespace() {
        if (!optionalKey.isPresent()) {
            return Optional.empty();
        }
        return optionalKey.get().getNamespace();
    }

    /**
     * Returns the key pattern if there is one. This is needed for validation.
     *
     * @return the key pattern, if any
     */
    Optional<Pattern> getPattern() {
        if (!optionalKey.isPresent() || optionalKey.get().getPattern() == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(Pattern.compile(optionalKey.get().getPattern()));
    }

    /**
     * Returns the selectable items for the key if the key is a controlled
     * vocabulary.
     *
     * @return the selectable items for the key
     */
    Set<String> getSelectItems() {
        if (!optionalKey.isPresent()) {
            return Collections.emptySet();
        }
        return optionalKey.get().getOptions().parallelStream().map(option -> option.getValue())
                .collect(Collectors.toSet());
    }

    /**
     * Returns the selectable items for the key if the key is a controlled
     * vocabulary. The identifiers for the values are best selected based on the
     * list of preferred human languages. The result is a linked hash map and in
     * the order in which the elements in the ruleset file were specified.
     *
     * @param priorityList
     *            language preference list
     * @return the selectable items for the key
     */
    Map<String, String> getSelectItems(List<LanguageRange> priorityList) {
        if (!optionalKey.isPresent()) {
            return Collections.emptyMap();
        }
        return Labeled.listByTranslatedLabel(ruleset, optionalKey.get().getOptions(), Option::getValue,
            Option::getLabels, priorityList);
    }

    /**
     * Returns the data type of the key.
     *
     * @return the data type
     */
    Type getType() {
        if (optionalKey.isPresent()) {
            return optionalKey.get().getType();
        } else {
            return Type.STRING;
        }
    }

    /**
     * Returns whether a key is complex. A key complexes when it breaks into
     * subkeys.
     *
     * @return whether a key is complex
     */
    boolean isComplex() {
        if (!optionalKey.isPresent()) {
            return false;
        }
        return !optionalKey.get().getKeys().isEmpty();
    }

    /**
     * Returns whether the universal key has options.
     *
     * @return whether the universal key has options
     */
    boolean isHavingOptions() {
        if (optionalKey.isPresent()) {
            return !optionalKey.get().getOptions().isEmpty();
        } else {
            return false;
        }
    }
}
