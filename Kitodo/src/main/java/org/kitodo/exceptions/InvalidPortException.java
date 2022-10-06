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

/**
 * This exception is thrown during import of catalog configurations from 'kitodo_opac.xml' if the catalog
 * configuration contains a 'port' value that is not an integer in the range of valid ports from 0 to 65535.
 */
public class InvalidPortException extends CatalogConfigurationImportException {

    /**
     * Constructor with given invalid port value.
     *
     * @param portValue as String
     */
    public InvalidPortException(String portValue) {
        super(Helper.getTranslation("importConfig.migration.error.invalidPort", portValue));
    }
}
