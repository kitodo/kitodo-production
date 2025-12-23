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

public enum MetadataFormat {
    MODS("mods-3-8.xsd"),
    MARC("MARC21slim.xsd"),
    PICA("pica-xml-v1-0.xsd"),
    EAD("ead.xsd"),
    OTHER(null),
    KITODO("kitodo.xsd");

    private final String schemaDefinitionFileName;

    /**
     * Constructs a MetadataFormat instance with the specified schema file name.
     *
     * @param schemaFileName
     *        the name of the schema definition file associated with this metadata format,
     *        or null if no file name is specified
     */
    MetadataFormat(String schemaFileName) {
        this.schemaDefinitionFileName = schemaFileName;
    }

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
        return OTHER;
    }

    /**
     * Get default record ID XPath for given format.
     *
     * @param format MetadataFormat
     * @return default record ID XPath for given format
     */
    public static String getDefaultRecordIdXpath(String format) {
        switch (format) {
            case "MODS":
                return ".//*[local-name()='recordInfo']/*[local-name()='recordIdentifier']/text()";
            case "MARC":
                return ".//*[local-name()='controlfield'][@tag='001']/text()";
            case "PICA":
                return ".//*[local-name()='datafield'][@tag='003@']/*[local-name()='subfield'][@code='0']/text()";
            default:
                return "";
        }
    }

    /**
     * Get default record title XPath for given format.
     *
     * @param format MetadataFormat
     * @return default record title XPath for given format
     */
    public static String getDefaultRecordTitleXpath(String format) {
        switch (format) {
            case "MODS":
                return ".//*[local-name()='titleInfo']/*[local-name()='title']/text()";
            case "MARC":
                return ".//*[local-name()='datafield'][@tag='245']/*[local-name()='subfield'][@code='a']/text()";
            case "PICA":
                return ".//*[local-name()='datafield'][@tag='021A']/*[local-name()='subfield'][@code='a']/text()";
            default:
                return "";
        }
    }

    /**
     * Retrieves the schema definition file name associated with this metadata format.
     *
     * @return the schema definition file name as a string, or null if no file name is defined
     */
    public String getSchemaDefinitionFileName() {
        return schemaDefinitionFileName;
    }
}
