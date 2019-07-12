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

package org.kitodo.production.metadata.copier;

import org.apache.commons.configuration.ConfigurationException;

/**
 * A DataSelector is useful to access a data object. There are different
 * DataSelectors available to access metadata and program variables.
 *
 * <p>
 * The factory method {{@link #create(String)} can be used to retrieve a
 * DataSelector instance for a given path.
 */
public abstract class DataSelector {
    /**
     * Symbol indicating that the element of several to choose shall be the last
     * one.
     */
    protected static final String LAST_CHILD_QUANTIFIER = ">";

    /**
     * Symbol indicating that the next segment of the path is a document
     * structure hierarchy level.
     */
    protected static final String METADATA_PATH_SEPARATOR = "/";

    /**
     * Symbol indicating that the next segment of the path is a metadata.
     */
    protected static final String METADATA_SEPARATOR = "@";

    /**
     * Symbol indicating that the next segment of the path is a reference to the
     * node of the logical document structure that the metadata will be written
     * to.
     */
    protected static final String RESPECTIVE_DESTINATION_REFERENCE = "#";

    /**
     * Symbol indicating that the value is a static string.
     */
    private static final String STRING_MARK = "\"";

    /**
     * Symbol indicating that the selector is to select a variable.
     */
    protected static final String VARIABLE_REFERENCE = "$";

    /**
     * Factory method to create a DataSelector.
     *
     * @param path
     *            path to the data object to access
     * @return a subclass implementing the data selector required for the given
     *         path
     * @throws ConfigurationException
     *             if the path cannot be evaluated
     */
    public static DataSelector create(String path) throws ConfigurationException {
        if (path.startsWith(METADATA_PATH_SEPARATOR) || path.startsWith(METADATA_SEPARATOR)) {
            return MetadataSelector.create(path);
        }
        if (path.startsWith(VARIABLE_REFERENCE)) {
            return new VariableSelector(path);
        }
        if (path.startsWith(STRING_MARK)) {
            return new StringSelector(path);
        }
        if (path.startsWith(RESPECTIVE_DESTINATION_REFERENCE)) {
            return new DestinationReferenceSelector(path);
        }
        throw new ConfigurationException(
                "Cannot create data selector: Path must start with \"@\", \"/\" or \"$\", but is: " + path);
    }

    /**
     * Calling findIn() on the implementing instance should return the value of
     * the metadata named by the path used to construct the metadata selector.
     * Should return null if either the path or the metadata at the end of the
     * path aren’t available.
     *
     * @param data
     *            data collection to locate the metadata in
     * @return the value the path points to, or null if absent
     * @throws RuntimeException
     *             if the path cannot be resolved
     */
    public abstract String findIn(CopierData data);
}
