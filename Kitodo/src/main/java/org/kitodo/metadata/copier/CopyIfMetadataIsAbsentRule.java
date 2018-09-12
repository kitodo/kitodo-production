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

package org.kitodo.metadata.copier;

import java.util.List;

import org.apache.commons.configuration.ConfigurationException;

/**
 * The CopyIfMetadataIsAbsentRule defines that a metadata is copied right to
 * left in case that the structure node defined on the left exists but doesn’t
 * yet have a metadata as named. Examples:
 *
 * <code>/@CurrentNoSorting ""= /*[0]@CurrentNoSorting</code> − copy the sort
 * number form the first child to the top struct if it doesn’t have a sort
 * number yet
 *
 * <code>/*[0]@TitleDocMain ""= /@TitleDocMain</code> − copy the main title from
 * the top struct to its first child element if it doesn’t have a main tile yet
 *
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class CopyIfMetadataIsAbsentRule extends DataCopyrule {

    /**
     * Symbolic operator representing the rule: ""=.
     */
    protected static final String OPERATOR = "\"\"=";

    /**
     * Element to apply the rule on.
     */
    private MetadataSelector destination;

    /**
     * Element to take the data from.
     */
    private DataSelector source;

    /**
     * This method actually applies the rule to the given fileformat.
     *
     * @see de.sub.goobi.metadaten.copier.DataCopyrule#apply(de.sub.goobi.metadaten.copier.CopierData)
     */
    @Override
    public void apply(CopierData data) {
        String value = source.findIn(data);
        if (value == null) {
            return;
        }
        destination.createIfPathExistsOnly(data, value);
    }

    /**
     * Returns the maximum number of objects this rule can accept, always 1.
     *
     * @see de.sub.goobi.metadaten.copier.DataCopyrule#getMaxObjects()
     */
    @Override
    protected int getMaxObjects() {
        return 1;
    }

    /**
     * Returns the minimum number of objects this rule requires, always 1.
     *
     * @see de.sub.goobi.metadaten.copier.DataCopyrule#getMinObjects()
     */
    @Override
    protected int getMinObjects() {
        return 1;
    }

    /**
     * Saves the source object path and creates a selector for it. The source
     * selector can be arbitrary DataSelector, which may be read-only.
     *
     * @see de.sub.goobi.metadaten.copier.DataCopyrule#setObjects(java.util.List)
     */
    @Override
    protected void setObjects(List<String> objects) throws ConfigurationException {
        source = DataSelector.create(objects.get(0));
    }

    /**
     * Saves the destination object path and creates a selector for it. The
     * destination selector must be a writable MetadataSelector.
     *
     * @see de.sub.goobi.metadaten.copier.DataCopyrule#setSubject(java.lang.String)
     */
    @Override
    protected void setSubject(String subject) throws ConfigurationException {
        destination = MetadataSelector.create(subject);
    }

    /**
     * Returns a string that textually represents this copy rule.
     *
     * @return a string representation of this copy rule
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return destination.toString() + ' ' + OPERATOR + ' ' + source.toString();
    }
}
