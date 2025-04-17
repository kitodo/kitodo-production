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

package org.kitodo.api.validation.longtermpreservation;

import java.net.URI;
import java.util.List;

/** Validation for long-term preservation. */
public interface LongTermPreservationValidationInterface {

    /**
     * Validates a file for long-term preservation.
     *
     * @param fileUri
     *            The uri to the image, which should be validated.
     * @param fileType
     *            The fileType of the image at the given path.
     * @return A validation result. 
     */
    LtpValidationResult validate(URI fileUri, FileType fileType, List<? extends LtpValidationConditionInterface> conditions);

    /**
     * Based on a file type, return a list of possible properties that can be validated. 
     * The list is used for auto-suggesting property candiates to the user.
     * 
     * @param filetype the file type
     * @return a list of properties that can be extracted for a file type
     */
    List<String> getPossibleValidationConditionProperties(FileType filetype);
}
