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

package org.kitodo.validation.metadata;

import java.net.URI;

import org.kitodo.api.validation.ValidationResult;
import org.kitodo.api.validation.metadata.MetadataValidationInterface;

public class MetadataValidation implements MetadataValidationInterface {

    @Override
    public ValidationResult validate(URI metsFileUri, URI rulesetFileUri) {
        return null;
    }
}
