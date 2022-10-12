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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum MetadataFormatConversion {
    MODS_2_KITODO("mods2kitodo.xsl", null, MetadataFormat.KITODO),
    PICA_2_KITODO("pica2kitodo.xsl", null, MetadataFormat.KITODO),
    MARC_2_MODS("marc21slim2mods.xsl", "https://www.loc.gov/standards/mods/v3/MARC21slim2MODS3-4.xsl",
            MetadataFormat.MODS);

    private static final Map<MetadataFormat, List<MetadataFormatConversion>> DEFAULT_CONVERSIONS = new HashMap<>();

    static {
        DEFAULT_CONVERSIONS.put(MetadataFormat.MODS, Collections.singletonList(MODS_2_KITODO));
        DEFAULT_CONVERSIONS.put(MetadataFormat.PICA, Collections.singletonList(PICA_2_KITODO));
        DEFAULT_CONVERSIONS.put(MetadataFormat.MARC, Arrays.asList(MARC_2_MODS, MODS_2_KITODO));
    }

    private final String fileName;
    private URL source;
    private final MetadataFormat targetFormat;

    /**
     * Constructor setting filename and source URI for the XSL transformation file.
     * @param filename Filename including suffix without any path.
     * @param source Remote source where the file can be retrieved from if it is not yet available in Kitodo.
     * @param targetFormat target MetadataFormat of this MetadataFormatConversion
     */
    MetadataFormatConversion(String filename, String source, MetadataFormat targetFormat) {
        this.fileName = filename;
        try {
            this.source = new URL(source);
        } catch (MalformedURLException e) {
            this.source = null;
        }
        this.targetFormat = targetFormat;
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
    public URL getSource() {
        return source;
    }

    /**
     * Get targetFormat.
     *
     * @return value of targetFormat
     */
    public MetadataFormat getTargetFormat() {
        return targetFormat;
    }

    /**
     * Get filename of default metadata conversion file for given metadata format.
     *
     * @param metadataFormat metadata format for which filename of default conversion file is returned
     * @return filename of default conversion file or null if no default conversion file exists for given format
     */
    public static List<MetadataFormatConversion> getDefaultConfigurationFileName(MetadataFormat metadataFormat) {
        if (DEFAULT_CONVERSIONS.containsKey(metadataFormat)) {
            return DEFAULT_CONVERSIONS.get(metadataFormat);
        }
        return null;
    }

}
