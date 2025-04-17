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

import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

/**
 * Various settings that influence how images of a folder are validated for long-term-preservation.
 */
@Entity
@Table(name = "ltp_validation_configuration")
public class LtpValidationConfiguration extends BaseBean {
    
    @Column(name = "title")
    private String title = "";

    @Column(name = "mimeType")
    private String mimeType = "";

    @Column(name = "requireNoErrorToUploadImage")
    private boolean requireNoErrorToUploadImage = false;

    @Column(name = "requireNoErrorToFinishTask")
    private boolean requireNoErrorToFinishTask = false;

    @OneToMany(mappedBy = "ltpValidationConfiguration", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderColumn(name = "sorting")
    private List<LtpValidationCondition> validationConditions;

    @OneToMany(mappedBy = "ltpValidationConfiguration", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Folder> folders;

    /**
     * Return the title of this validation configuration.
     * @return the ittle of this validation configuration
     */
    public String getTitle() {
        return title;
    }

    /**
     * Return the mime type of files this validation configuration can be applied to.
     * 
     * @return the mime type of files this validation configuratio can be applied to.
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Return a list of validation conditions that are checked against an image that 
     * is validated with this configuration.
     * 
     * @return the lis of validation conditions that will be checked
     */
    public List<LtpValidationCondition> getValidationConditions() {
        return validationConditions;
    }

    /**
     * The list of folders this validation configuration is applied to.
     * 
     * @return the list of folders this validation configuration applied to
     */
    public List<Folder> getFolders() {
        return this.folders;
    }

    /**
     * Whether uploading an image should be canceled if an error was found during validation.
     * 
     * @return true if upliading an image should be canceled if an error was found during validation
     */
    public boolean getRequireNoErrorToUploadImage() {
        return this.requireNoErrorToUploadImage;
    }

    /**
     * Whether finishing an image validation task should be canceled if an error was found during validation.
     * 
     * @return true if finishing an image validation task should be canceled if an error was found during validation
     */
    public boolean getRequireNoErrorToFinishTask() {
        return this.requireNoErrorToFinishTask;
    }

    /**
     * Sets the title of this validation configuration.
     * 
     * @param title the title of this validation configuration
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Sets the mime type this validation configuration can be applied to.
     * 
     * @param mimeType the mime type this validation configuration can be  applied to
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * Sets the list of validation conditions that are checked for an image.
     * 
     * @param validationConditions the list of validation conditions that are checked for an image
     */
    public void setValidationConditions(List<LtpValidationCondition> validationConditions) {
        this.validationConditions = validationConditions;
    }

    /**
     * Sets the list of folders that use this validation configuration.
     * 
     * @param folders the list of folders that use this validation configuration
     */
    public void setFolders(List<Folder> folders) {
        this.folders = folders;
    }

    /**
     * Sets whether uploading an image should be canceled if an error was found during validation.
     * 
     * @param requireNoErrorToUploadImage true if uploading an image should be canceled
     */
    public void setRequireNoErrorToUploadImage(boolean requireNoErrorToUploadImage) {
        this.requireNoErrorToUploadImage = requireNoErrorToUploadImage;
    }

    /**
     * Sets whether finishing an image validation task should be canceled if an error was found during validation.
     * @param requireNoErrorToFinishTask true if finishing an image validation task should be canceled
     */
    public void setRequireNoErrorToFinishTask(boolean requireNoErrorToFinishTask) {
        this.requireNoErrorToFinishTask = requireNoErrorToFinishTask;
    }

    /**
     * Equals implementation based on database id.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object instanceof LtpValidationConfiguration) {
            LtpValidationConfiguration configuration = (LtpValidationConfiguration) object;
            return Objects.equals(this.getId(), configuration.getId());
        }

        return false;
    }

    /**
     * Hash code implementation based on all properties of validation configuration.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, title, mimeType, requireNoErrorToUploadImage, requireNoErrorToFinishTask, validationConditions);
    }

}
