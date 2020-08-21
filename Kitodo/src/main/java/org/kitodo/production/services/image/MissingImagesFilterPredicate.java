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

import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.production.model.Subfolder;

public class MissingImagesFilterPredicate implements Predicate<Subfolder> {
    private static final Logger logger = LogManager.getLogger(MissingImagesFilterPredicate.class);

    /**
     * This message is written to the logfile if the file was found.
     */
    private static final String IMAGE_FOUND = "Image {} was found in folder {}.";

    /**
     * This message is written to the logfile if the file was not found.
     */
    private static final String IMAGE_NOT_FOUND = "Image {} not found in folder {}: Marked for generation.";

    /**
     * The canonical part of the filename. Usually this is the base name without
     * an extension, but in some special cases, it can only be part of the base
     * name. This is configured in the folder and separated in the content
     * folder.
     */
    private final String canonical;

    /**
     * Creates a filter predicate that checks if the file exists in the folder.
     * The name of the file results from the settings of the folder passed into
     * the {@link #test(Subfolder)} function, and the canonical name part and the
     * variables.
     *
     * @param canonical
     *            the canonical part of the file name
     */
    public MissingImagesFilterPredicate(String canonical) {
        this.canonical = canonical;
    }

    /**
     * Check if there is a corresponding file in the folder. The name of the
     * file results from the settings of the folder, and the canonical name part
     * and the variables passed in the constructor.
     *
     * @return true, if the picture needs to be generated
     */
    @Override
    public boolean test(Subfolder destination) {
        boolean present = destination.getURIIfExists(canonical).isPresent();
        logger.debug(present ? IMAGE_FOUND : IMAGE_NOT_FOUND, canonical, destination);
        return !present;
    }
}
