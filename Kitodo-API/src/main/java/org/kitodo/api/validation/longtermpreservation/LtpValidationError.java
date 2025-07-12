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

/**
 * General error types that might occur during image validation.
 */
public enum LtpValidationError {

    /**
     * The file to be validated cannot be found at the specified URI.
     */
    FILE_NOT_FOUND,

    /**
     * There was an IO error reading the file (e.g. permission denied).
     */
    IO_ERROR,

    /**
     * The image can not be validated because the file type is not supported by
     * the backend.
     */
    FILE_TYPE_NOT_SUPPORTED,

    /**
     * Any other error, e.g., an unknown exception in the validation backend.
     */
    UNKNOWN_ERROR,
}
