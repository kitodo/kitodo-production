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

package org.kitodo.api.validation;

import java.util.Collection;

public class ValidationResult {

    /** The state of the validation. */
    private State state;

    /** A list of result messages. */
    private Collection<String> resultMessages;

    public ValidationResult(State state, Collection<String> resultMessages) {
        this.state = state;
        this.resultMessages = resultMessages;
    }

    /**
     * Gets valid.
     *
     * @return The valid.
     */
    public State getState() {
        return state;
    }

    /**
     * Gets the resultMessages.
     *
     * @return The resultMessages.
     */
    public Collection<String> getResultMessages() {
        return resultMessages;
    }
}
