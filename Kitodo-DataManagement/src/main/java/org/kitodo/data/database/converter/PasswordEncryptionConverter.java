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

import javax.persistence.AttributeConverter;

import org.kitodo.data.database.enums.PasswordEncryption;

public class PasswordEncryptionConverter implements AttributeConverter<PasswordEncryption, Integer> {

    @Override
    public Integer convertToDatabaseColumn(PasswordEncryption passwordEncryption) {
        return passwordEncryption.getValue();
    }

    @Override
    public PasswordEncryption convertToEntityAttribute(Integer passwordEncryptionValue) {
        return PasswordEncryption.getEncryptionFromValue(passwordEncryptionValue);
    }
}
