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

package org.kitodo.exceptions;

import org.kitodo.production.helper.Helper;

public class NoSuchMetadataFieldException extends NoSuchFieldException {

    /**
     * The ID string on the non-existing field.
     */
    private final String key;

    /**
     * The human-readable label of the non-existing field.
     */
    private final String label;

    /**
     * Creates a new no such metadata field exception.
     *
     * @param key
     *            the ID string on the non-existing field
     * @param label
     *            the human-readable label of the non-existing field
     */
    public NoSuchMetadataFieldException(String key, String label) {
        this.key = key;
        this.label = label;
    }

    @Override
    public String getLocalizedMessage() {
        return Helper.getTranslation("dataEditor.invalidStructureField", label, key);
    }

    @Override
    public String getMessage() {
        return "Cannot save \"" + label + "\": There is no such field (" + key + ").";
    }
}
