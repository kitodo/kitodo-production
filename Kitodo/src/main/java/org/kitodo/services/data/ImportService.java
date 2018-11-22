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

package org.kitodo.services.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.externaldatamanagement.ExternalDataImportInterface;
import org.kitodo.api.externaldatamanagement.SearchResult;
import org.kitodo.config.OPACConfig;
import org.kitodo.serviceloader.KitodoServiceLoader;

public class ImportService {

    private static final Logger logger = LogManager.getLogger(ImportService.class);

    private static ImportService instance = null;
    ExternalDataImportInterface importModule;

    /**
     * Return singleton variable of type ImportService.
     *
     * @return unique instance of ImportService
     */
    public static ImportService getInstance() {
        if (Objects.equals(instance, null)) {
            synchronized (ImportService.class) {
                if (Objects.equals(instance, null)) {
                    instance = new ImportService();
                }
            }
        }
        return instance;
    }

    /**
     * Load ExternalDataImportInterface implementation with KitodoServiceLoader and perform given query string
     * with loaded module.
     *
     * @param searchField field to query
     * @param searchTerm  given search term
     * @param catalogName catalog to search
     * @return search result
     */
    public SearchResult performSearch(String searchField, String searchTerm, String catalogName)
            throws IllegalArgumentException {
        importModule = initializeImportModule();
        try {
            OPACConfig.getOPACConfiguration(catalogName);
        } catch (IllegalArgumentException e) {
            logger.error(e.getLocalizedMessage());
            throw new IllegalArgumentException("Error: OPAC '" + catalogName + "' is not supported!");
        }

        return importModule.search(catalogName, searchField, searchTerm, 10);
    }

    private ExternalDataImportInterface initializeImportModule() {
        KitodoServiceLoader<ExternalDataImportInterface> loader =
                new KitodoServiceLoader<>(ExternalDataImportInterface.class);
        return loader.loadModule();
    }

    /**
     * Load search fields of catalog with given name 'opac' from OPAC configuration file and return them as a list
     * of Strings.
     *
     * @param opac name of catalog whose search fields are loaded
     * @return list containing search fields
     * @throws IllegalArgumentException thrown when configuration for catalog with name 'opac' cannot be found in
     *                                  opac configuration file
     */
    public List<String> getAvailableSearchFields(String opac) throws IllegalArgumentException {
        try {
            HierarchicalConfiguration searchFields = OPACConfig.getSearchFields(opac);
            List<String> fields = new ArrayList<>();
            for (HierarchicalConfiguration searchField : searchFields.configurationsAt("searchField")) {
                fields.add(searchField.getString("[@label]"));
            }
            return fields;
        } catch (IllegalArgumentException e) {
            logger.error(e.getLocalizedMessage());
            throw new IllegalArgumentException("Error: OPAC '" + opac + "' is not supported!");
        }
    }

    /**
     * Load catalog names from OPAC configuration file and return them as a list of Strings.
     *
     * @return list of catalog names
     * @throws IllegalArgumentException thrown if no catalogs are configured in OPAC configuration file
     */
    public List<String> getAvailableCatalogs() throws IllegalArgumentException {
        try {
            return OPACConfig.getCatalogs();
        } catch (IllegalArgumentException e) {
            logger.error(e.getLocalizedMessage());
            throw new IllegalArgumentException("Error: no supported OPACs found in configuration file!");
        }
    }
}
