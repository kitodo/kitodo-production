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

import java.time.Month;
import java.time.MonthDay;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.kitodo.dataeditor.ruleset.xml.Division;
import org.kitodo.dataeditor.ruleset.xml.Key;
import org.kitodo.dataeditor.ruleset.xml.Ruleset;

/**
 * The division declaration provides access to a division in the rule set.
 */
public class DivisionDeclaration extends KeyDeclaration {
    /**
     * A reference to the division, if there is any.
     */
    private Optional<Division> optionalDivision;

    /**
     * Creates a new division declaration.
     *
     * @param ruleset
     *            the ruleset
     * @param division
     *            the division
     */
    public DivisionDeclaration(Ruleset ruleset, Division division) {
        super(ruleset, division.getId(), division.getLabels(), false);
        this.optionalDivision = Optional.of(division);
    }

    /**
     * Creates a new division declaration for an unknown division.
     *
     * @param ruleset
     *            the ruleset
     * @param id
     *            the identifier of the division
     */
    DivisionDeclaration(Ruleset ruleset, String id) {
        super(ruleset, id, Collections.emptyList(), true);
        this.optionalDivision = Optional.empty();
    }

    /**
     * Returns the list of allowed sub-divisions. Usually this is the passed
     * list filtered by the rule. Only the exception that a division might have
     * a subdivision by date is treated here. In that case, the only allowed
     * sub-division is the one that is next in the date hierarchy (except for
     * the last one, which is normal again).
     *
     * @param filteredSubdivisions
     *            the passed list filtered by the rule
     * @return the list of allowed sub-divisions
     */
    Map<String, String> getAllowedSubdivisions(Map<String, String> filteredSubdivisions) {
        if (optionalDivision.isPresent()) {
            List<Division> subdivisions = optionalDivision.get().getDivisions();
            if (!subdivisions.isEmpty()) {
                Map<String, String> allowedSubdivisions = new HashMap<>(2);
                String divisionId = subdivisions.get(0).getId();
                allowedSubdivisions.put(divisionId, filteredSubdivisions.get(divisionId));
                return allowedSubdivisions;
            }
        }
        for (Division division : ruleset.getDivisions()) {
            List<Division> divisions = division.getDivisions();
            for (int i = 0; i < divisions.size() - 1; i++) {
                if (divisions.get(i).getId().equals(id)) {
                    Map<String, String> allowedSubdivisions = new HashMap<>(2);
                    String divisionId = divisions.get(i + 1).getId();
                    allowedSubdivisions.put(divisionId, filteredSubdivisions.get(divisionId));
                    return allowedSubdivisions;
                }
            }
        }
        return filteredSubdivisions;
    }

    Optional<KeyDeclaration> getDatesKey() {
        if (optionalDivision.isPresent()) {
            Division division = optionalDivision.get();
            Optional<String> optionalDatesKeyId = division.getDates();
            if (optionalDatesKeyId.isPresent()) {
                Optional<Key> optionalKey = ruleset.getKey(optionalDatesKeyId.get());
                if (optionalKey.isPresent()) {
                    return Optional.of(new KeyDeclaration(ruleset, optionalKey.get()));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Returns the sequence description to form a process title.
     *
     * @return the key used to store the date
     */
    public Optional<String> getProcessTitle() {
        return optionalDivision.isPresent() ? optionalDivision.get().getProcessTitle() : Optional.empty();
    }

    /**
     * Returns division declarations for the subdivisions by date.
     *
     * @return division declarations for the subdivisions by date
     */
    public List<DivisionDeclaration> getAllowedDivisionDeclarations() {
        if (optionalDivision.isPresent()) {
            return optionalDivision.get().getDivisions().stream()
                    .map(division -> new DivisionDeclaration(ruleset, division)).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    String getScheme() {
        if (optionalDivision.isPresent()) {
            return optionalDivision.get().getScheme();
        } else {
            return "";
        }
    }

    MonthDay getYearBegin() {
        if (optionalDivision.isPresent()) {
            for (Division division : ruleset.getDivisions()) {
                List<Division> divisions = division.getDivisions();
                if (!divisions.isEmpty() && divisions.get(0).getId().equals(id)) {
                    String yearBegin = division.getYearBegin();
                    if (yearBegin != null) {
                        return MonthDay.parse(yearBegin);
                    }
                }
            }
        }
        return MonthDay.of(Month.JANUARY, 1);
    }

    /**
     * Finds out whether the division has subdivisions by date.
     *
     * @return whether the division has subdivisions by date
     */
    boolean hasSubdivisionByDate() {
        if (optionalDivision.isPresent() && !optionalDivision.get().getDivisions().isEmpty()) {
            return true;
        } else {
            for (Division division : ruleset.getDivisions()) {
                List<Division> divisions = division.getDivisions();
                for (int i = 0; i < divisions.size() - 1; i++) {
                    if (divisions.get(i).getId().equals(id)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    /**
     * Gets the division.
     *
     * @return the division
     */
    public Division getDivision() {
        return optionalDivision.orElseGet(Division::new);
    }
}
