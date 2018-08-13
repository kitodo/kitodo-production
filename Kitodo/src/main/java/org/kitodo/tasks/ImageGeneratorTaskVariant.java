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

import org.kitodo.api.validation.State;
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
                try {
                    return !λ.getURIIfExists(vars, canonical,
                        FileFormatsConfig.getFileFormat(λ.getMimeType()).get().getExtension(false)).isPresent();
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
                try {
                    FileFormat fileFormat = FileFormatsConfig.getFileFormat(λ.getMimeType()).get();
                    Optional<URI> imageURI = λ.getURIIfExists(vars, canonical, fileFormat.getExtension(false));
                    if (!imageURI.isPresent()) {
                        return false;
                    } else {
                        Optional<FileType> fileType = fileFormat.getFileType();
                        if (fileType.isPresent()) {
                            KitodoServiceLoader<LongTimePreservationValidationInterface> longTimePreservationValidationInterfaceLoader
                                    = new KitodoServiceLoader<>(LongTimePreservationValidationInterface.class);
                            return !longTimePreservationValidationInterfaceLoader.loadModule()
                                    .validate(imageURI.get(), fileType.get()).getState().equals(State.SUCCESS);
                        } else {
                            return false;
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
     * @return
     */
    public abstract Predicate<? super Folder> getFilter(Map<String, String> vars, String canonical);
}
