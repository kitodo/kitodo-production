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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/** Enables the conversion of a DataRecord from one format to another. */
public interface SchemaConverterInterface {

    /**
     * Converts a given DataRecord to the given MetadataFormat 'targetMetadataFormat' and FileFormat 'targetFileFormat'.
     *
     * @param record DataRecord to be converted
     * @param targetMetadataFormat MetadataFormat to which the given DataRecord is converted
     * @param targetFileFormat FileFormat to which the given DataRecord is converted
     * @param mappingFiles list of mapping files; if empty, the schema converter module uses a default mapping
     * @return The result of the conversion as a DataRecord.
     */
    DataRecord convert(DataRecord record, MetadataFormat targetMetadataFormat, FileFormat targetFileFormat,
                       List<File> mappingFiles) throws IOException, URISyntaxException;

    /**
     * Check and return whether the current SchemaConverter supports the given FileFormat as a target format or not.
     *
     * @param format target FileFormat
     * @return true, if given FileFormat 'format' is supported as a target FileFormat by the SchemaConverter.
     *         false otherwise
     */
    boolean supportsTargetFileFormat(FileFormat format);

    /**
     * Check and return whether the current SchemaConverter supports the given FileFormat as a source format or not.
     *
     * @param format source FileFormat
     * @return true, if given FileFormat 'format' is supported as a source FileFormat by the SchemaConverter.
     *         false otherwise
     */
    boolean supportsSourceFileFormat(FileFormat format);
}
