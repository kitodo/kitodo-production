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

package org.kitodo.api.validation.longtimepreservation;

import org.kitodo.api.validation.ValidationInterface;
import org.kitodo.api.validation.ValidationResult;

import java.nio.file.Path;

public interface LongTimePreservationValidationInterface extends ValidationInterface {

    /**
     * Validates an image for longTimePreservation.
     *
     * @param imageFilePath The path to the image, which should be validated.
     * @param fileType The fileType of the image at the given path.
     * @return A validation result.
     */
    ValidationResult validate(Path imageFilePath, FileType fileType);

}
