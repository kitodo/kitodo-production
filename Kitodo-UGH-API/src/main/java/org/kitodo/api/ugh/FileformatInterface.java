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

import org.kitodo.api.ugh.exceptions.PreferencesException;
import org.kitodo.api.ugh.exceptions.ReadException;
import org.kitodo.api.ugh.exceptions.WriteException;

public interface FileformatInterface {

    DigitalDocumentInterface getDigitalDocument()
            throws PreferencesException /* Error on creating process */;

    /**
     * @return always {@code true}. The return value is never used.
     */
    boolean read(String path) throws PreferencesException, ReadException;

    /**
     * @return constantly {@code true} or {@code false}, depending on the
     *         implementing class. The return value is never used.
     */
    boolean setDigitalDocument(DigitalDocumentInterface newFile);

    boolean write(String string) throws PreferencesException, WriteException;

}
