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

package org.kitodo.data.database.beans;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;
import org.kitodo.api.externaldatamanagement.ImportConfigurationType;
import org.kitodo.data.database.persistence.MappingFileDAO;
import org.kitodo.data.database.persistence.SearchFieldDAO;
import org.kitodo.data.database.persistence.UrlParameterDAO;

@Entity(name = "ImportConfiguration")
@Table(name = "importconfiguration")
public class ImportConfiguration extends BaseBean {

    @Column
    private String title;

    @Column
    private String description;

    @Column(name = "configuration_type")
    private String configurationType;

    @Column(name = "prestructured_import")
    private Boolean prestructuredImport = false;

    @Column(name = "interface_type")
    private String interfaceType;

    @Column(name = "return_format")
    private String returnFormat;

    @Column(name = "metadata_format")
    private String metadataFormat;

    @Column(name = "default_import_depth")
    private Integer defaultImportDepth;

    @Column(name = "parent_element_trim_mode")
    private String parentElementTrimMode;

    @Column
    private String host;

    @Column
    private String scheme;

    @Column
    private String path;

    @Column
    private Integer port;

    @Column(name = "anonymous_access")
    private boolean anonymousAccess = false;

    @Column
    private String username;

    @Column
    private String password;

    @Column(name = "query_delimiter")
    private String queryDelimiter;

    @Column(name = "item_field_xpath")
    private String itemFieldXpath;

    @Column(name = "item_field_owner_sub_path")
    private String itemFieldOwnerSubPath;

    @Column(name = "item_field_owner_metadata")
    private String itemFieldOwnerMetadata;

    @Column(name = "item_field_signature_sub_path")
    private String itemFieldSignatureSubPath;

    @Column(name = "item_field_signature_metadata")
    private String itemFieldSignatureMetadata;

    @Column(name = "id_prefix")
    private String idPrefix;

