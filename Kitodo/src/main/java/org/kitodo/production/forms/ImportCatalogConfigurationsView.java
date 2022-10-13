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

package org.kitodo.production.forms;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.apache.commons.configuration.ConfigurationException;
import org.kitodo.api.schemaconverter.MetadataFormat;
import org.kitodo.config.OPACConfig;
import org.kitodo.data.database.beans.MappingFile;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.helper.CatalogConfigurationImporter;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.primefaces.PrimeFaces;

@Named
@ViewScoped
public class ImportCatalogConfigurationsView implements Serializable {

    private Integer numberOfCatalogs;
    private List<String> catalogs;
    private List<String> selectedCatalogs;
    private HashMap<String, String> convertedCatalogs;
    private MappingFile currentMappingFile = null;
    private HashMap<String, MappingFile> allMappingFiles;
    private LinkedList<String> mappingFilenames;
    private int currentMappingFileIndex;
    private final CatalogConfigurationImporter importer;

    public ImportCatalogConfigurationsView() {
        importer = new CatalogConfigurationImporter();
    }

    /**
     * Get numberOfCatalogs.
     *
     * @return value of numberOfCatalogs
     */
    public Integer getNumberOfCatalogs() {
        if (Objects.isNull(numberOfCatalogs)) {
            numberOfCatalogs = OPACConfig.getCatalogs().size();
        }
        return numberOfCatalogs;
    }

    /**
     * Set numberOfCatalogs.
     *
     * @param numberOfCatalogs as int
     */
    public void setNumberOfCatalogs(Integer numberOfCatalogs) {
        this.numberOfCatalogs = numberOfCatalogs;
    }

    /**
     * Get catalogs.
     *
     * @return value of catalogs
     */
    public List<String> getCatalogs() {
        if (Objects.isNull(catalogs)) {
            catalogs = OPACConfig.getCatalogs();
        }
        return catalogs;
    }

    /**
     * Set catalogs.
     *
     * @param catalogs as List of String
     */
    public void setCatalogs(List<String> catalogs) {
        this.catalogs = catalogs;
    }

    /**
     * Get selectedCatalogs.
     *
     * @return value of selectedCatalogs
     */
    public List<String> getSelectedCatalogs() {
        if (Objects.isNull(selectedCatalogs) && Objects.nonNull(catalogs)) {
            selectedCatalogs = new LinkedList<>(catalogs);
        }
        return selectedCatalogs;
    }

    /**
     * Set selectedCatalogs.
     *
     * @param selectedCatalogs as List of String
     */
    public void setSelectedCatalogs(List<String> selectedCatalogs) {
        this.selectedCatalogs = selectedCatalogs;
    }

    public void prepare() {
        convertedCatalogs = new HashMap<>();
        currentMappingFileIndex = 0;
    }

    /**
     * Start catalog configuration import.
     */
    public void startImport() {
        try {
            importMappingFiles();
        } catch (DAOException | ConfigurationException e) {
            Helper.setErrorMessage(e);
        }
    }

    /**
     * Import mapping files.
     * @throws DAOException thrown when a mapping file could not be saved to the database.
     */
    private void importMappingFiles() throws DAOException, ConfigurationException {
        currentMappingFileIndex = 0;
        allMappingFiles = importer.getAllMappingFiles(selectedCatalogs);
        mappingFilenames = new LinkedList<>(allMappingFiles.keySet());
        if (!allMappingFiles.isEmpty() && currentMappingFileIndex < mappingFilenames.size()) {
            importMappingFile(mappingFilenames.get(currentMappingFileIndex));
        } else {
            importConfigurations();
        }
    }

    private void importMappingFile(String filename) throws DAOException, ConfigurationException {
        currentMappingFile = allMappingFiles.get(filename);
        if (Objects.isNull(currentMappingFile)) {
            currentMappingFile = new MappingFile();
            currentMappingFile.setFile(filename);
            currentMappingFile.setTitle(filename);
            PrimeFaces.current().ajax().update("mappingFileFormatsDialog");
            PrimeFaces.current().executeScript("PF('mappingFileFormatsDialog').show();");
        } else {
            currentMappingFileIndex++;
            if (currentMappingFileIndex < mappingFilenames.size()) {
                importMappingFile(mappingFilenames.get(currentMappingFileIndex));
            } else {
                importConfigurations();
            }
        }
    }

    /**
     * Add mapping file to database.
     * @throws DAOException when mapping file could not be saved to database
     */
    public void addMappingFile() throws DAOException {
        try {
            ServiceManager.getMappingFileService().saveToDatabase(currentMappingFile);
            currentMappingFileIndex++;
            if (currentMappingFileIndex < mappingFilenames.size()) {
                importMappingFile(mappingFilenames.get(currentMappingFileIndex));
            } else {
                importConfigurations();
            }
        } catch (ConfigurationException e) {
            Helper.setErrorMessage(e);
        }
    }

    private void importConfigurations() throws DAOException, ConfigurationException {
        convertedCatalogs = importer.importCatalogConfigurations(selectedCatalogs);
        PrimeFaces.current().ajax().update("catalogConfigurationImportResultDialog",  "configurationTable",
                "mappingTable");
        PrimeFaces.current().executeScript("PF('catalogConfigurationImportResultDialog').show()");
    }

    /**
     * Get list of catalog configurations successfully imported.
     * @return list of catalog configurations successfully imported.
     */
    public List<String> getSuccessfulImports() {
        if (Objects.nonNull(convertedCatalogs)) {
            return convertedCatalogs.entrySet().stream().filter(e -> Objects.isNull(e.getValue()))
                    .map(Map.Entry::getKey).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * Get list of failed catalog configuration imports.
     * @return list of failed catalog configuration imports.
     */
    public List<String> getFailedImports() {
        if (Objects.nonNull(convertedCatalogs)) {
            return convertedCatalogs.entrySet().stream().filter(e -> Objects.nonNull(e.getValue()))
                    .map(Map.Entry::getKey).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * Get error message for import of catalog configuration with name 'opacTitle'. Returns empty
     * String for successful imports.
     * @param opacTitle name of catalog configuration
     * @return error message for failed catalog configuration import; empty String for successful imports
     */
    public String getErrorMessage(String opacTitle) {
        if (convertedCatalogs.containsKey(opacTitle)) {
            return convertedCatalogs.get(opacTitle);
        }
        return "";
    }

    /**
     * Get currentMappingFile.
     *
     * @return value of currentMappingFile
     */
    public MappingFile getCurrentMappingFile() {
        return currentMappingFile;
    }

    /**
     * Set currentMappingFile.
     *
     * @param currentMappingFile as org.kitodo.data.database.beans.MappingFile
     */
    public void setCurrentMappingFile(MappingFile currentMappingFile) {
        this.currentMappingFile = currentMappingFile;
    }

    /**
     * Get metadata formats.
     *
     * @return metadata formats
     */
    public List<MetadataFormat> getMetadataFormats() {
        return List.of(MetadataFormat.values());
    }
}
