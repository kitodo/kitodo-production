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

package org.kitodo.production.services.validation;

import java.net.URI;

import org.kitodo.api.validation.ValidationResult;
import org.kitodo.api.validation.longtermpreservation.FileType;
import org.kitodo.api.validation.longtermpreservation.LongTermPreservationValidationInterface;
import org.kitodo.serviceloader.KitodoServiceLoader;

/**
 * This class does nothing more than call the methods on the long term
 * preservation validation interface.
 */
public class LongTermPreservationValidationService {

    private final LongTermPreservationValidationInterface longTermPreservationValidation;

    public LongTermPreservationValidationService() {
        longTermPreservationValidation = getValidationModule();
    }

    /**
     * Loads the module for long-term archival validation.
     *
     * @return the loaded module
     */
    private LongTermPreservationValidationInterface getValidationModule() {
        KitodoServiceLoader<LongTermPreservationValidationInterface> loader = new KitodoServiceLoader<>(
                LongTermPreservationValidationInterface.class);
        return loader.loadModule();
    }

    /**
     * Validates a file for longTimePreservation.
     *
     * @param fileUri
     *            The uri to the image, which should be validated.
     * @param fileType
     *            The fileType of the image at the given path.
     * @return A validation result.
     */
    public ValidationResult validate(URI fileUri, FileType fileType) {
        return longTermPreservationValidation.validate(fileUri, fileType);
    }
}
