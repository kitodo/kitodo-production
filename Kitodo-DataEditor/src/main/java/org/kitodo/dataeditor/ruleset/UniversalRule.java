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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Triple;
import org.kitodo.dataeditor.ruleset.xml.Rule;
import org.kitodo.dataeditor.ruleset.xml.Ruleset;
import org.kitodo.dataeditor.ruleset.xml.Unspecified;

/**
 * A universal rule is a rule that may be in place or not. If there is no
 * corresponding rule element in the rule set, the rule behaves as if there is a
 * rule that declares no restrictions.
 */
public class UniversalRule {
    /**
     * Generates a triplet of rule with triple as a key. This is due to the
     * problem because the rule is basically the key is three fields and applies
     * everything.
     *
     * @param rule
     *            rule for which a hashish key is to be formed
     * @return key is tripple
     */
    private static Triple<String, String, String> formAKeyForARuleInATemporaryMap(Rule rule) {
        return Triple.of(rule.getDivision().orElse(null), rule.getKey().orElse(null), rule.getValue().orElse(null));
    }

    /**
     * Maybe a rule, but maybe not.
     */
    private Optional<Rule> optionalRule;

    /**
     * The ruleset.
     */
    private Ruleset ruleset;

    /**
     * Constructor for a new universal rule. Can with rule or without.
     *
     * @param ruleset
     *            the ruleset
     * @param optionalRule
     *            maybe a rule, but maybe not
     */
    public UniversalRule(Ruleset ruleset, Optional<Rule> optionalRule) {
        this.ruleset = ruleset;
        this.optionalRule = optionalRule;
    }

    /**
     * A filter to generate the possibilities based on a rule. This is because
     * the rule restricts the possibilities and gives order to elements, Or does
     * not restrict and gives order anyway for elements where mentioned and rest
     * is just like that. We have that twice for subdivisions and options so
     * this is summarized here and only getter is fetched from outside.
     *
     * @param possibilities
     *            list of possibilities unfiltered
     * @param getter
     *            which field to read
     * @return list is filtered
     */
    private Map<String, String> filterPossibilitiesBasedOnRule(Map<String, String> possibilities,
            Function<Rule, Optional<String>> getter) {
        if (optionalRule.isPresent()) {
            Map<String, String> result = new LinkedHashMap<>();
            Rule rule = optionalRule.get();
            for (Rule permit : rule.getPermits()) {
                Optional<String> getterResult = getter.apply(permit);
                if (getterResult.isPresent()) {
                    String entry = getterResult.get();
                    if (possibilities.containsKey(entry)) {
                        result.put(entry, possibilities.get(entry));
                    }
                }
            }
            if (rule.getUnspecified().equals(Unspecified.UNRESTRICTED)) {
                for (Entry<String, String> entryPair : possibilities.entrySet()) {
                    if (!result.containsKey(entryPair.getKey())) {
                        result.put(entryPair.getKey(), entryPair.getValue());
                    }
                }
            }
            return result;
        } else {
            return possibilities;
        }
    }

    /**
     * Returns only the allowed sub-divisions by rule, possibly only resorted.
     *
     * @param divisions
     *            list input
     * @return exit
     */
    Map<String, String> getAllowedSubdivisions(Map<String, String> divisions) {
        return filterPossibilitiesBasedOnRule(divisions, Rule::getDivision);
    }

    /**
     * Returns the universal keys explicitly allowed in the rule. This is done
     * by looking into rule and making explicit universal keys for it.
     *
     * @return the universal keys explicitly allowed
     */
    LinkedList<UniversalKey> getExplicitlyPermittedUniversalKeys(UniversalKey universalKey) {
        LinkedList<UniversalKey> result = new LinkedList<>();
        if (optionalRule.isPresent()) {
            for (Rule rule : optionalRule.get().getPermits()) {
                Optional<String> optionalKey = rule.getKey();
                if (optionalKey.isPresent()) {
                    result.add(universalKey.getUniversalKey(optionalKey.get()));
                }
            }
        }
        return result;
    }

    /**
     * Returns the maximum count, or maximum if undefined. This is not possible
     * anyway, otherwise you have to switch to a long.
     *
     * @return maximum count
     */
    int getMaxOccurs() {
        if (optionalRule.isPresent() && optionalRule.get().getMaxOccurs() != null) {
            return optionalRule.get().getMaxOccurs();
        } else {
            return Integer.MAX_VALUE;
        }
    }

    /**
     * Returns the minimum count, or zero if undefined.
     *
     * @return minimum count
     */
    int getMinOccurs() {
        if (optionalRule.isPresent() && optionalRule.get().getMinOccurs() != null) {
            return optionalRule.get().getMinOccurs();
        } else {
            return 0;
        }
    }

    /**
     * Returns a permission universal rule for a key.
     *
     * @param keyId
     *            key for which a permission universal rule is to be returned
     * @return permission universal rule for the key
     */
    UniversalRule getUniversalPermitRuleForKey(String keyId, boolean division) {
        UniversalRule result = optionalRule.isPresent()
                ? new UniversalRule(ruleset,
                        optionalRule.get().getPermits().parallelStream()
                                .filter(rule -> keyId.equals(rule.getKey().orElse(null))).findAny())
                : new UniversalRule(ruleset, Optional.empty());
        if (division) {
            result.merge(ruleset.getUniversalRestrictionRuleForKey(keyId));
        }
        return result;
    }

