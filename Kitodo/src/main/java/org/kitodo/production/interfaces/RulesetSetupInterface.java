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

package org.kitodo.production.interfaces;

import java.util.List;
import java.util.Locale.LanguageRange;

import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;

/**
 * An interface that a form needs to implement to include a metadata panel.
 * This design provides for using the metadata panel in different places of the
 * application.
 */
public interface RulesetSetupInterface {
    /**
     * Returns the ruleset management to access the ruleset.
     *
     * @return the ruleset
     */
    RulesetManagementInterface getRulesetManagement();

    /**
     * Returns the current acquisition stage to adapt the displaying of fields
     * accordingly.
     *
     * @return the current acquisition stage
     */
    String getAcquisitionStage();

    /**
     * Returns the language preference list of the editing user to display
     * labels and options in the user-preferred language.
     *
     * @return the language preference list
     */
    List<LanguageRange> getPriorityList();
}
