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

package org.kitodo.production.helper.metadata.legacytypeimplementations;

/**
 * The interface provides the method of storing the metadata in the binding.
 */
public interface BindingSaveInterface {
    /**
     * Saves the metadata in the binding.
     * 
     * @param legacyMetadataHelper
     *            metadata to save
     */
    void saveMetadata(LegacyMetadataHelper legacyMetadataHelper);
}
