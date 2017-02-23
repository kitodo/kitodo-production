/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the GPL3-License.txt file that was
 * distributed with this source code.
 */

package org.kitodo.api.ruleseteditor;

import java.io.File;

public interface RulesetEditorInterface {

    /** Creates a new rulesetFile */
    File createRulesetFile();

    /**
     * Edits a given ruleset file.
     *
     * @param rulesetFile
     *            The rulesetfile to edit.
     * @return an edited rulesetfile.
     */
    File editRulesetfile(File rulesetFile);

}
