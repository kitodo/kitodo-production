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

package org.kitodo.api.validation.filestructure;

import java.net.URI;
import java.net.URL;

import org.kitodo.api.validation.ValidationResult;

public interface FileStructureValidationInterface {

    /**
     * validates a given xml file against a xsd at a given location.
     * 
     * @param xmlFileUri
     *            The file to validate.
     * @param xsdFileUrl
     *            The location of the schema to validate against.
     * @return A ValidationResult, with result boolean and resultMessages.
     */
    ValidationResult validate(URI xmlFileUri, URL xsdFileUrl);

}