    @OneToMany(mappedBy = "importConfiguration", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SearchField> searchFields;

    @OneToMany(mappedBy = "importConfiguration", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UrlParameter> urlParameters;

    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "default_searchfield_id", referencedColumnName = "id")
    private SearchField defaultSearchField;

    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "identifier_searchfield_id", referencedColumnName = "id")
    private SearchField idSearchField;

    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "parent_searchfield_id", referencedColumnName = "id")
    private SearchField parentSearchField;

    @ManyToOne
    @JoinColumn(name = "default_templateprocess_id", foreignKey = @ForeignKey(name = "FK_importconfiguration_process_id"))
    private Process defaultTemplateProcess;

    @ManyToMany(cascade = CascadeType.PERSIST)
    @OrderColumn(name = "sorting")
    @JoinTable(name = "importconfiguration_x_mappingfile", joinColumns = {
        @JoinColumn(name = "importconfiguration_id",
                foreignKey = @ForeignKey(name = "FK_importconfiguration_x_mappingfile_importconfiguration_id")) },
            inverseJoinColumns = {
                @JoinColumn(name = "mappingfile_id",
                    foreignKey = @ForeignKey(name = "FK_importconfiguration_x_mappingfile_mappingfile_id")) })
    private List<MappingFile> mappingFiles;

    @ManyToOne
    @JoinColumn(name = "parent_mappingfile_id", foreignKey = @ForeignKey(name = "FK_parent_mappingfile_id"))
    private MappingFile parentMappingFile;

    @Column(name = "sru_version")
    private String sruVersion;

    @Column(name = "sru_record_schema")
    private String sruRecordSchema;

    @Column(name = "oai_metadata_prefix")
    private String oaiMetadataPrefix;

    @Column(name = "metadata_record_id_xpath")
    private String metadataRecordIdXPath;

    @Column(name = "metadata_record_title_xpath")
    private String metadataRecordTitleXPath;

    /**
     * Default constructor.
     */
    public ImportConfiguration() {
        defaultImportDepth = 2;
        searchFields = new ArrayList<>();
    }

    /**
     * Get title.
     *
     * @return value of title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set title.
     *
     * @param title as java.lang.String
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get description.
     *
     * @return value of description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get configurationType.
     *
     * @return value of configurationType
     */
    public String getConfigurationType() {
        return configurationType;
    }

    /**
     * Set configurationType.
     *
     * @param configurationType as java.lang.String
     */
    public void setConfigurationType(String configurationType) {
        this.configurationType = configurationType;
    }

    /**
     * Set description.
     *
     * @param description as java.lang.String
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get prestructuredImport.
     *
     * @return value of prestructuredImport
     */
    public Boolean getPrestructuredImport() {
        return prestructuredImport;
    }

    /**
     * Set prestructuredImport.
     *
     * @param prestructuredImport as java.lang.Boolean
     */
    public void setPrestructuredImport(Boolean prestructuredImport) {
        this.prestructuredImport = prestructuredImport;
    }

    /**
     * Get interfaceType.
     *
     * @return value of interfaceType
     */
    public String getInterfaceType() {
        return interfaceType;
    }

    /**
     * Set interfaceType.
     *
     * @param interfaceType as java.lang.String
     */
    public void setInterfaceType(String interfaceType) {
        this.interfaceType = interfaceType;
    }

    /**
     * Get returnFormat.
     *
     * @return value of returnFormat
     */
    public String getReturnFormat() {
        return returnFormat;
    }

    /**
     * Set returnFormat.
     *
     * @param returnFormat as java.lang.String
     */
    public void setReturnFormat(String returnFormat) {
        this.returnFormat = returnFormat;
    }

    /**
     * Get metadataFormat.
     *
     * @return value of metadataFormat
     */
    public String getMetadataFormat() {
        return metadataFormat;
    }

    /**
     * Set metadataFormat.
     *
     * @param metadataFormat as java.lang.String
     */
    public void setMetadataFormat(String metadataFormat) {
        this.metadataFormat = metadataFormat;
    }

    /**
     * Get defaultImportDepth.
     *
     * @return value of defaultImportDepth
     */
    public Integer getDefaultImportDepth() {
        return defaultImportDepth;
    }

    /**
     * Set defaultImportDepth.
     *
     * @param defaultImportDepth as java.lang.Integer
     */
    public void setDefaultImportDepth(Integer defaultImportDepth) {
        this.defaultImportDepth = defaultImportDepth;
    }

    /**
     * Get host.
     *
     * @return value of host
     */
    public String getHost() {
        return host;
    }

    /**
     * Set host.
     *
     * @param host as java.lang.String
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Get scheme.
     *
     * @return value of scheme
     */
    public String getScheme() {
        return scheme;
    }

    /**
     * Set scheme.
     *
     * @param scheme as java.lang.String
     */
    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    /**
     * Get path.
     *
     * @return value of path
     */
    public String getPath() {
        return path;
    }

    /**
     * Set path.
     *
     * @param path as java.lang.String
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Get port.
     *
     * @return value of port
     */
    public Integer getPort() {
        return port;
    }

    /**
     * Set port.
     *
     * @param port as java.lang.Integer
     */
    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * Get anonymousAccess.
     *
     * @return value of anonymousAccess
     */
    public boolean isAnonymousAccess() {
        return anonymousAccess;
    }

    /**
     * Set anonymousAccess.
     *
     * @param anonymousAccess as boolean
     */
    public void setAnonymousAccess(boolean anonymousAccess) {
        this.anonymousAccess = anonymousAccess;
    }

    /**
     * Get username.
     *
     * @return value of username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set username.
     *
     * @param username as java.lang.String
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Get password.
     *
     * @return value of password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set password.
     *
     * @param password as java.lang.String
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Get parentElementTrimMode.
     *
     * @return value of parentElementTrimMode
     */
    public String getParentElementTrimMode() {
        return parentElementTrimMode;
    }

    /**
     * Set parentElementTrimMode.
     *
     * @param parentElementTrimMode as java.lang.String
     */
    public void setParentElementTrimMode(String parentElementTrimMode) {
        this.parentElementTrimMode = parentElementTrimMode;
    }

    /**
     * Get queryDelimiter.
     *
     * @return value of queryDelimiter
     */
    public String getQueryDelimiter() {
        return queryDelimiter;
    }

    /**
     * Set queryDelimiter.
     *
     * @param queryDelimiter as java.lang.String
     */
    public void setQueryDelimiter(String queryDelimiter) {
        this.queryDelimiter = queryDelimiter;
    }

    /**
     * Get itemFieldXpath.
     *
     * @return value of itemFieldXpath
     */
    public String getItemFieldXpath() {
        return itemFieldXpath;
    }

    /**
     * Set itemFieldXpath.
     *
     * @param itemFieldXpath as java.lang.String
     */
    public void setItemFieldXpath(String itemFieldXpath) {
        this.itemFieldXpath = itemFieldXpath;
    }

    /**
     * Get itemFieldOwnerSubPath.
     *
     * @return value of itemFieldOwnerSubPath
     */
    public String getItemFieldOwnerSubPath() {
        return itemFieldOwnerSubPath;
    }

    /**
     * Set itemFieldOwnerSubPath.
     *
     * @param itemFieldOwnerSubPath as java.lang.String
     */
    public void setItemFieldOwnerSubPath(String itemFieldOwnerSubPath) {
        this.itemFieldOwnerSubPath = itemFieldOwnerSubPath;
    }

    /**
     * Get itemFieldOwnerMetadata.
     *
     * @return value of itemFieldOwnerMetadata
     */
    public String getItemFieldOwnerMetadata() {
        return itemFieldOwnerMetadata;
    }

    /**
     * Set itemFieldOwnerMetadata.
     *
     * @param itemFieldOwnerMetadata as java.lang.String
     */
    public void setItemFieldOwnerMetadata(String itemFieldOwnerMetadata) {
        this.itemFieldOwnerMetadata = itemFieldOwnerMetadata;
    }

    /**
     * Get itemFieldSignatureSubPath.
     *
     * @return value of itemFieldSignatureSubPath
     */
    public String getItemFieldSignatureSubPath() {
        return itemFieldSignatureSubPath;
    }

    /**
     * Set itemFieldSignatureSubPath.
     *
     * @param itemFieldSignatureSubPath as java.lang.String
     */
    public void setItemFieldSignatureSubPath(String itemFieldSignatureSubPath) {
        this.itemFieldSignatureSubPath = itemFieldSignatureSubPath;
    }

    /**
     * Get itemFieldSignatureMetadata.
     *
     * @return value of itemFieldSignatureMetadata
     */
    public String getItemFieldSignatureMetadata() {
        return itemFieldSignatureMetadata;
    }

    /**
     * Set itemFieldSignatureMetadata.
     *
     * @param itemFieldSignatureMetadata as java.lang.String
     */
    public void setItemFieldSignatureMetadata(String itemFieldSignatureMetadata) {
        this.itemFieldSignatureMetadata = itemFieldSignatureMetadata;
    }

    /**
     * Get idPrefix.
     *
     * @return value of idPrefix
     */
    public String getIdPrefix() {
        return idPrefix;
    }

    /**
     * Set idPrefix.
     *
     * @param idPrefix as java.lang.String
     */
    public void setIdPrefix(String idPrefix) {
        this.idPrefix = idPrefix;
    }

    /**
     * Get searchFields.
     *
     * @return value of searchFields
     */
    public List<SearchField> getSearchFields() {
        initialize(new SearchFieldDAO(), this.searchFields);
        if (Objects.isNull(this.searchFields)) {
            this.searchFields = new ArrayList<>();
        }
        return searchFields;
    }

    /**
     * Set searchFields.
     *
     * @param searchFields List of SearchField
     */
    public void setSearchFields(List<SearchField> searchFields) {
        this.searchFields = searchFields;
    }

    /**
     * Get urlParameters.
     *
     * @return value of urlParameters
     */
    public List<UrlParameter> getUrlParameters() {
        initialize(new UrlParameterDAO(), this.urlParameters);
        if (Objects.isNull(this.urlParameters)) {
            this.urlParameters = new ArrayList<>();
        }
        return urlParameters;
    }

    /**
     * Set urlParameters.
     *
     * @param urlParameters as List of UrlParameter
     */
    public void setUrlParameters(List<UrlParameter> urlParameters) {
        this.urlParameters = urlParameters;
    }

    /**
     * Get defaultSearchField.
     *
     * @return value of defaultSearchField
     */
    public SearchField getDefaultSearchField() {
        return defaultSearchField;
    }

    /**
     * Set defaultSearchField.
     *
     * @param defaultSearchField default SearchField
     */
    public void setDefaultSearchField(SearchField defaultSearchField) {
        this.defaultSearchField = defaultSearchField;
    }

    /**
     * Get idSearchField.
     *
     * @return value of idSearchField
     */
    public SearchField getIdSearchField() {
        return idSearchField;
    }

    /**
     * Set idSearchField.
     *
     * @param idSearchField as org.kitodo.data.database.beans.SearchField
     */
    public void setIdSearchField(SearchField idSearchField) {
        this.idSearchField = idSearchField;
    }

    /**
     * Get parentSearchField.
     *
     * @return value of parentSearchField
     */
    public SearchField getParentSearchField() {
        return parentSearchField;
    }

    /**
     * Set parentSearchField.
     *
     * @param parentSearchField as org.kitodo.data.database.beans.SearchField
     */
    public void setParentSearchField(SearchField parentSearchField) {
        this.parentSearchField = parentSearchField;
    }

    /**
     * Get defaultTemplateProcess.
     *
     * @return value of defaultTemplateProcess
     */
    public Process getDefaultTemplateProcess() {
        return defaultTemplateProcess;
    }

    /**
     * Set defaultTemplateProcess.
     *
     * @param defaultTemplateProcess as org.kitodo.data.database.beans.Process
     */
    public void setDefaultTemplateProcess(Process defaultTemplateProcess) {
        this.defaultTemplateProcess = defaultTemplateProcess;
    }

    /**
     * Get mappingFiles.
     *
     * @return value of mappingFiles
     */
    public List<MappingFile> getMappingFiles() {
        initialize(new MappingFileDAO(), this.mappingFiles);
        if (Objects.isNull(this.mappingFiles)) {
            this.mappingFiles = new ArrayList<>();
        }
        return mappingFiles;
    }

    /**
     * Set mappingFiles.
     *
     * @param mappingFiles List of MappingFile
     */
    public void setMappingFiles(List<MappingFile> mappingFiles) {
        this.mappingFiles = mappingFiles;
    }

    /**
     * Get parentMappingFile.
     *
     * @return value of parentMappingFile
     */
    public MappingFile getParentMappingFile() {
        return parentMappingFile;
    }

    /**
     * Set parentMappingFile.
     *
     * @param parentMappingFile as org.kitodo.data.database.beans.MappingFile
     */
    public void setParentMappingFile(MappingFile parentMappingFile) {
        this.parentMappingFile = parentMappingFile;
    }

    /**
     * Get sruVersion.
     *
     * @return value of sruVersion
     */
    public String getSruVersion() {
        return sruVersion;
    }

    /**
     * Set sruVersion.
     *
     * @param sruVersion as java.lang.String
     */
    public void setSruVersion(String sruVersion) {
        this.sruVersion = sruVersion;
    }

    /**
     * Get sruRecordSchema.
     *
     * @return value of sruRecordSchema
     */
    public String getSruRecordSchema() {
        return sruRecordSchema;
    }

    /**
     * Set sruRecordSchema.
     *
     * @param sruRecordSchema as java.lang.String
     */
    public void setSruRecordSchema(String sruRecordSchema) {
        this.sruRecordSchema = sruRecordSchema;
    }

    /**
     * Get oaiMetadataPrefix.
     *
     * @return value of oaiMetadataPrefix
     */
    public String getOaiMetadataPrefix() {
        return oaiMetadataPrefix;
    }

    /**
     * Set oaiMetadataPrefix.
     *
     * @param oaiMetadataPrefix as java.lang.String
     */
    public void setOaiMetadataPrefix(String oaiMetadataPrefix) {
        this.oaiMetadataPrefix = oaiMetadataPrefix;
    }

    /**
     * Get metadataRecordIdXPath.
     *
     * @return value of metadataRecordIdXPath
     */
    public String getMetadataRecordIdXPath() {
        return metadataRecordIdXPath;
    }

    /**
     * Set metadataRecordIdXPath.
     *
     * @param metadataRecordIdXPath as java.lang.String
     */
    public void setMetadataRecordIdXPath(String metadataRecordIdXPath) {
        this.metadataRecordIdXPath = metadataRecordIdXPath;
    }

    /**
     * Get metadataRecordTitleXPath.
     *
     * @return value of metadataRecordTitleXPath
     */
    public String getMetadataRecordTitleXPath() {
        return metadataRecordTitleXPath;
    }

    /**
     * Set metadataRecordTitleXPath.
     *
     * @param metadataRecordTitleXPath as java.lang.String
     */
    public void setMetadataRecordTitleXPath(String metadataRecordTitleXPath) {
        this.metadataRecordTitleXPath = metadataRecordTitleXPath;
    }

    /**
     * Get message key of configuration type.
     *
     * @return message key of configuration type
     */
    public String getConfigurationTypeKey() {
        if (StringUtils.isNotBlank(configurationType)) {
            try {
                return ImportConfigurationType.valueOf(configurationType).getMessageKey();
            } catch (IllegalArgumentException e) {
                return "";
            }
        }
        return "";
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof ImportConfiguration) {
            ImportConfiguration importConfiguration = (ImportConfiguration) object;
            return Objects.equals(this.getId(), importConfiguration.getId());
        }
        return false;
    }

    /**
     * hashCode method of current class.
     *
     * @see java.lang.Object#hashCode()
     * @return int
     */
    @Override
    public int hashCode() {
        return Objects.hash(
                title,
                description,
                configurationType,
                prestructuredImport,
                interfaceType,
                returnFormat,
                metadataFormat,
                defaultImportDepth,
                parentElementTrimMode,
                host,
                scheme,
                path,
                port,
                anonymousAccess,
                username,
                password,
                queryDelimiter,
                itemFieldXpath,
                itemFieldOwnerSubPath,
                itemFieldOwnerMetadata,
                itemFieldSignatureSubPath,
                itemFieldSignatureMetadata,
                idPrefix,
                searchFields,
                urlParameters,
                defaultSearchField,
                idSearchField,
                parentSearchField,
                defaultTemplateProcess,
                mappingFiles,
                parentMappingFile,
                sruVersion,
                sruRecordSchema,
                oaiMetadataPrefix,
                metadataRecordIdXPath,
                metadataRecordTitleXPath
        );
    }
}
