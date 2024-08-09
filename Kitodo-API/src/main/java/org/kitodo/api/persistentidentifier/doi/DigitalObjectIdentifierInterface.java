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

package org.kitodo.api.persistentidentifier.doi;

public interface DigitalObjectIdentifierInterface {

    /**
     * Generates a DOI for the given list of parameters.
     *
     * @param parameters
     *            list of String parameters for DOI generation
     * @return a valid DOI
     */
    String generate(String... parameters);
}
