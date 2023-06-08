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

package org.kitodo.api.externaldatamanagement;

import java.util.Map;

import org.kitodo.api.schemaconverter.FileFormat;
import org.kitodo.api.schemaconverter.MetadataFormat;

public class DataImport {

    private String title;
    private SearchInterfaceType searchInterfaceType;
    private FileFormat returnFormat;
    private MetadataFormat metadataFormat;
    private String host;
    private String scheme;
    private String path;
    private int port;
    private Boolean anonymousAccess;
    private String username;
    private String password;
    private String idPrefix;
    private String idParameter;
    private Map<String, String> searchFields;
    private Map<String, String> urlParameters;
    private String recordIdXPath;
    private String recordTitleXPath;

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
     * Get searchInterfaceType.
     *
     * @return value of searchInterfaceType
     */
    public SearchInterfaceType getSearchInterfaceType() {
        return searchInterfaceType;
    }

    /**
     * Set searchInterfaceType.
     *
     * @param searchInterfaceType as org.kitodo.api.externaldatamanagement.SearchInterfaceType
     */
    public void setSearchInterfaceType(SearchInterfaceType searchInterfaceType) {
        this.searchInterfaceType = searchInterfaceType;
    }

    /**
     * Get returnFormat.
     *
     * @return value of returnFormat
     */
    public FileFormat getReturnFormat() {
        return returnFormat;
    }

    /**
     * Set returnFormat.
     *
     * @param returnFormat as org.kitodo.api.schemaconverter.FileFormat
     */
    public void setReturnFormat(FileFormat returnFormat) {
        this.returnFormat = returnFormat;
    }

    /**
     * Get metadataFormat.
     *
     * @return value of metadataFormat
     */
    public MetadataFormat getMetadataFormat() {
        return metadataFormat;
    }

    /**
     * Set metadataFormat.
     *
     * @param metadataFormat as org.kitodo.api.schemaconverter.MetadataFormat
     */
    public void setMetadataFormat(MetadataFormat metadataFormat) {
        this.metadataFormat = metadataFormat;
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
    public int getPort() {
        return port;
    }

    /**
     * Set port.
     *
     * @param port port of URL
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Get anonymousAccess.
     *
     * @return value of anonymousAccess
     */
    public Boolean getAnonymousAccess() {
        return anonymousAccess;
    }

    /**
     * Set anonymousAccess.
     *
     * @param anonymousAccess as java.lang.Boolean
     */
    public void setAnonymousAccess(Boolean anonymousAccess) {
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
     * Get idParameter.
     *
     * @return value of idParameter
     */
    public String getIdParameter() {
        return idParameter;
    }

    /**
     * Set idParameter.
     *
     * @param idParameter as java.lang.String
     */
    public void setIdParameter(String idParameter) {
        this.idParameter = idParameter;
    }

    /**
     * Get searchFields.
     *
     * @return value of searchFields
     */
    public Map<String, String> getSearchFields() {
        return searchFields;
    }

    /**
     * Set searchFields.
     *
     * @param searchFields as Map
     */
    public void setSearchFields(Map<String, String> searchFields) {
        this.searchFields = searchFields;
    }

    /**
     * Get urlParameters.
     *
     * @return value of urlParameters
     */
    public Map<String, String> getUrlParameters() {
        return urlParameters;
    }

    /**
     * Set urlParameters.
     *
     * @param urlParameters as Map
     */
    public void setUrlParameters(Map<String, String> urlParameters) {
        this.urlParameters = urlParameters;
    }

    /**
     * Get recordIdXPath.
     *
     * @return value of recordIdXPath
     */
    public String getRecordIdXPath() {
        return recordIdXPath;
    }

    /**
     * Set recordIdXPath.
     *
     * @param recordIdXPath as java.lang.String
     */
    public void setRecordIdXPath(String recordIdXPath) {
        this.recordIdXPath = recordIdXPath;
    }

    /**
     * Get recordTitleXPath.
     *
     * @return value of recordTitleXPath
     */
    public String getRecordTitleXPath() {
        return recordTitleXPath;
    }

    /**
     * Set recordTitleXPath.
     *
     * @param recordTitleXPath as java.lang.String
     */
    public void setRecordTitleXPath(String recordTitleXPath) {
        this.recordTitleXPath = recordTitleXPath;
    }

}
