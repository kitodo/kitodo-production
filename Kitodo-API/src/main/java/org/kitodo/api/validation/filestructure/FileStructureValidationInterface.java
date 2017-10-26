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

import org.kitodo.api.validation.ValidationInterface;
import org.kitodo.api.validation.ValidationResult;

/** Validates a given xml file against a given xsd schema. */
public interface FileStructureValidationInterface extends ValidationInterface {

    /**
     * validates a xml file at a given uri against a xsd at a given location.
     * 
     * @param xmlFileUri
     *            The file to validate.
     * @param xsdFileUri
     *            The location of the schema to validate against.
     * @return A ValidationResult, with result boolean and resultMessages.
     */
    ValidationResult validate(URI xmlFileUri, URI xsdFileUri);

}
