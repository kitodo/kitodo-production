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

package org.kitodo.dto;

import java.util.List;

public class TemplateDTO extends BaseDTO {

    private String origin;
    private ProcessDTO process;
    private List<PropertyDTO> properties;
    private Integer propertiesSize;

    /**
     * Get origin.
     * 
     * @return origin as String
     */
    public String getOrigin() {
        return origin;
    }

    /**
     * Set origin.
     * 
     * @param origin
     *            as String
     */
    public void setOrigin(String origin) {
        this.origin = origin;
    }

    /**
     * Get list of properties.
     *
     * @return list of properties as PropertyDTO
     */
    public List<PropertyDTO> getProperties() {
        return properties;
    }

    /**
     * Set list of properties.
     *
     * @param properties
     *            list of properties as PropertyDTO
     */
    public void setProperties(List<PropertyDTO> properties) {
        this.properties = properties;
    }

    /**
     * Get size of properties.
     * 
     * @return size od properties as Integer
     */
    public Integer getPropertiesSize() {
        return propertiesSize;
    }

    /**
     * Set size of properties.
     * 
     * @param propertiesSize
     *            as Integer
     */
    public void setPropertiesSize(Integer propertiesSize) {
        this.propertiesSize = propertiesSize;
    }

    /**
     * Get process as ProcessDTO.
     * 
     * @return process as ProcessDTO
     */
    public ProcessDTO getProcess() {
        return process;
    }

    /**
     * Set process as ProcessDTO.
     * 
     * @param process
     *            as ProcessDTO
     */
    public void setProcess(ProcessDTO process) {
        this.process = process;
    }
}
