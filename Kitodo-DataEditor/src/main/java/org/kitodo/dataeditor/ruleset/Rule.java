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
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Triple;
import org.kitodo.dataeditor.ruleset.xml.RestrictivePermit;
import org.kitodo.dataeditor.ruleset.xml.Ruleset;
import org.kitodo.dataeditor.ruleset.xml.Unspecified;

/**
 * A rule that may be in place or not. If there is no corresponding rule element
 * in the ruleset, the rule behaves as if there is a rule that declares no
 * restrictions.
 */
public class Rule {
    /**
     * Generates a triplet of rule with triple as a key. This is due to the
     * problem because the rule is basically the key is three fields and applies
     * everything.
     *
     * @param restrictivePermit
     *            restrictive permit for which a hash key is to be formed
     * @return key is triple
     */
    private static Triple<String, String, String> formAKeyForARuleInATemporaryMap(RestrictivePermit restrictivePermit) {
        return Triple.of(restrictivePermit.getDivision().orElse(null), restrictivePermit.getKey().orElse(null), restrictivePermit.getValue().orElse(null));
    }

    /**
     * Maybe a rule, but maybe not.
     */
    private Optional<RestrictivePermit> optionalRestrictivePermit;

    /**
     * The ruleset.
     */
    private Ruleset ruleset;

