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

/**
 * This exception is thrown when an ExternalDataImportInterface implementation
 * cannot find a data record with the provided search criteria.
 */
public class NoRecordFoundException extends Exception {

    /**
     * Standard constructor with a message String.
     *
     * @param message exception message
     */
    public NoRecordFoundException(String message) {
        super(message);
    }

}
