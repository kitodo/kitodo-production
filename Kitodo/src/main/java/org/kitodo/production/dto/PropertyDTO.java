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

package org.kitodo.production.dto;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.kitodo.data.database.beans.BaseBean;

/**
 * Property DTO object.
 */
public class PropertyDTO extends BaseDTO {

    private String title;
    private String value;
    private String creationDate;

    /**
     * Get title.
     *
     * @return title as String
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set title.
     *
     * @param title
     *            as String
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get value.
     *
     * @return value as String
     */
    public String getValue() {
        return value;
    }

    /**
     * Set value.
     *
     * @param value
     *            as String
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Get creation date as String.
     *
     * @return creation date as String.
     */
    public String getCreationTime() {
        return creationDate;
    }

    /**
     * Set creation date as String.
     *
     * @param creationDate
     *            as String
     */
    public void setCreationTime(String creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Returns the creation time of the property. {@link Date} is a specific
     * instant in time, with millisecond precision.
     *
     * @return the creation time
     */
    public Date getCreationDate() {
        try {
            return StringUtils.isNotBlank(this.creationDate)
                    ? new SimpleDateFormat(BaseBean.DATE_FORMAT).parse(this.creationDate)
                    : null;
        } catch (ParseException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = Objects.nonNull(creationDate) ? new SimpleDateFormat(BaseBean.DATE_FORMAT).format(creationDate)
                : null;
    }
}
