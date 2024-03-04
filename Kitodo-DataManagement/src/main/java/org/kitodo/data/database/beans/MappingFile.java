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
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity(name = "MappingFile")
@Table(name = "mappingfile")
public class MappingFile extends BaseBean {

    @Column(name = "title")
    private String title;

    @Column(name = "file")
    private String file;

    @Column(name = "input_metadata_format")
    private String inputMetadataFormat;

    @Column(name = "output_metadata_format")
    private String outputMetadataFormat;

    @Column(name = "prestructured_import")
    private Boolean prestructuredImport = false;

    @ManyToMany(mappedBy = "mappingFiles", cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private List<ImportConfiguration> importConfigurations;

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
     * Get file.
     *
     * @return value of file
     */
    public String getFile() {
        return file;
    }

    /**
     * Set file.
     *
     * @param file as java.lang.String
     */
    public void setFile(String file) {
        this.file = file;
    }

    /**
     * Get inputMetadataFormat.
     *
     * @return value of inputMetadataFormat
     */
    public String getInputMetadataFormat() {
        return inputMetadataFormat;
    }

    /**
     * Set inputMetadataFormat.
     *
     * @param inputMetadataFormat as java.lang.String
     */
    public void setInputMetadataFormat(String inputMetadataFormat) {
        this.inputMetadataFormat = inputMetadataFormat;
    }

    /**
     * Get outputMetadataFormat.
     *
     * @return value of outputMetadataFormat
     */
    public String getOutputMetadataFormat() {
        return outputMetadataFormat;
    }

    /**
     * Set outputMetadataFormat.
     *
     * @param outputMetadataFormat as java.lang.String
     */
    public void setOutputMetadataFormat(String outputMetadataFormat) {
        this.outputMetadataFormat = outputMetadataFormat;
    }

    /**
     * Get importConfigurations.
     *
     * @return value of importConfigurations
     */
    public List<ImportConfiguration> getImportConfigurations() {
        return importConfigurations;
    }

    /**
     * Set importConfigurations.
     *
     * @param importConfigurations List of ImportConfiguration
     */
    public void setImportConfigurations(List<ImportConfiguration> importConfigurations) {
        this.importConfigurations = importConfigurations;
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

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof MappingFile) {
            MappingFile mappingFile = (MappingFile) object;
            return mappingFile.getId().equals(this.getId());
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
                file,
                inputMetadataFormat,
                outputMetadataFormat,
                prestructuredImport,
                importConfigurations
            );
    }
}
