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

package org.kitodo.production.forms.dto;

import org.kitodo.data.database.enums.LinkingMode;

public class FolderDTO {

    private Integer id;
    private String fileGroup;
    private String mimeType;
    private String path;
    private String urlStructure;

    private LinkingMode linkingMode;

    private Integer imageSize;
    private Integer dpi;
    private Double derivative;

    private boolean copyFolder;
    private boolean createFolder;
    private boolean validateFolder;

    private Integer ltpValidationConfigurationId;

    /**
     * Initializes the folder configuration with default values.
     */
    public FolderDTO() {
        this.mimeType = "image/jpeg";
        this.path = "";
        this.linkingMode = LinkingMode.ALL;
        this.copyFolder = true;
        this.createFolder = true;
        this.validateFolder = true;
    }

    /**
     * Returns the unique identifier of the folder configuration.
     *
     * @return the folder ID
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the folder configuration.
     *
     * @param id the folder ID to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Returns the file group associated with this folder.
     *
     * @return the file group name
     */
    public String getFileGroup() {
        return fileGroup;
    }

    /**
     * Sets the file group associated with this folder.
     *
     * @param fileGroup the file group to set
     */
    public void setFileGroup(String fileGroup) {
        this.fileGroup = fileGroup;
    }

    /**
     * Returns the MIME type used for files in this folder.
     *
     * @return the MIME type
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Sets the MIME type used for files in this folder.
     *
     * @param mimeType the MIME type to set
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * Returns the file system path of the folder.
     *
     * @return the folder path
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the file system path of the folder.
     *
     * @param path the folder path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Returns the URL structure associated with this folder.
     *
     * @return the URL structure
     */
    public String getUrlStructure() {
        return urlStructure;
    }

    /**
     * Sets the URL structure associated with this folder.
     *
     * @param urlStructure the URL structure to set
     */
    public void setUrlStructure(String urlStructure) {
        this.urlStructure = urlStructure;
    }

    /**
     * Returns the linking mode used for this folder.
     *
     * @return the linking mode
     */
    public LinkingMode getLinkingMode() {
        return linkingMode;
    }

    /**
     * Sets the linking mode used for this folder.
     *
     * @param linkingMode the linking mode to set
     */
    public void setLinkingMode(LinkingMode linkingMode) {
        this.linkingMode = linkingMode;
    }

    /**
     * Returns the image size setting.
     *
     * @return the image size
     */
    public Integer getImageSize() {
        return imageSize;
    }

    /**
     * Sets the image size setting.
     *
     * @param imageSize the image size to set
     */
    public void setImageSize(Integer imageSize) {
        this.imageSize = imageSize;
    }

    /**
     * Returns the DPI (dots per inch) configuration.
     *
     * @return the DPI value
     */
    public Integer getDpi() {
        return dpi;
    }

    /**
     * Sets the DPI (dots per inch) configuration.
     *
     * @param dpi the DPI value to set
     */
    public void setDpi(Integer dpi) {
        this.dpi = dpi;
    }

    /**
     * Returns the derivative value used for processing.
     *
     * @return the derivative value
     */
    public Double getDerivative() {
        return derivative;
    }

    /**
     * Sets the derivative value used for processing.
     *
     * @param derivative the derivative value to set
     */
    public void setDerivative(Double derivative) {
        this.derivative = derivative;
    }

    /**
     * Indicates whether the folder should be copied.
     *
     * @return true if the folder should be copied, false otherwise
     */
    public boolean isCopyFolder() {
        return copyFolder;
    }

    /**
     * Sets whether the folder should be copied.
     *
     * @param copyFolder true to enable copying, false otherwise
     */
    public void setCopyFolder(boolean copyFolder) {
        this.copyFolder = copyFolder;
    }

    /**
     * Indicates whether the folder should be created if it does not exist.
     *
     * @return true if the folder should be created, false otherwise
     */
    public boolean isCreateFolder() {
        return createFolder;
    }

    /**
     * Sets whether the folder should be created if it does not exist.
     *
     * @param createFolder true to enable creation, false otherwise
     */
    public void setCreateFolder(boolean createFolder) {
        this.createFolder = createFolder;
    }

    /**
     * Indicates whether the folder should be validated.
     *
     * @return true if validation is enabled, false otherwise
     */
    public boolean isValidateFolder() {
        return validateFolder;
    }

    /**
     * Sets whether the folder should be validated.
     *
     * @param validateFolder true to enable validation, false otherwise
     */
    public void setValidateFolder(boolean validateFolder) {
        this.validateFolder = validateFolder;
    }

    /**
     * Returns the ID of the LTP validation configuration.
     *
     * @return the validation configuration ID
     */
    public Integer getLtpValidationConfigurationId() {
        return ltpValidationConfigurationId;
    }

    /**
     * Sets the ID of the LTP validation configuration.
     *
     * @param ltpValidationConfigurationId the configuration ID to set
     */
    public void setLtpValidationConfigurationId(Integer ltpValidationConfigurationId) {
        this.ltpValidationConfigurationId = ltpValidationConfigurationId;
    }
}
