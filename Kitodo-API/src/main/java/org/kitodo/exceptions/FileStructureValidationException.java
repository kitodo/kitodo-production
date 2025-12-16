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

import org.kitodo.api.validation.ValidationResult;

/**
 * This exception is thrown when an error occurs during the validation process.
 */
public class FileStructureValidationException extends Exception {

    private final ValidationResult validationResult;
    boolean externalDataValidation = false;

    /**
     * Constructs a new FileStructureValidationException with the specified detail
     * message and validation result.
     *
     * @param message the detail message describing the exception
     * @param validationResult the result of the file structure validation process
     */
    public FileStructureValidationException(String message, ValidationResult validationResult) {
        super(message);
        this.validationResult = validationResult;
    }

    /**
     * Constructor settings exception message, validation result and whether the validated data was external data or not.
     * @param message exception message
     * @param validationResult the result of the validation
     * @param externalDataValidation boolean flag signaling whether the validated data was external or not
     */
    public FileStructureValidationException(String message, ValidationResult validationResult, boolean externalDataValidation) {
        super(message);
        this.validationResult = validationResult;
        this.externalDataValidation = externalDataValidation;
    }

    /**
     * Get externalDataValidation.
     *
     * @return value of externalDataValidation
     */
    public boolean isExternalDataValidation() {
        return externalDataValidation;
    }

    /**
     * Get validationResult.
     *
     * @return value of validationResult
     */
    public ValidationResult getValidationResult() {
        return validationResult;
    }
}