    /**
     * Constructor for a new rule. May come with a restrictive permit or
     * without.
     *
     * @param ruleset
     *            the ruleset
     * @param optionalRestrictivePermit
     *            there may be a restrictive permit, or not
     */
    public Rule(Ruleset ruleset, Optional<RestrictivePermit> optionalRestrictivePermit) {
        this.ruleset = ruleset;
        this.optionalRestrictivePermit = optionalRestrictivePermit;
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
            Function<RestrictivePermit, Optional<String>> getter) {
        if (optionalRestrictivePermit.isPresent()) {
            Map<String, String> filteredPossibilities = new LinkedHashMap<>();
            RestrictivePermit restrictivePermit = optionalRestrictivePermit.get();
            for (RestrictivePermit permit : restrictivePermit.getPermits()) {
                Optional<String> getterResult = getter.apply(permit);
                if (getterResult.isPresent()) {
                    String entry = getterResult.get();
                    if (possibilities.containsKey(entry)) {
                        filteredPossibilities.put(entry, possibilities.get(entry));
                    }
                }
            }
            if (restrictivePermit.getUnspecified().equals(Unspecified.UNRESTRICTED)) {
                for (Entry<String, String> entryPair : possibilities.entrySet()) {
                    if (!filteredPossibilities.containsKey(entryPair.getKey())) {
                        filteredPossibilities.put(entryPair.getKey(), entryPair.getValue());
                    }
                }
            }
            return filteredPossibilities;
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
        return filterPossibilitiesBasedOnRule(divisions, RestrictivePermit::getDivision);
    }

    /**
     * Returns the key declarations explicitly allowed in the rule. This is done
     * by looking into rule and making explicit key declarations for it.
     *
     * @return the key declarations explicitly allowed
     */
    LinkedList<KeyDeclaration> getExplicitlyPermittedKeys(KeyDeclaration keyDeclaration) {
        LinkedList<KeyDeclaration> explicitlyPermittedKeys = new LinkedList<>();
        if (optionalRestrictivePermit.isPresent()) {
            for (RestrictivePermit permit : optionalRestrictivePermit.get().getPermits()) {
                Optional<String> optionalKey = permit.getKey();
                if (optionalKey.isPresent()) {
                    explicitlyPermittedKeys.add(keyDeclaration.getSubkeyDeclaration(optionalKey.get()));
                }
            }
        }
        return explicitlyPermittedKeys;
    }

    /**
     * Returns the maximum count, or maximum if undefined. This is not possible
     * anyway, otherwise you have to switch to a long.
     *
     * @return maximum count
     */
    int getMaxOccurs() {
        if (optionalRestrictivePermit.isPresent() && optionalRestrictivePermit.get().getMaxOccurs() != null) {
            return optionalRestrictivePermit.get().getMaxOccurs();
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
        if (optionalRestrictivePermit.isPresent() && optionalRestrictivePermit.get().getMinOccurs() != null) {
            return optionalRestrictivePermit.get().getMinOccurs();
        } else {
            return 0;
        }
    }

    /**
     * Returns a permit rule for a key.
     *
     * @param keyId
     *            key for which a permit rule is to be returned
     * @return permit rule for the key
     */
    Rule getRuleForKey(String keyId, boolean division) {
        Rule permitRuleForKey = optionalRestrictivePermit.isPresent()
                ? new Rule(ruleset,
                        optionalRestrictivePermit.get().getPermits().parallelStream()
                                .filter(rule -> keyId.equals(rule.getKey().orElse(null))).findAny())
                : new Rule(ruleset, Optional.empty());
        if (division) {
            permitRuleForKey.merge(ruleset.getRuleForKey(keyId));
        }
        return permitRuleForKey;
    }

    /**
     * Returns the selection items allowed by the rule.
     *
     * @param selectItems
     *            the selection items
     * @return the selection items
     */
    Map<String, String> getSelectItems(Map<String, String> selectItems) {
        return filterPossibilitiesBasedOnRule(selectItems, RestrictivePermit::getValue);
    }

    /**
     * Returns if after rule this is repeatable. (That’s if it’s not a rule, or
     * says more than 1.)
     *
     * @return whether this is repeatable
     */
    boolean isRepeatable() {
        return !optionalRestrictivePermit.isPresent() || optionalRestrictivePermit.get().getMaxOccurs() == null
                || optionalRestrictivePermit.get().getMaxOccurs() > 1;
    }

    /**
     * Returns whether unspecified is unrestricted.
     *
     * @return whether unspecified is unrestricted
     */
    boolean isUnspecifiedUnrestricted() {
        return !optionalRestrictivePermit.isPresent()
                || optionalRestrictivePermit.get().getUnspecified().equals(Unspecified.UNRESTRICTED);
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
    private RestrictivePermit merge(RestrictivePermit one, RestrictivePermit another) {
        RestrictivePermit merged = new RestrictivePermit();

        // we assume that both rules to merge are rules on the same entity
        merged.setDivision(one.getDivision());
        merged.setKey(one.getKey());
        merged.setValue(one.getValue());

        mergeQuantities(one, another, merged);

        // if one of both is forbidden, then it is forbidden
        merged.setUnspecified(
            one.getUnspecified().equals(Unspecified.FORBIDDEN) || another.getUnspecified().equals(Unspecified.FORBIDDEN)
                    ? Unspecified.FORBIDDEN
                    : Unspecified.UNRESTRICTED);

        // for sub-rules, apply recursively
        HashMap<Triple<String, String, String>, RestrictivePermit> anotherPermits = new LinkedHashMap<>();
        for (RestrictivePermit anotherPermit : another.getPermits()) {
            anotherPermits.put(formAKeyForARuleInATemporaryMap(anotherPermit), anotherPermit);
        }
        List<RestrictivePermit> mergedPermits = new LinkedList<>();
        for (RestrictivePermit onePermit : one.getPermits()) {
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
     * Combines two rules. The function happens in separate, this is just
     * wrapping.
     *
     * @param other
     *            the other rule
     */
    void merge(Rule other) {
        if (optionalRestrictivePermit.isPresent()) {
            if (other.optionalRestrictivePermit.isPresent()) {
                optionalRestrictivePermit = Optional
                        .of(merge(optionalRestrictivePermit.get(), other.optionalRestrictivePermit.get()));
            }
        } else {
            optionalRestrictivePermit = other.optionalRestrictivePermit;
        }
    }

    /**
     * This is taken out because otherwise checkstyle is unfortunate because
     * function is length 59 and should only be 50 lines. This is part of the
     * {@link #merge(RestrictivePermit, RestrictivePermit)} function and connects the quantities. Merge is
     * with strictness here, that is, the stricter value of both becomes valid.
     *
     * @param one
     *            one rule
     * @param another
     *            another rule
     * @param merged
     *            merged rule
     */
    private void mergeQuantities(RestrictivePermit one, RestrictivePermit another, RestrictivePermit merged) {
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
