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

package org.kitodo.data.database.converter;

import java.util.Objects;

import javax.persistence.AttributeConverter;

import org.kitodo.data.database.enums.PropertyType;

public class PropertyTypeConverter implements AttributeConverter<PropertyType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(PropertyType propertyType) {
        if (Objects.nonNull(propertyType)) {
            return propertyType.getId();
        }
        return PropertyType.UNKNOWN.getId();
    }

    @Override
    public PropertyType convertToEntityAttribute(Integer propertyTypeValue) {
        if (Objects.nonNull(propertyTypeValue)) {
            return PropertyType.getById(propertyTypeValue);
        }
        return PropertyType.UNKNOWN;
    }
}
