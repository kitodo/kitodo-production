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

package org.kitodo.api.dataeditor.rulesetmanagement;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Locale.LanguageRange;
import java.util.Map;

/**
 * Interface for a service that provides access to the ruleset.
 *
 * <p>
 * The ruleset is an elongated XML file in an internal format that sets the
 * rules for editing METS files in the Kitodo.Production application profile.
 * Much of the complexity of the rule set is realized by this service and hidden
 * to the outside.
 */
public interface RulesetManagementInterface {
    /**
     * Returns the acquisition levels mentioned in the rule set. This function
     * can be used, for example, to populate the possibilities of a select
     * input.
     *
     * @return all acquisition levels showing up
     */
    Collection<String> getAcquisitionStages();

    /**
     * Returns all outline elements. The result is a map whose keys are the ID
     * strings of the outline elements. The mapped values are the labels in the
     * language best suited to the given language priority list. This function
     * can be used, for example, to populate the possibilities of a select
     * input.
     *
     * @param priorityList
     *            weighted list of user-preferred display languages. Return
     *            value of the function {@link LanguageRange#parse(String)}.
     * @return all outline elements as map from IDs to labels
     */
    Map<String, String> getStructuralElements(List<LanguageRange> priorityList);

    /**
     * Returns a service that provides a view to the rule set. In the view, the
     * elements are selected for the adaptation, acquisition stage and division,
     * and labeled best suited to the given language priority list.
     *
     * @param structuralElement
     *            current division
     * @param acquisitionStage
     *            current acquisition level
     * @param priorityList
     *            weighted list of user-preferred display languages. Return
     *            value of the function {@link LanguageRange#parse(String)}.
     * @return a service that provides a view to the rule set
     */
    StructuralElementViewInterface getStructuralElementView(String structuralElement, String acquisitionStage,
            List<LanguageRange> priorityList);

    /**
     * Loads a ruleset into this management.
     *
     * @param rulesetFile
     *            ruleset to load
     * @throws IOException
     *             if the reading fails
     */
    void load(File rulesetFile) throws IOException;
}
