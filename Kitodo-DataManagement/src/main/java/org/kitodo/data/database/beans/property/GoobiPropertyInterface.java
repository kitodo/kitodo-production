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

package org.kitodo.data.database.beans.property;

import java.util.Date;

import org.kitodo.data.database.helper.enums.PropertyType;

public interface GoobiPropertyInterface {

    Integer getId();

    void setId(Integer id);

    String getTitle();

    void setTitle(String title);

    String getValue();

    void setValue(String value);

    String getChoice();

    void setChoice(String choice);

    Boolean isObligatory();

    void setObligatory(Boolean obligatory);

    Date getCreationDate();

    void setCreationDate(Date creation);

    /**
     * Get data type as {@link PropertyType}.
     *
     * @return current data type
     */
    PropertyType getType();

    /**
     * Set data type to specific value from {@link PropertyType}.
     *
     * @param dataType
     *            as {@link PropertyType}
     */
    void setType(PropertyType dataType);

    Integer getContainer();

    void setContainer(Integer container);
}
