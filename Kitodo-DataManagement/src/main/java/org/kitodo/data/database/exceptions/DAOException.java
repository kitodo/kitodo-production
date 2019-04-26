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

package org.kitodo.data.database.exceptions;

public class DAOException extends Exception {

    public DAOException(Exception e) {
        super(e);
    }

    public DAOException(String string) {
        super(string);
    }
}
