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

/**
 * This exception is thrown during the import of catalog configurations from 'kitodo_opac.xml' when
 * a mandatory XML configuration element like "interfaceType" is missing.
 */
public class MandatoryParameterMissingException extends Exception {

    /**
     * Constructor with given parameter name.
     * @param parameterName name of missing parameter
     */
    public MandatoryParameterMissingException(String parameterName) {
        super("Mandatory XML parameter '" + parameterName + "' missing!");
    }
}
