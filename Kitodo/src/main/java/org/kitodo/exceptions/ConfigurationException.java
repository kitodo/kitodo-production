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

public class ConfigurationException extends Exception {
    /**
     * Constructor with given exception message.
     *
     * @param exceptionMessage
     *            as String
     */
    public ConfigurationException(String exceptionMessage) {
        super(exceptionMessage);
    }

    /**
     * Constructor with given exception message and throwable cause.
     *
     * @param exceptionMessage as String
     * @param cause as Throwable
     */
    public ConfigurationException(String exceptionMessage, Throwable cause) {
        super(exceptionMessage, cause);
    }
}
