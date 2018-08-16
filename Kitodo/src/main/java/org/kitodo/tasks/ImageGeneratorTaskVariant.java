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

package org.kitodo.tasks;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.validation.State;
import org.kitodo.api.validation.ValidationResult;
import org.kitodo.api.validation.longtimepreservation.FileType;
import org.kitodo.api.validation.longtimepreservation.LongTimePreservationValidationInterface;
import org.kitodo.config.xml.fileformats.FileFormat;
import org.kitodo.config.xml.fileformats.FileFormatsConfig;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.serviceloader.KitodoServiceLoader;

/**
 * Enumeration of the possible variants how images can be generated manually.
 */
public enum ImageGeneratorTaskVariant {
    /**
     * Gerenates all images.
     */
    ALL_IMAGES {
        @Override
        public Predicate<? super Folder> getFilter(Map<String, String> vars, String unused) {
            return λ -> true;
        }
    },

    /**
     * Generates all images that are missing in the destination folder.
     */
    MISSING_IMAGES {
        @Override
        public Predicate<? super Folder> getFilter(Map<String, String> vars, String canonical) {
            return λ -> {
                Logger logger = LogManager.getLogger(ImageGeneratorTaskVariant.class);
                try {
                    boolean present = λ.getURIIfExists(vars, canonical,
                        FileFormatsConfig.getFileFormat(λ.getMimeType()).get().getExtension(false)).isPresent();
                    if (present) {
                        logger.debug("Image {0} was found in folder {1}.", canonical, λ);
                        return false;
                    } else {
                        logger.debug("Image {0} not found in folder {1}: Marked for generation.", canonical, λ);
                        return true;
                    }
                } catch (IOException | JAXBException e) {
                    throw new UndeclaredThrowableException(e);
                }
            };
        }
    },

    /**
     * Generates all images that are missing in the destination folder or that
     * do not validate.
     */
    MISSING_OR_DAMAGED_IMAGES {
        @Override
        public Predicate<? super Folder> getFilter(Map<String, String> vars, String canonical) {
            return λ -> {
                Logger logger = LogManager.getLogger(ImageGeneratorTaskVariant.class);
                try {
                    FileFormat fileFormat = FileFormatsConfig.getFileFormat(λ.getMimeType()).get();
                    Optional<URI> imageURI = λ.getURIIfExists(vars, canonical, fileFormat.getExtension(false));
                    if (!imageURI.isPresent()) {
                        logger.info("Image {0} not found in folder {1}: Marked for generation.", canonical, λ);
                        return true;
                    } else {
                        Optional<FileType> fileType = fileFormat.getFileType();
                        if (fileType.isPresent()) {
                            KitodoServiceLoader<LongTimePreservationValidationInterface> longTimePreservationValidationInterfaceLoader
                                    = new KitodoServiceLoader<>(LongTimePreservationValidationInterface.class);
                            ValidationResult result = longTimePreservationValidationInterfaceLoader.loadModule()
                                    .validate(imageURI.get(), fileType.get());
                            if (result.getState().equals(State.SUCCESS)) {
                                logger.info("Image {0} in folder {1} was validated {2}.", canonical, λ,
                                    result.getState());
                                return false;
                            } else {
                                logger.info("Image {0} in folder {1} was validated {2}. Image marked for regeneration.",
                                    canonical, λ, result.getState());
                                result.getResultMessages().forEach(logger::debug);
                                return true;
                            }
                        } else {
                            logger.warn(
                                "Image {0} in folder {1} cannot be validated: No validator configured. Image marked for regeneration.",
                                canonical, λ);
                            return true;
                        }
                    }
                } catch (IOException | JAXBException e) {
                    throw new UndeclaredThrowableException(e);
                }
            };
        }
    };

    /**
     * Returns the corresponding filter for the generator variant.
     *
     * @param canonical
     *            canonical part of the file name
     * @return the filter for the generator variant
     */
    public abstract Predicate<? super Folder> getFilter(Map<String, String> vars, String canonical);
}
