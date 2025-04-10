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

@Entity
@Table(name = "ltp_validation_configuration")
public class LtpValidationConfiguration extends BaseBean {
    
    /**
     * The title of this validation configuration.
     */
    @Column(name="title")
    private String title = "";

    @Column(name="mimeType")
    private String mimeType = "";

    @Column(name="requireNoErrorToUploadImage")
    private boolean requireNoErrorToUploadImage = false;

    @Column(name="requireNoErrorToFinishTask")
    private boolean requireNoErrorToFinishTask = false;

    /**
     * The list of validation conditions that are checked when validating files 
     * of a folder based on this configuration.
     */
    @OneToMany(mappedBy="ltpValidationConfiguration", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderColumn(name = "sorting")
    private List<LtpValidationCondition> validationConditions;

    @OneToMany(mappedBy = "ltpValidationConfiguration", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Folder> folders;


    public String getTitle() {
        return title;
    }

    public String getMimeType() {
        return mimeType;
    }

    public List<LtpValidationCondition> getValidationConditions() {
        return validationConditions;
    }

    public List<Folder> getFolders() {
        return this.folders;
    };

    public boolean getRequireNoErrorToUploadImage() {
        return this.requireNoErrorToUploadImage;
    }

    public boolean getRequireNoErrorToFinishTask() {
        return this.requireNoErrorToFinishTask;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public void setValidationConditions(List<LtpValidationCondition> validationConditions) {
        this.validationConditions = validationConditions;
    }

    public void setFolders(List<Folder> folders) {
        this.folders = folders;
    }

    public void setRequireNoErrorToUploadImage(boolean requireNoErrorToUploadImage) {
        this.requireNoErrorToUploadImage = requireNoErrorToUploadImage;
    }

    public void setRequireNoErrorToFinishTask(boolean requireNoErrorToFinishTask) {
        this.requireNoErrorToFinishTask = requireNoErrorToFinishTask;
    }

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

    @Override
    public int hashCode() {
        return Objects.hash(id, title, mimeType, requireNoErrorToUploadImage, requireNoErrorToFinishTask);
    }

}
