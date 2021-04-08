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

import java.util.List;
import java.util.Locale.LanguageRange;
import java.util.Map;
import java.util.Optional;

import org.kitodo.api.dataeditor.rulesetmanagement.DatesSimpleMetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.dataeditor.ruleset.xml.Ruleset;

/**
 * A division view gives us a sight of a division. The view qualifies according
 * to the division, acquisition stage and language(s).
 */
class DivisionView extends NestedKeyView<DivisionDeclaration> implements StructuralElementViewInterface {
    /**
     * Creates a division view. The view qualifies according to the division,
     * acquisition stage and language(s).
     *
     * @param ruleset
     *            the ruleset
     * @param divisionDeclaration
     *            the division declaration
     * @param acquisitionStage
     *            the acquisition stage
     * @param priorityList
     *            the user's wish list for the best possible translation
     */
    public DivisionView(Ruleset ruleset, DivisionDeclaration divisionDeclaration, String acquisitionStage,
            List<LanguageRange> priorityList) {

        super(ruleset, divisionDeclaration,
                ruleset.getRuleForDivision(divisionDeclaration.getId()),
                ruleset.getSettings(acquisitionStage), priorityList, true);
    }

    /**
     * Returns all allowed subdivisions. This results from what are first in the
     * rule set, then which of the rule are allowed (and in which order) and at
     * the end it is then checked whether the division is not subdivided by date
     * (in this case, only the subdivisions to date are allowed).
     *
     * @return all allowed subdivisions
     */
    @Override
    public Map<String, String> getAllowedSubstructuralElements() {
        boolean hasSubdivisionByDate = declaration.hasSubdivisionByDate();
        Map<String, String> declaredDivisions = ruleset.getDivisions(priorityList, true, hasSubdivisionByDate);
        Map<String, String> filteredDivisions = rule.getAllowedSubdivisions(declaredDivisions);
        if (hasSubdivisionByDate) {
            return declaration.getAllowedSubdivisions(filteredDivisions);
        } else {
            return filteredDivisions;
        }
    }

    @Override
    public Optional<DatesSimpleMetadataViewInterface> getDatesSimpleMetadata() {
        Optional<KeyDeclaration> optionalDatesKey = declaration.getDatesKey();
        if (optionalDatesKey.isPresent()) {
            KeyView datesKeyView = new KeyView(optionalDatesKey.get(),
                    rule.getRuleForKey(optionalDatesKey.get().getId(), division), settings,
                    priorityList);
            datesKeyView.setScheme(declaration.getScheme());
            datesKeyView.setYearBegin(declaration.getYearBegin());
            return Optional.of(datesKeyView);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> getProcessTitle() {
        return declaration.getProcessTitle();
    }
}
