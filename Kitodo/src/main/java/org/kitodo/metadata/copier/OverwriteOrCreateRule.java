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
 * Data copy rule that either overwrites the metadata described by the selector
 * on the left hand side or creates it anew, if it isn’t yet present.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class OverwriteOrCreateRule extends DataCopyrule {

    /**
     * Operator representing the OverwriteOrCreateRule in the data copier
     * syntax.
     */
    protected static final String OPERATOR = "=";

    /**
     * Selector for the metadata to be overwritten or created.
     */
    private MetadataSelector destination;

    /**
     * Selector for the data to be copied.
     */
    private DataSelector source;

    /**
     * Applies the rule to the given data object.
     *
     * @param data
     *            data to apply the rule on
     * @see org.kitodo.metadata.copier.DataCopyrule#apply(org.kitodo.metadata.copier.CopierData)
     */
    @Override
    public void apply(CopierData data) {
        String value = source.findIn(data);
        if (value == null) {
            return;
        }
        destination.createOrOverwrite(data, value);
    }

    /**
     * Returns the minimal number of objects required by the rule to work as
     * expected, that is 1.
     *
     * @return always 1
     * @see org.kitodo.metadata.copier.DataCopyrule#getMinObjects()
     */
    @Override
    protected int getMinObjects() {
        return 1;
    }

    /**
     * Returns the maximal number of objects supported by the rule to work as
     * expected, that is 1.
     *
     * @return always 1
     * @see org.kitodo.metadata.copier.DataCopyrule#getMaxObjects()
     */
    @Override
    protected int getMaxObjects() {
        return 1;
    }

    /**
     * Saves the source object path.
     *
     * @see org.kitodo.metadata.copier.DataCopyrule#setObjects(java.util.List)
     */
    @Override
    protected void setObjects(List<String> objects) throws ConfigurationException {
        source = DataSelector.create(objects.get(0));
    }

    /**
     * Saves the destination object path.
     *
     * @see org.kitodo.metadata.copier.DataCopyrule#setSubject(java.lang.String)
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
