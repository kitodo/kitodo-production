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

package org.kitodo.api.ugh.exceptions;

public class PreferencesException extends UGHException {

    private static final long serialVersionUID = 1L;

    public PreferencesException() {
    }

    public PreferencesException(String message) {
        super(message);
    }

    public PreferencesException(String message, Throwable t) {
        super(message, t);
    }

    public PreferencesException(Throwable t) {
        super(t);
    }

}
