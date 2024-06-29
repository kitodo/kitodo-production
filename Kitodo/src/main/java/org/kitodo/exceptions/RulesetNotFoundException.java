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

package org.kitodo.exceptions;

import java.io.FileNotFoundException;

import org.kitodo.production.helper.Helper;

public class RulesetNotFoundException extends FileNotFoundException {
    /**
     * Creates a new ruleset not found exception.
     *
     * @param missingFile
     *            name of missing ruleset file
     */
    public RulesetNotFoundException(String missingFile) {
        super(Helper.getTranslation("rulesetNotFound", missingFile));
    }
}
