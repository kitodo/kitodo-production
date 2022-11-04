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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.externaldatamanagement.ImportConfigurationType;
import org.kitodo.api.externaldatamanagement.SearchInterfaceType;
import org.kitodo.api.schemaconverter.FileFormat;
import org.kitodo.api.schemaconverter.MetadataFormat;
import org.kitodo.data.database.beans.ImportConfiguration;
import org.kitodo.data.database.beans.MappingFile;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.SearchField;
import org.kitodo.data.database.beans.UrlParameter;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.primefaces.model.DualListModel;

@Named
@ViewScoped
public class ImportConfigurationEditView extends BaseForm {

    private static final Logger logger = LogManager.getLogger(ImportConfigurationEditView.class);
    private ImportConfiguration importConfiguration = new ImportConfiguration();
    private List<SelectItem> searchFields = new ArrayList<>();
    private static final List<String> SRU_VERSIONS = List.of("1.1", "1.2", "2.0");
    private static final List<String> SCHEMES = List.of("https", "http", "ftp");
    private static final List<String> PARENT_ELEMENT_TYPES = Collections.singletonList("reference");
    private static final List<String> PARENT_ELEMENT_TRIM_MODES = Collections.singletonList("parenthesis");
    private static final List<String> DEFAULT_ID_XPATHS;
    private static final List<String> DEFAULT_TITLE_XPATHS;
    private static final String URL_PARAMETER_UNIQUE = "importConfig.urlParameters.conditionUnique";

    static {
        DEFAULT_ID_XPATHS = List.of(
                ".//*[local-name()='recordInfo']/*[local-name()='recordIdentifier']/text()",
                ".//*[local-name()='datafield'][@tag='245']/*[local-name()='subfield'][@code='a']/text()",
                ".//*[local-name()='datafield'][@tag='003@']/*[local-name()='subfield'][@code='0']/text()"
        );
    }

    static {
        DEFAULT_TITLE_XPATHS = List.of(
                ".//*[local-name()='titleInfo']/*[local-name()='title']/text()",
                ".//*[local-name()='controlfield'][@tag='001']/text()",
                ".//*[local-name()='datafield'][@tag='021A']/*[local-name()='subfield'][@code='a']/text()"
        );
    }

    /**
     * Return list of default record ID XPaths.
     *
     * @return list of default record ID XPaths
     */
    public List<String> getRecordIdXPathDefault() {
        return DEFAULT_ID_XPATHS;
    }

    /**
     * Return list of default record title XPaths.
     *
     * @return list of default record title XPaths.
     */
    public List<String> getRecordTitleXPathDefault() {
        return DEFAULT_TITLE_XPATHS;
    }

