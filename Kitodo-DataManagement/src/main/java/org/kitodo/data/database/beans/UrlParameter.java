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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "urlparameter")
public class UrlParameter extends BaseBean {

    @Column(name = "parameter_key")
    private String parameterKey;

    @Column(name = "parameter_value")
    private String parameterValue;

    @ManyToOne
    @JoinColumn(name = "importconfiguration_id",
            foreignKey = @ForeignKey(name = "FK_urlparameter_importconfiguration"))
    private ImportConfiguration importConfiguration;

    /**
     * Get parameterKey.
     *
     * @return value of parameterKey
     */
    public String getParameterKey() {
        return parameterKey;
    }

    /**
     * Set parameterKey.
     *
     * @param parameterKey as java.lang.String
     */
    public void setParameterKey(String parameterKey) {
        this.parameterKey = parameterKey;
    }

    /**
     * Get parameterValue.
     *
     * @return value of parameterValue
     */
    public String getParameterValue() {
        return parameterValue;
    }

    /**
     * Set parameterValue.
     *
     * @param parameterValue as java.lang.String
     */
    public void setParameterValue(String parameterValue) {
        this.parameterValue = parameterValue;
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
}
