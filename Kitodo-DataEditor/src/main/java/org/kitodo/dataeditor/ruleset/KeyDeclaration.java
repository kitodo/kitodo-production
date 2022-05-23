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
 * A key declaration provides access to a key element in the ruleset. A key in
 * the ruleset can either be nestable or simple, both are both possible and have
 * the same declaration. Distinction is only view of the nested key view or
 * normal key view for it.
 */
class KeyDeclaration extends Labeled {
    /**
     * The key, if there is one.
     */
    private final Optional<Key> optionalKey;

    /**
     * Creates a key declaration for a known key.
     *
     * @param ruleset
     *            the ruleset
     * @param key
     *            the key
     */
    KeyDeclaration(Ruleset ruleset, Key key) {
        super(ruleset, key.getId(), key.getLabels(), false);
        optionalKey = Optional.of(key);
    }

    /**
     * Creates a key declaration for a known key.
     *
     * @param ruleset
     *            the ruleset
     * @param key
     *            the key
     * @param undefined
     *            whether he is undefined or not
     */
    KeyDeclaration(Ruleset ruleset, Key key, boolean undefined) {
        super(ruleset, key.getId(), key.getLabels(), undefined);
        optionalKey = Optional.of(key);
    }

    /**
     * Creates a key declaration for an unknown known key. A key is unknown if
     * it is not in the ruleset, but it is in the data. This case must be
     * handled.
     *
     * @param ruleset
     *            the ruleset
     * @param id
     *            the identifier of the key
     */
    KeyDeclaration(Ruleset ruleset, String id) {
        super(ruleset, id, Collections.emptyList(), true);
        optionalKey = Optional.empty();
    }

    /**
     * Create a new division declaration. This constructor is called by the
     * subclass {@link DivisionDeclaration} to create a division declaration,
     * which is a subclass of a key declaration, because a division is a nested
     * key with extra features.
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
    protected KeyDeclaration(Ruleset ruleset, String id, Collection<Label> labels, boolean undefined) {
        super(ruleset, id, labels, undefined);
        optionalKey = Optional.of(ruleset.getFictiousRulesetKey());
    }

    /**
     * Returns the default element(s) that will be displayed when creating a new
     * metadata entry. Empty, however, is the nominal case.
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
     * Returns a key declaration for a subkey (for nested keys).
     *
     * @param keyId
     *            identifier of the required key declaration
     * @return a key declaration for the sub-key
     */
    KeyDeclaration getSubkeyDeclaration(String keyId) {
        if (optionalKey.isPresent()) {
            Optional<Key> keyInKey = optionalKey.get().getKeys().parallelStream()
                    .filter(key -> keyId.equals(key.getId())).findAny();
            if (keyInKey.isPresent()) {
                return new KeyDeclaration(ruleset, keyInKey.get());
            }
        }
        return new KeyDeclaration(ruleset, keyId);
    }

    /**
     * Returns key declarations for all sub-keys.
     *
     * @return key declarations for all sub-keys
     */
    Collection<KeyDeclaration> getKeyDeclarations() {
        if (optionalKey.isPresent()) {
            return optionalKey.get().getKeys().parallelStream().map(key -> new KeyDeclaration(ruleset, key))
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    int getMinDigits() {
        return optionalKey.isPresent() ? optionalKey.get().getMinDigits() : 1;
    }

    /**
     * Returns the namespace of the key, if there is one. This is needed for
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
     * Returns whether the key declaration has options.
     *
     * @return whether the key declaration has options
     */
    boolean isWithOptions() {
        if (optionalKey.isPresent()) {
            return !optionalKey.get().getOptions().isEmpty();
        } else {
            return false;
        }
    }
}
