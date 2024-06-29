/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.kitodo.api.ruleseteditor;

import java.net.URI;

/**
 * The ruleseteditor gives the possibility to edit existing rulesets and create
 * new rulesets.
 */
public interface RulesetEditorInterface {

    /** Creates a new rulesetFile. */
    URI createRulesetFile();

    /**
     * Edits a given ruleset file.
     *
     * @param rulesetFileUri
     *            The uri to the rulesetfile to edit.
     */
    void editRulesetFile(URI rulesetFileUri);

}
