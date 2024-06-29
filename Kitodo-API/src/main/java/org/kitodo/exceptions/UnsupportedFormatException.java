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

package org.kitodo.exceptions;

/**
 * This exception is thrown when no SchemaConverter implementation could be found for a given FileFormat or
 * MetadataFormat.
 */
public class UnsupportedFormatException extends Exception {

    /**
     * Standard constructor with a message String.
     *
     * @param message exception message
     */
    public UnsupportedFormatException(String message) {
        super(message);
    }

}
