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

package org.kitodo.api.schemaconverter;

import java.util.UnknownFormatConversionException;

public enum FileFormat {
    XML,
    JSON,
    CSV;

    /**
     * Determine and return the adequate FileFormat for given String 'formatString'.
     *
     * @param formatString
     *          String for which an adequate FileFormat will be determined and returned
     * @return FileFormat
     *          Adequate FileFormat for given String
     */
    public static FileFormat getFileFormat(String formatString) {
        for (FileFormat format : FileFormat.values()) {
            if (formatString.compareToIgnoreCase(format.toString()) == 0) {
                return format;
            }
        }
        throw new UnknownFormatConversionException("Unable to find FileFormat for given String '"
                + formatString + "'!");
    }
}
