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

/**
 * Interface to the PICA plus reader.
 */
public interface PicaPlusInterface extends FileformatInterface {

    /**
     * Reads a file and creates a digital document instance.
     *
     * @param filename
     *            full path to file which should be read
     * @return always {@code true}. The return value is never used.
     */
    boolean read(Node node) throws ReadException;
}
