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

import org.kitodo.api.validation.ValidationResult;

import java.io.File;
import java.net.URL;

public interface FileStructureValidationInterface {

    /**
     * validates a given xml file against a xsd at a given location.
     * @param xmlFile The file to validate.
     * @param xsdFileUrl The location of the schema to validate against.
     * @return A ValidationResult, with result boolean and resultMessages.
     */
    ValidationResult validate(File xmlFile, URL xsdFileUrl);

}
