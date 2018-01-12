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

package org.kitodo.api.ugh;

import org.kitodo.api.ugh.exceptions.ReadException;
import org.w3c.dom.Node;

public interface PicaPlusInterface extends FileformatInterface {

    /**
     * @return always {@code true}. The result value is never used.
     */
    boolean read(Node node) throws ReadException;
}
