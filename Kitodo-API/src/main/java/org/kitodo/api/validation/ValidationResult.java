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

import java.util.ArrayList;

public class ValidationResult {

    /** If the validation is valid. */
    private boolean valid;

    /** A list of result messages. */
    private ArrayList<String> resultMessages;

    /** Gets valid. */
    public boolean isValid() {
        return valid;
    }

    /** Sets valid. */
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    /** Gets the resultMessages. */
    public ArrayList<String> getResultMessages() {
        return resultMessages;
    }

    /** Sets the resultMessages. */
    public void setResultMessages(ArrayList<String> resultMessages) {
        this.resultMessages = resultMessages;
    }
}
