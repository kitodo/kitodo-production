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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.ConfigurationException;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyDocStructHelperInterface;

/**
 * A DestinationReferenceSelector provides methods to retrieve document
 * structure nodes relative to the respective document structure that the result
 * of the operation shall be written to for reading from them.
 */
public class DestinationReferenceSelector extends DataSelector {

    /**
     * Regular expression pattern to parse the path string.
     */
    private static final Pattern DESTINATION_REFERENCE_SELECTOR_SCHEME = Pattern
            .compile(Pattern.quote(RESPECTIVE_DESTINATION_REFERENCE) + "(\\d+)([" + METADATA_PATH_SEPARATOR
                    + METADATA_SEPARATOR + "].*)");

    /**
     * Hierarchical level to retrieve (0 references the top level).
     */
    private final int index;

    /**
     * A further selector to read data relative to the resolved result of this
     * selector.
     */
    private final MetadataSelector nextSelector;

    /**
     * Creates a new DestinationReferenceSelector.
     *
     * @param path
     *            reference to resolve
     * @throws ConfigurationException
     *             if the path is syntactically wrong
     */
    public DestinationReferenceSelector(String path) throws ConfigurationException {
        Matcher pathSplitter = DESTINATION_REFERENCE_SELECTOR_SCHEME.matcher(path);
        if (!pathSplitter.find()) {
            throw new ConfigurationException("Invalid destination reference selector: " + path);
        }
        this.index = Integer.parseInt(pathSplitter.group(1));
        this.nextSelector = MetadataSelector.create(pathSplitter.group(2));
    }

    /**
     * Returns the document structure level indicated by the index form the
     * respective destination path.
     *
     * @see org.kitodo.production.metadata.copier.DataSelector#findIn(org.kitodo.production.metadata.copier.CopierData)
     */
    @Override
    public String findIn(CopierData data) {
        LegacyDocStructHelperInterface currentLevel = data.getLogicalDocStruct();
        MetadataSelector destination = data.getDestination();
        for (int descend = index; descend > 0; descend--) {
            if (!(destination instanceof MetadataPathSelector)) {
                return null;
            }
            int childReference = ((MetadataPathSelector) destination).getIndex();
            currentLevel = currentLevel.getAllChildren().get(childReference);
            destination = ((MetadataPathSelector) destination).getSelector();
        }
        return nextSelector.findIn(currentLevel);
    }

}
