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

package org.kitodo.exceptions;

import org.kitodo.production.helper.Helper;

/**
 * This exception is thrown during the import of catalog configurations from 'kitodo_opac.xml' if the catalog
 * configuration is missing the mandatory XML element 'mappingFiles' and no default mapping file is defined for the
 * given metadata format.
 */
public class MappingFilesMissingException extends CatalogConfigurationImportException {

    /**
     * Constructor with given metadata format.
     * @param metadataFormat name of metadata format
     */
    public MappingFilesMissingException(String metadataFormat) {
        super(Helper.getTranslation("importConfig.migration.error.mappingFilesMissing", metadataFormat));
    }
}
