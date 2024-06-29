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

package org.kitodo.config.enums;

public interface ParameterInterface {

    /**
     * Get name of parameter - derived from toString method. Every enum which
     * implements this interface needs to overwrite toString() method.
     *
     * @return value of name
     */
    default String getName() {
        return this.toString();
    }
}
