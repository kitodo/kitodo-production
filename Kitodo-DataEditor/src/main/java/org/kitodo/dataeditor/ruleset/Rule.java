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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.MetadataEntry;
import org.kitodo.dataeditor.ruleset.xml.Condition;
import org.kitodo.dataeditor.ruleset.xml.ConditionsMapInterface;
import org.kitodo.dataeditor.ruleset.xml.RestrictivePermit;
import org.kitodo.dataeditor.ruleset.xml.Ruleset;
import org.kitodo.dataeditor.ruleset.xml.Unspecified;

/**
 * A rule that may be in place or not. If there is no corresponding rule element
 * in the ruleset, the rule behaves as if there is a rule that declares no
 * restrictions.
 */
public class Rule {
    private static final Logger logger = LogManager.getLogger(Rule.class);

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
     * Returns only the allowed sub-divisions by rule, possibly only resorted.
     *
     * @param divisions
     *            list input
     * @return exit
     */
    Map<String, String> getAllowedSubdivisions(Map<String, String> divisions) {
        if (!optionalRestrictivePermit.isPresent()) {
            return divisions;
        }
        Map<String, String> filteredPossibilities = new LinkedHashMap<>();
        RestrictivePermit restrictivePermit = optionalRestrictivePermit.get();
        for (RestrictivePermit permit : restrictivePermit.getPermits()) {
            Optional<String> getterResult = permit.getDivision();
            if (getterResult.isPresent()) {
                String entry = getterResult.get();
                if (divisions.containsKey(entry)) {
                    filteredPossibilities.put(entry, divisions.get(entry));
                }
            }
        }
        if (restrictivePermit.getUnspecified().equals(Unspecified.UNRESTRICTED)) {
            for (Entry<String, String> entryPair : divisions.entrySet()) {
                if (!filteredPossibilities.containsKey(entryPair.getKey())) {
                    filteredPossibilities.put(entryPair.getKey(), entryPair.getValue());
                }
            }
        }
        return filteredPossibilities;
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
     * @param metadata
     *            metadata, for conditional select items
     * @return the selection items
     */
    Map<String, String> getSelectItems(Map<String, String> selectItems, List<Map<MetadataEntry, Boolean>> metadata) {
        if (!optionalRestrictivePermit.isPresent()) {
            return selectItems;
        }
        Map<String, String> filteredPossibilities = new LinkedHashMap<>();
        RestrictivePermit restrictivePermit = optionalRestrictivePermit.get();
        for (RestrictivePermit permit : getConditionalPermits(restrictivePermit, metadata)) {
            Optional<String> getterResult = permit.getValue();
            if (getterResult.isPresent()) {
                String entry = getterResult.get();
                if (selectItems.containsKey(entry)) {
                    filteredPossibilities.put(entry, selectItems.get(entry));
                }
            }
        }
        if (restrictivePermit.getUnspecified().equals(Unspecified.UNRESTRICTED)) {
            for (Entry<String, String> entryPair : selectItems.entrySet()) {
                if (!filteredPossibilities.containsKey(entryPair.getKey())) {
                    filteredPossibilities.put(entryPair.getKey(), entryPair.getValue());
                }
            }
        }
        return filteredPossibilities;
    }

    private static Collection<RestrictivePermit> getConditionalPermits(RestrictivePermit restrictivePermit,
            List<Map<MetadataEntry, Boolean>> metadata) {

        Collection<RestrictivePermit> result = new ArrayList<>(restrictivePermit.getPermits());
        Map<String, Optional<MetadataEntry>> metadataCache = new HashMap<>();
        getConditionalPermitsRecursive(restrictivePermit, metadataCache, metadata, result);
        return result;
    }

    private static void getConditionalPermitsRecursive(ConditionsMapInterface conditionsMapInterface,
            Map<String, Optional<MetadataEntry>> metadataCache, List<Map<MetadataEntry, Boolean>> metadata,
            Collection<RestrictivePermit> result) {

        for (String conditionKey : conditionsMapInterface.getConditionKeys()) {
            Optional<MetadataEntry> possibleMetadata = metadataCache.computeIfAbsent(conditionKey,
                key -> getMetadataEntryForKey(key, metadata));
            if (possibleMetadata.isPresent()) {
                Condition condition = conditionsMapInterface.getCondition(conditionKey, possibleMetadata.get().getValue());
                if (Objects.nonNull(condition)) {
                    result.addAll(condition.getPermits());
                    getConditionalPermitsRecursive(condition, metadataCache, metadata, result);
                }
            }
        }
    }

    private static Optional<MetadataEntry> getMetadataEntryForKey(final String key,
            final List<Map<MetadataEntry, Boolean>> metadata) {
        String effectiveKey = key;
        int metadataIndex = metadata.size() - 1;
        while (effectiveKey.startsWith("../")) {
            effectiveKey = effectiveKey.substring(3);
            metadataIndex--;
        }
        if (metadataIndex < 0) {
            logger.warn("<condition key=\"{}\"> can never be met because metadata has only {} layers", key,
                metadata.size());
            return Optional.empty();
        }

        Map<MetadataEntry, Boolean> effectiveMetadata = metadata.get(metadataIndex);
        MetadataEntry found = null;
        for (Entry<MetadataEntry, Boolean> mapEntry : effectiveMetadata.entrySet()) {
            MetadataEntry metadataEntry = mapEntry.getKey();
            if (metadataEntry.getKey().equals(effectiveKey)) {
                found = metadataEntry;
                mapEntry.setValue(Boolean.TRUE);
                break;
            }
        }
        return Optional.ofNullable(found);
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
        HashMap<RestrictivePermit, RestrictivePermit> anotherPermits = new LinkedHashMap<>();
        for (RestrictivePermit anotherPermit : another.getPermits()) {
            anotherPermits.put(anotherPermit, anotherPermit);
        }
        List<RestrictivePermit> mergedPermits = new LinkedList<>();
        for (RestrictivePermit onePermit : one.getPermits()) {
            if (anotherPermits.containsKey(onePermit)) {
                mergedPermits.add(merge(onePermit, anotherPermits.get(onePermit)));
                anotherPermits.remove(onePermit);
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
