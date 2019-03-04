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
