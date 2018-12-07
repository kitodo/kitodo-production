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

package org.kitodo.api.dataeditor.rulesetmanagement;

/**
 * Provides a shared super interface for type view services. Type view services
 * provide filtered views on meta-data types.
 */
public interface MetadataViewInterface {
    /**
     * Returns the string used to encode this key in the meta-data file.
     *
     * @return the ID of this key
     */
    String getId();

    /**
     * Returns a label to display to the user.
     *
     * @return a label to display to the user
     */
    String getLabel();

    /**
     * Returns whether the key is a complex key. A complex key consists of
     * subfields, which in turn can be complex, while a simple key only takes a
     * single value. This can also be determined by type checking, but if it's
     * just about displaying two different icons or filling two different lists,
     * then this boolean is a helpful thing.
     *
     * @return whether the key is a complex key
     */
    boolean isComplex();

    /**
     * Returns whether the key is undefined. This can happen when opening a METS
     * file that contains meta-data that was not defined in the rule set. The
     * application should indicate this fact. In this case, the label
     * corresponds to the transferred key ID string, regardless of the language.
     * If the key is not complex, it is configured as a simple text-type key.
     *
     * @return whether the key is undefined
     */
    boolean isUndefined();
}
