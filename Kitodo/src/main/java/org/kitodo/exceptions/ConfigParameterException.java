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

public class ConfigParameterException extends RuntimeException {

    /**
     * Constructor with given parameter name and type for exception message.
     *
     * @param parameterName
     *            as String
     * @param type
     *            as String
     */
    public ConfigParameterException(String parameterName, String type) {
        super("Default value for key: " + parameterName + " not defined or have different type than " + type + "!");
    }
}
