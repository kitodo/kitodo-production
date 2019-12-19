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

public enum MetadataFormatConversion {
    MODS_2_KITODO("mods2kitodo.xsl", null),
    PICA_2_KITODO("pica2kitodo.xsl", null),
    MARC_2_MODS("marc21slim2mods.xsl", "https://www.loc.gov/standards/mods/v3/MARC21slim2MODS3-4.xsl");

    private String fileName;
    private String source;

    /**
     * Constructor setting filename and source URI for the XSL transformation file.
     * @param filename Filename including suffix without any path.
     * @param source Remote source where the file can be retrieved from if it is not yet available in Kitodo.
     */
    MetadataFormatConversion(String filename, String source) {
        this.fileName = filename;
        this.source = source;
    }

    /**
     * Get fileName.
     *
     * @return value of fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Get source.
     *
     * @return value of source
     */
    public String getSource() {
        return source;
    }
}
