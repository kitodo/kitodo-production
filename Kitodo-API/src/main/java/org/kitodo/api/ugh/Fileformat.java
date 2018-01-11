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

import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.WriteException;

public interface Fileformat {

    DigitalDocument getDigitalDocument()
            throws PreferencesException /* Error on creating process */;

    void read(String path) throws PreferencesException, ReadException;

    void setDigitalDocument(DigitalDocument newFile);

    boolean write(String string) throws WriteException;

}
