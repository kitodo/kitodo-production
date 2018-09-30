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

package org.kitodo.api.validation.metadata;

import java.net.URI;

import org.kitodo.api.validation.ValidationInterface;
import org.kitodo.api.validation.ValidationResult;

/** Validates a metadata file against a given ruleset. */
public interface MetadataValidationInterface extends ValidationInterface {

    /**
     * Validates if a mets file is confirm to a rulesetFile.
     *
     * @param metsFileUri
     *            The uri to the mets file which should be validated.
     * @param rulesetFileUri
     *            The uri to the ruleset file to validate against.
     * @return A validation result.
     */
    ValidationResult validate(URI metsFileUri, URI rulesetFileUri);

}
