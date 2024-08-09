/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
