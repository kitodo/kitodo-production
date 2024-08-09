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

public class MetadataException extends RuntimeException {

    /**
     * Constructor with given exception message and exception itself.
     *
     * @param exceptionMessage
     *            as String
     * @param exception
     *            as Exception
     */
    public MetadataException(String exceptionMessage, Exception exception) {
        super(exceptionMessage, exception);
    }
}
