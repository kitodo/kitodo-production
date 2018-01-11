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

package org.kitodo.data.database.helper.enums;

import org.kitodo.api.ugh.FileformatInterface;
import org.kitodo.api.ugh.MetsModsInterface;
import org.kitodo.api.ugh.RDFFileInterface;
import org.kitodo.api.ugh.XStreamInterface;

public enum MetadataFormat {

    RDF("Rdf", true, RDFFileInterface.class),
    METS("Mets", true, MetsModsInterface.class),
    XSTREAM("XStream", true, XStreamInterface.class),
    METS_AND_RDF("Mets & Rdf", false, null);

    private final String name;
    private final boolean usableForInternal;
    private final Class<? extends FileformatInterface> clazz;

    MetadataFormat(String inName, boolean inUsableForInternal, Class<? extends FileformatInterface> implClass) {
        this.name = inName;
        this.usableForInternal = inUsableForInternal;
        this.clazz = implClass;
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

    public Class<? extends FileformatInterface> getImplClass() {
        return this.clazz;
    }
}
