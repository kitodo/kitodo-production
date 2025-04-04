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

package org.kitodo.longtermpreservationvalidation;

import java.net.URI;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.validation.ValidationResult;
import org.kitodo.api.validation.longtermpreservation.FileType;
import org.kitodo.api.validation.longtermpreservation.LongTermPreservationValidationInterface;

/**
 * A LongTermPreservationValidationInterface implementation using Jhove.
 */
public class LongTermPreservationValidation implements LongTermPreservationValidationInterface {

    private static final Logger logger = LogManager.getLogger(LongTermPreservationValidation.class);

    /**
     * Returns the matching module name for the given file type.
     */
    @SuppressWarnings("serial")
    private static final Map<FileType, String> MODULE_NAMES = new EnumMap<>(FileType.class);

    static {
        MODULE_NAMES.put(FileType.GIF, "GIF-hul");
        MODULE_NAMES.put(FileType.JPEG, "JPEG-hul");
        MODULE_NAMES.put(FileType.JPEG_2000, "JPEG2000-hul");
        MODULE_NAMES.put(FileType.PDF, "PDF-hul");
        MODULE_NAMES.put(FileType.PNG, "PNG-gdm");
        MODULE_NAMES.put(FileType.TIFF, "TIFF-hul");
    }



    /**
     * {@inheritDoc}<!-- . -->
     *
     * @param fileUri
     *            file URI to validate
     * @param fileType
     *            file type to validate
     */
    @Override
    public ValidationResult validate(URI fileUri, FileType fileType) {
        logger.error("LongTermPreservationValidation.validate");
        KitodoOutputHandler result = new KitodoOutputHandler();
        try {
            KitodoJhoveBase.validate(fileUri.getPath(), MODULE_NAMES.get(fileType), result);
        } catch (Exception e) {
            result.treatException(e);
        }
        return result.toValidationResult();
    }
}
