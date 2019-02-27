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

package org.kitodo.data.database.enums;

public enum MetadataFormat {

    RDF("Rdf", true),
    METS("Mets", true),
    XSTREAM("XStream", true),
    METS_AND_RDF("Mets & Rdf", false);

    private final String name;
    private final boolean usableForInternal;

    MetadataFormat(String inName, boolean inUsableForInternal) {
        this.name = inName;
        this.usableForInternal = inUsableForInternal;
    }

    public String getName() {
        return this.name;
    }

    public boolean isUsableForInternal() {
        return this.usableForInternal;
    }

    /**
     * Find file formats helper by name.
     *
     * @param inputName
     *            name
     * @return file format
     */
    public static MetadataFormat findFileFormatsHelperByName(String inputName) {
        for (MetadataFormat metadataFormat : MetadataFormat.values()) {
            if (metadataFormat.getName().equals(inputName)) {
                return metadataFormat;
            }
        }
        return XSTREAM;
    }

    public static MetadataFormat getDefaultFileFormat() {
        return XSTREAM;
    }
}
