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

    public FolderDTO() {
        this.mimeType = "image/jpeg";
        this.path = "";
        this.linkingMode = LinkingMode.ALL;
        this.copyFolder = true;
        this.createFolder = true;
        this.validateFolder = true;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFileGroup() {
        return fileGroup;
    }

    public void setFileGroup(String fileGroup) {
        this.fileGroup = fileGroup;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUrlStructure() {
        return urlStructure;
    }

    public void setUrlStructure(String urlStructure) {
        this.urlStructure = urlStructure;
    }

    public LinkingMode getLinkingMode() {
        return linkingMode;
    }

    public void setLinkingMode(LinkingMode linkingMode) {
        this.linkingMode = linkingMode;
    }

    public Integer getImageSize() {
        return imageSize;
    }

    public void setImageSize(Integer imageSize) {
        this.imageSize = imageSize;
    }

    public Integer getDpi() {
        return dpi;
    }

    public void setDpi(Integer dpi) {
        this.dpi = dpi;
    }

    public Double getDerivative() {
        return derivative;
    }

    public void setDerivative(Double derivative) {
        this.derivative = derivative;
    }

    public boolean isCopyFolder() {
        return copyFolder;
    }

    public void setCopyFolder(boolean copyFolder) {
        this.copyFolder = copyFolder;
    }

    public boolean isCreateFolder() {
        return createFolder;
    }

    public void setCreateFolder(boolean createFolder) {
        this.createFolder = createFolder;
    }

    public boolean isValidateFolder() {
        return validateFolder;
    }

    public void setValidateFolder(boolean validateFolder) {
        this.validateFolder = validateFolder;
    }

    public Integer getLtpValidationConfigurationId() {
        return ltpValidationConfigurationId;
    }

    public void setLtpValidationConfigurationId(Integer ltpValidationConfigurationId) {
        this.ltpValidationConfigurationId = ltpValidationConfigurationId;
    }
}