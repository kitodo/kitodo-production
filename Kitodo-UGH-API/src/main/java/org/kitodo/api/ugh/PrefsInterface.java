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

import java.util.List;
import org.kitodo.api.ugh.exceptions.PreferencesException;

public interface PrefsInterface {
    List<DocStructTypeInterface> getAllDocStructTypes();

    DocStructTypeInterface getDocStrctTypeByName(String identifier);

    MetadataTypeInterface getMetadataTypeByName(String identifier);

    /**
     * @return always {@code true}. The return value is never used.
     */
    boolean loadPrefs(String string) throws PreferencesException;
}
