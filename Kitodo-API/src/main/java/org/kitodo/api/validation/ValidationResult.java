/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
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
