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

/**
 * Reads global preferences (rule set files) and provides methods to access
 * information and retrieve information about {@code MetadataType} and
 * {@code DocStructType} objects.
 */
public interface PrefsInterface {
    /**
     * Returns all document structure types defined in this rule set.
     *
     * @return all document structure types
     */
    List<DocStructTypeInterface> getAllDocStructTypes();

    /**
     * Returns the {@code DocStructType} named by its identifier, if there is
     * such in the rule set. Otherwise returns {@code null}.
     *
     * @param identifier
     *            identifier (internal name) of the {@code DocStructType}
     * @return the {@code DocStructType}, otherwise {@code null}.
     */
    DocStructTypeInterface getDocStrctTypeByName(String identifier);

    /**
     * Needs string as parameter and returns MetadataType object with this name.
     *
     * @param identifier
     *            parameter
     * @return MetadataType object with this name
     */
    MetadataTypeInterface getMetadataTypeByName(String identifier);

    /**
     * Loads all known DocStruct types from the prefs XML file.
     * 
     * @param fileName
     *            file to load
     * @return always {@code true}. The return value is never used.
     */
    boolean loadPrefs(String fileName) throws PreferencesException;
}
