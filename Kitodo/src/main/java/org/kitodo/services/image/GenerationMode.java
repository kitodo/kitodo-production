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

package org.kitodo.services.image;

import java.util.function.Predicate;

import org.kitodo.model.UseFolder;

/**
 * Enumeration of the possible variants how images can be generated manually.
 */
public enum GenerationMode {
    /**
     * Gerenates all images.
     */
    ALL {
        @Override
        public Predicate<? super UseFolder> getFilter(String unused) {
            return λ -> true;
        }
    },

    /**
     * Generates all images that are missing in the destination folder.
     */
    MISSING {
        @Override
        public Predicate<? super UseFolder> getFilter(String canonical) {
            return new MissingImagesFilterPredicate(canonical);
        }
    },

    /**
     * Generates all images that are missing in the destination folder or that
     * do not validate.
     */
    MISSING_OR_DAMAGED {
        @Override
        public Predicate<? super UseFolder> getFilter(String canonical) {
            return new MissingOrDamagedImagesFilterPredicate(canonical);
        }
    };

    /**
     * Returns the corresponding filter for the generator variant.
     *
     * @param canonical
     *            canonical part of the file name
     * @return the filter for the generator variant
     */
    public abstract Predicate<? super UseFolder> getFilter(String canonical);
}
