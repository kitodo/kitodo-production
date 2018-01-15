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

package de.sub.goobi.helper.exceptions;

public class ImportPluginException extends Exception {
    private static final long serialVersionUID = -6443010347859488741L;

    public ImportPluginException(String message) {
        super(message);
    }

    public ImportPluginException(String message, Throwable cause) {
        super(message, cause);
    }

    public ImportPluginException(Throwable cause) {
        super(cause);
    }
}
