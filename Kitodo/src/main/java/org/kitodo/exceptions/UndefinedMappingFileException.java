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
 * This exception is thrown during the import of catalog configurations from 'kitodo_opac.xml' when no MappingFile
 * instance was found for an XML transformation file referenced in the catalog configuration.
 */
public class UndefinedMappingFileException extends CatalogConfigurationImportException {

    /**
     * Constructor with given XSLT filename.
     * @param xsltFilename as String
     */
    public UndefinedMappingFileException(String xsltFilename) {
        super(Helper.getTranslation("importConfig.migration.error.mappingFileMissing", xsltFilename));
    }
}
