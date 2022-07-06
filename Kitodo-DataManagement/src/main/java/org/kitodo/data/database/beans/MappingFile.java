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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
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

    @ManyToMany(mappedBy = "mappingFiles", cascade = CascadeType.PERSIST)
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
}
