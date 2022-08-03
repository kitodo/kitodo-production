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

package org.kitodo.api.schemaconverter;

import java.util.UnknownFormatConversionException;

public enum MetadataFormat {
    MODS,
    MARC,
    PICA,
    OTHER,
    KITODO;

    /**
     * Determine and return the adequate MetadataFormat for given String 'formatString'.
     *
     * @param formatString
     *          String for which an adequate MetadataFormat will be determined and returned
     * @return MetadataFormat
     *          Adequate MetadataFormat for given String
     */
    public static MetadataFormat getMetadataFormat(String formatString) {
        for (MetadataFormat format : MetadataFormat.values()) {
            if (formatString.compareToIgnoreCase(format.toString()) == 0) {
                return format;
            }
        }
        throw new UnknownFormatConversionException("Unable to find MetadataFormat for given String '"
                + formatString + "'!");
    }
}
