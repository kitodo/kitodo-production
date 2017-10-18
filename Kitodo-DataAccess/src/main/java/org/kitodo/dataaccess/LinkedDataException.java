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

package org.kitodo.dataaccess;

/**
 * A checked exception being thrown if one result is to be retrieved, but none
 * or more than one result is available.
 */
public class LinkedDataException extends Exception {
    private static final long serialVersionUID = 1L;
}