    /**
     * Returns the selection items.
     *
     * @param selectItems
     *            the selection items
     * @return the selection items
     */
    Set<String> getSelectItems(Set<String> selectItems) {
        return getSelectItems(selectItems.stream().collect(Collectors.toMap(Function.identity(), Function.identity())))
                .keySet();
    }

    /**
     * Returns the selection items. This is with filter, and besides, if it is
     * not multiple choice but optional then the first field is empty with empty
     * to select nothing as option. The question is if this must be here but I
     * have now made it for convenience, otherwise goes elsewhere.
     *
     * @param selectItems
     *            the selection items
     * @return the selection items
     */
    Map<String, String> getSelectItems(Map<String, String> selectItems) {
        Map<String, String> filteredOptions = filterPossibilitiesBasedOnRule(selectItems, Rule::getValue);
        if (!isRepeatable() && (!optionalRule.isPresent() || optionalRule.get().getMinOccurs() == null
                || optionalRule.get().getMinOccurs() < 1)) {
            Map<String, String> mapWithANonselectedElement = new LinkedHashMap<>(
                    (int) Math.ceil((filteredOptions.size() + 1) / 0.75));
            mapWithANonselectedElement.put("", "");
            mapWithANonselectedElement.putAll(filteredOptions);
            return mapWithANonselectedElement;
        } else {
            return filteredOptions;
        }
    }

    /**
     * Returns if after rule this is repeatable. (That’s if it’s not a rule, or
     * says more than 1.)
     *
     * @return whether this is repeatable
     */
    boolean isRepeatable() {
        return !optionalRule.isPresent() || optionalRule.get().getMaxOccurs() == null
                || optionalRule.get().getMaxOccurs() > 1;
    }

    /**
     * Returns whether unspecified is unrestricted.
     *
     * @return whether unspecified is unrestricted
     */
    boolean isUnspecifiedUnrestricted() {
        return !optionalRule.isPresent() || optionalRule.get().getUnspecified().equals(Unspecified.UNRESTRICTED);
    }

    /**
     * Combines two rules into each other. The first rule, if in doubt, is more
     * specific to the order of elements, otherwise it’s the same as around.
     * This is so if rule is nesting, and additional rule is found for key, then
     * merged and nesting rule is first and thus more specific to the case but
     * other rule otherwise considered as well. This is important but difficult
     * to implement and so it is done now.
     *
     * @param one
     *            a rule
     * @param another
     *            the other rule
     * @return merged rule
     */
    private Rule merge(Rule one, Rule another) {
        Rule merged = new Rule();

        /*
         * We assume that both rules are the same only, otherwise this would be
         * a problem. Recursively, this is not a problem, because the program
         * pays attention.
         */
        merged.setDivision(one.getDivision());
        merged.setKey(one.getKey());
        merged.setValue(one.getValue());

        mergeQuantities(one, another, merged);

        // here too, if one is forbidden then forbidden
        merged.setUnspecified(
            one.getUnspecified().equals(Unspecified.FORBIDDEN) || another.getUnspecified().equals(Unspecified.FORBIDDEN)
                    ? Unspecified.FORBIDDEN
                    : Unspecified.UNRESTRICTED);

        // and for sub-rule is recursive
        HashMap<Triple<String, String, String>, Rule> anotherPermits = new LinkedHashMap<>();
        for (Rule anotherPermit : another.getPermits()) {
            anotherPermits.put(formAKeyForARuleInATemporaryMap(anotherPermit), anotherPermit);
        }
        List<Rule> mergedPermits = new LinkedList<>();
        for (Rule onePermit : one.getPermits()) {
            Triple<String, String, String> key = formAKeyForARuleInATemporaryMap(onePermit);
            if (anotherPermits.containsKey(key)) {
                mergedPermits.add(merge(onePermit, anotherPermits.get(key)));
                anotherPermits.remove(key);
            } else {
                mergedPermits.add(onePermit);
            }
        }
        mergedPermits.addAll(anotherPermits.values());
        merged.setPermits(mergedPermits);

        return merged;
    }

    /**
     * Connects two universal rules. The function happens in separate, this is
     * just wrapping.
     *
     * @param other
     *            the other universal rule
     */
    void merge(UniversalRule other) {
        if (optionalRule.isPresent()) {
            if (other.optionalRule.isPresent()) {
                optionalRule = Optional.of(merge(optionalRule.get(), other.optionalRule.get()));
            }
        } else {
            optionalRule = other.optionalRule;
        }
    }

    /**
     * This is taken out because otherwise checkstyle is unfortunate because
     * function is length 59 and should only be 50 lines. This is part of the
     * {@link #merge(Rule, Rule)} function and connects the quantities. Merge is
     * with strictness here, that is, the stricter value of both becomes valid.
     *
     * @param one
     *            one rule
     * @param another
     *            another rule
     * @param merged
     *            merged rule
     */
    private void mergeQuantities(Rule one, Rule another, Rule merged) {
        if (one.getMinOccurs() == null) {
            merged.setMinOccurs(another.getMinOccurs());
        } else {
            if (another.getMinOccurs() == null) {
                merged.setMinOccurs(one.getMinOccurs());
            } else {
                merged.setMinOccurs(Math.max(one.getMinOccurs(), another.getMinOccurs()));
            }
        }

        if (one.getMaxOccurs() == null) {
            merged.setMaxOccurs(another.getMaxOccurs());
        } else {
            if (another.getMaxOccurs() == null) {
                merged.setMaxOccurs(one.getMaxOccurs());
            } else {
                merged.setMaxOccurs(Math.min(one.getMaxOccurs(), another.getMaxOccurs()));
            }
        }
    }
}
