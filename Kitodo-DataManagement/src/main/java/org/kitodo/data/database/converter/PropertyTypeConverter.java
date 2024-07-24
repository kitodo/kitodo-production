/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
