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

package org.kitodo.production.exceptions;

public class ExportFileException extends Exception {
    private static final long serialVersionUID = 8440137060804389036L;

    public ExportFileException(String s) {
        super(s);
    }
}