    /**
     * Load import configuration by ID.
     *
     * @param id
     *            ID of import configuration to load
     */
    public void load(int id) {
        try {
            if (id > 0) {
                importConfiguration = ServiceManager.getImportConfigurationService().getById(id);
            } else {
                importConfiguration = new ImportConfiguration();
            }
            setSaveDisabled(true);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE,
                    new Object[] { ObjectType.IMPORT_CONFIGURATION.getTranslationSingular(), id }, logger, e);
        }
    }

    /**
     * Save import configuration.
     *
     * @return page or empty String
     */
    public String save() {
        try {
            ServiceManager.getImportConfigurationService().saveToDatabase(importConfiguration);
            return projectsPage;
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_SAVING,
                    new Object[] {ObjectType.IMPORT_CONFIGURATION.getTranslationSingular()}, logger, e);
            return this.stayOnCurrentPage;
        }
    }

    /**
     * Get importConfiguration.
     *
     * @return value of importConfiguration
     */
    public ImportConfiguration getImportConfiguration() {
        return importConfiguration;
    }

    /**
     * Set importConfiguration.
     *
     * @param importConfiguration as org.kitodo.data.database.beans.ImportConfiguration
     */
    public void setImportConfiguration(ImportConfiguration importConfiguration) {
        this.importConfiguration = importConfiguration;
    }

    /**
     * Get interface types.
     *
     * @return interface types
     */
    public List<SearchInterfaceType> getInterfaceTypes() {
        return List.of(SearchInterfaceType.values());
    }

    /**
     * Get metadata formats.
     *
     * @return metadata formats
     */
    public List<MetadataFormat> getMetadataFormats() {
        return List.of(MetadataFormat.values());
    }

    /**
     * Get file formats. Currently only XML is supported.
     *
     * @return file formats
     */
    public List<FileFormat> getFileFormats() {
        return Collections.singletonList(FileFormat.XML);
    }

    /**
     * Get import configuration types.
     *
     * @return import configuration types
     */
    public List<ImportConfigurationType> getImportConfigurationTypes() {
        return List.of(ImportConfigurationType.values());
    }

    /**
     * Get schemes.
     *
     * @return schemes
     */
    public List<String> getSchemes() {
        return SCHEMES;
    }

    /**
     * Add given SearchField to ImportConfiguration.
     *
     * @param searchField SearchField
     */
    public void addSearchField(SearchField searchField) {
        if (importConfiguration.getSearchFields().stream().map(SearchField::getLabel).collect(Collectors.toList())
                .contains(searchField.getLabel())) {
            Helper.setErrorMessage("SearchField labels must be unique ('" + searchField.getLabel()
                    + "' already exists!)");
        } else {
            searchField.setImportConfiguration(importConfiguration);
            importConfiguration.getSearchFields().add(searchField);
        }
    }

    /**
     * Replace SearchField with index 'searchFieldIndex' in ImportConfiguration's SearchField list with given
     * SearchField 'searchField'.
     * @param searchField new SearchField
     * @param searchFieldIndex index in ImportConfigurations SearchField list
     */
    public void updateSearchField(SearchField searchField, int searchFieldIndex) {
        importConfiguration.getSearchFields().remove(searchFieldIndex);
        importConfiguration.getSearchFields().add(searchFieldIndex, searchField);
    }

    /**
     * Remove given SearchField from ImportConfiguration.
     *
     * @param searchField SearchField
     */
    public void removeSearchField(SearchField searchField) {
        searchField.setImportConfiguration(null);
        importConfiguration.getSearchFields().remove(searchField);
    }

    /**
     * Add given UrlParameter to importConfiguration.
     *
     * @param urlParameter UrlParameter to add
     */
    public void addUrlParameter(UrlParameter urlParameter) {
        if (importConfiguration.getUrlParameters().stream().map(UrlParameter::getParameterKey)
                .collect(Collectors.toList()).contains(urlParameter.getParameterKey())) {
            Helper.setErrorMessage(URL_PARAMETER_UNIQUE, new Object[]{urlParameter.getParameterKey()});
        } else {
            urlParameter.setImportConfiguration(importConfiguration);
            importConfiguration.getUrlParameters().add(urlParameter);
        }
    }

    /**
     * Replace UrlParameter with index 'urlParameterIndex' in ImportConfiguration's UrlParameter list with given
     * UrlParameter 'urlParameter'.
     * @param urlParameter new UrlParameter
     * @param urlParameterIndex index in ImportConfigurations UrlParameter list
     */
    public void updateUrlParameter(UrlParameter urlParameter, int urlParameterIndex) {
        importConfiguration.getUrlParameters().remove(urlParameterIndex);
        importConfiguration.getUrlParameters().add(urlParameterIndex, urlParameter);
    }

    /**
     * Remove give UrlParameter from importConfiguration.
     *
     * @param urlParameter UrlParameter to remove
     */
    public void removeUrlParameter(UrlParameter urlParameter) {
        urlParameter.setImportConfiguration(null);
        importConfiguration.getUrlParameters().remove(urlParameter);
    }

    /**
     * Get list of available trim modes.
     *
     * @return list of available trim modes.
     */
    public List<String> getParentElementTrimModes() {
        return PARENT_ELEMENT_TRIM_MODES;
    }

    /**
     * Get list of available parent element types.
     *
     * @return list of available parent element types
     */
    public List<String> getParentElementTypes() {
        return PARENT_ELEMENT_TYPES;
    }

    /**
     * Get mapping files that can be assigned to the current import configuration.
     *
     * @return list of mapping files assignable to the current import configuration
     */
    public DualListModel<MappingFile> getAvailableMappingFiles() {
        List<MappingFile> assignedMappingFiles = importConfiguration.getMappingFiles();
        List<MappingFile> assignableMappingFiles = new ArrayList<>();
        try {
            assignableMappingFiles = ServiceManager.getMappingFileService().getAll();
            assignableMappingFiles.removeAll(assignedMappingFiles);
        } catch (DAOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        return new DualListModel<>(assignableMappingFiles
                .stream().sorted(Comparator.comparing(MappingFile::getTitle)).collect(Collectors.toList()),
                assignedMappingFiles);
    }

    /**
     * Get mapping files.
     *
     * @return mapping files
     */
    public List<MappingFile> getMappingFiles() {
        try {
            return ServiceManager.getMappingFileService().getAll();
        } catch (DAOException e) {
            Helper.setErrorMessage(e);
            return new ArrayList<>();
        }
    }

    /**
     * Set mapping files that can be assigned to the current import configuration.
     *
     * @param mappingFilesModel DualListModel containing the mapping files assignable and assigned
     *                          to the current import configuration
     */
    public void setAvailableMappingFiles(DualListModel<MappingFile> mappingFilesModel) {
        importConfiguration.setMappingFiles(new ArrayList<>(mappingFilesModel.getTarget()));
    }

    /**
     * Get SRU versions.
     *
     * @return SRU versions
     */
    public List<String> getSruVersions() {
        return SRU_VERSIONS;
    }

    /**
     * Return list of template processes.
     *
     * @return list of template processes
     */
    public List<Process> getTemplateProcesses() {
        try {
            return ServiceManager.getProcessService().getTemplateProcesses();
        } catch (DataException | DAOException e) {
            Helper.setErrorMessage(e);
            return new ArrayList<>();
        }
    }

    /**
     * Get searchFields.
     *
     * @return value of searchFields
     */
    public List<SelectItem> getSearchFields() {
        searchFields = new ArrayList<>();
        for (SearchField searchField : importConfiguration.getSearchFields()) {
            searchFields.add(new SelectItem(searchField.getLabel(), searchField.getLabel()));
        }
        return searchFields;
    }

    /**
     * Set searchFields.
     *
     * @param searchFields as List of SelectItems
     */
    public void setSearchFields(List<SelectItem> searchFields) {
        this.searchFields = searchFields;
    }

    /**
     * Get search field map.
     *
     * @return search field map
     */
    public Map<String, SearchField> getSearchFieldMap() {
        return importConfiguration.getSearchFields().parallelStream()
                .collect(Collectors.toMap(SearchField::getLabel, Function.identity()));
    }

    /**
     * Get idSearchField.
     *
     * @return idSearchField
     */
    public String getIdSearchField() {
        SearchField idSearchField = importConfiguration.getIdSearchField();
        return Objects.isNull(idSearchField) ? null : idSearchField.getLabel();
    }

    /**
     * Set idSearchField.
     *
     * @param idSearchField new idSearchField
     */
    public void setIdSearchField(String idSearchField) {
        importConfiguration.setIdSearchField(getSearchFieldMap().get(idSearchField));
    }

    /**
     * Get defaultSearchField.
     *
     * @return defaultSearchField
     */
    public String getDefaultSearchField() {
        SearchField defaultSearchField = importConfiguration.getDefaultSearchField();
        return Objects.isNull(defaultSearchField) ? null : defaultSearchField.getLabel();
    }

    /**
     * Set defaultSearchField.
     *
     * @param defaultSearchField new defaultSearchField
     */
    public void setDefaultSearchField(String defaultSearchField) {
        importConfiguration.setDefaultSearchField(getSearchFieldMap().get(defaultSearchField));
    }

    /**
     * Get parentIdSearchField.
     *
     * @return parentIdSearchField
     */
    public String getParentIdSearchField() {
        SearchField parentIdSearchField = importConfiguration.getParentSearchField();
        return Objects.isNull(parentIdSearchField) ? null : parentIdSearchField.getLabel();
    }

    /**
     * Set parentIdSearchField.
     *
     * @param parentIdSearchField new parentIdSearchField
     */
    public void setParentIdSearchField(String parentIdSearchField) {
        importConfiguration.setParentSearchField(getSearchFieldMap().get(parentIdSearchField));
    }
}
