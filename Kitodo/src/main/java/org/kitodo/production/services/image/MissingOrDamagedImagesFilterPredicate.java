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

package org.kitodo.production.services.image;

import java.net.URI;
import java.util.Optional;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.validation.State;
import org.kitodo.api.validation.ValidationResult;
import org.kitodo.api.validation.longtermpreservation.FileType;
import org.kitodo.production.model.Subfolder;
import org.kitodo.production.services.validation.LongTermPreservationValidationService;

/**
 * A filter predicate that checks if the file exists in the folder and can be
 * validated.
 */
public class MissingOrDamagedImagesFilterPredicate implements Predicate<Subfolder> {
    private static final Logger logger = LogManager.getLogger(MissingOrDamagedImagesFilterPredicate.class);

    /**
     * This message is written to the log if the image file could not be found.
     */
    private static final String IMAGE_MISSING = "Image {} not found in folder {}: Marked for generation.";

    /**
     * This message is written to the log if the image file cannot be validated
     * because no Validator is configured for the MIME type.
     */
    private static final String NO_VALIDATOR_CONFIGURED
            = "Image {} in folder {} cannot be validated: No validator configured. Image marked for regeneration.";

    /**
     * This message is written to the log if the image file of the long term
     * preservation validation interface was not considered to be knot-clean.
     */
    private static final String VALIDATION_NO_SUCCESS = "Image {} in folder {} was validated {}. Image marked for regeneration.";

    /**
     * This message is written to the log if the image file of the long term
     * preservation validation interface was considered to be knot-clean.
     */
    private static final String VALIDATION_SUCCESS = "Image {} in folder {} was validated {}.";

    /**
     * The canonical part of the filename. Usually this is the base name without
     * an extension, but in some special cases, it can only be part of the base
     * name. This is configured in the folder and separated in the content
     * folder.
     */
    private final String canonical;

    /**
     * Creates a filter predicate that checks if the file exists in the folder
     * and can be validated. The name of the file results from the settings of
     * the folder passed into the {@link #test(Subfolder)} function, and the
     * canonical name part and the variables.
     *
     * @param canonical
     *            the canonical part of the file name
     */
    public MissingOrDamagedImagesFilterPredicate(String canonical) {
        this.canonical = canonical;
    }

    /**
     * Check if there is a corresponding file in the folder and if it is valid.
     * The name of the file results from the settings of the folder, and the
     * canonical name part and the variables passed in the constructor. If there
     * is such a file, the long time preservation validation interface is
     * queried to check if the file is valid.
     *
     * @param folder
     *            folder where to find the file
     * @return true, if the picture needs to be generated
     */
    @Override
    public boolean test(Subfolder folder) {
        Optional<URI> imageURI = folder.getURIIfExists(canonical);
        if (!imageURI.isPresent()) {
            logger.info(IMAGE_MISSING, canonical, folder);
            return true;
        }
        Optional<FileType> fileType = folder.getFileFormat().getFileType();
        if (fileType.isPresent()) {
            LongTermPreservationValidationService serviceLoader = new LongTermPreservationValidationService();
            ValidationResult validated = serviceLoader.validate(imageURI.get(), fileType.get());
            if (validated.getState().equals(State.SUCCESS)) {
                logger.info(VALIDATION_SUCCESS, canonical, folder, validated.getState());
                return false;
            } else {
                logger.info(VALIDATION_NO_SUCCESS, canonical, folder, validated.getState());
                validated.getResultMessages().forEach(logger::debug);
                return true;
            }
        } else {
            logger.warn(NO_VALIDATOR_CONFIGURED, canonical, folder);
            return true;
        }
    }
}
