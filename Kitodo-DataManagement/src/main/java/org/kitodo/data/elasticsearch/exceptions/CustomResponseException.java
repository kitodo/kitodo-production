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

package org.kitodo.data.elasticsearch.exceptions;

/**
 * Exception for checking code statuses from server responses.
 */
public class CustomResponseException extends Exception {

    public CustomResponseException(Exception exception) {
        super(exception);
    }

    public CustomResponseException(String message) {
        super(message);
    }
}
